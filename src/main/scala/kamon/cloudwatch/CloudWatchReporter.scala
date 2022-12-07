package kamon.cloudwatch

import com.typesafe.config.Config
import kamon.Kamon
import kamon.metric.PeriodSnapshot
import kamon.module.{MetricReporter, ModuleFactory}
import kamon.tag.TagSet
import org.slf4j.LoggerFactory

import java.util.concurrent.{ExecutorService, Executors}
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

final class CloudWatchModuleFactory extends ModuleFactory {
  private[this] val logger = LoggerFactory.getLogger(classOf[MetricsShipper].getPackage.getName)

  override def create(settings: ModuleFactory.Settings): CloudWatchReporter = {
    logger.info("Starting the Kamon CloudWatch reporter.")
    val cfg = Configuration.fromConfig(settings.config)
    new CloudWatchReporter(cfg)
  }
}

final class CloudWatchReporter private[cloudwatch] (cfg: Configuration)
    extends MetricReporter {

  private[this] val logger = LoggerFactory.getLogger(classOf[MetricsShipper].getPackage.getName)

  private[this] val configuration: AtomicReference[Configuration] =
    new AtomicReference(cfg)

  private[this] val shipper: MetricsShipper = new MetricsShipper(cfg)

  private[this] val executorService: ExecutorService = Executors.newFixedThreadPool(cfg.executionContextThreads)
  private[this] implicit val executionContext = ExecutionContext.fromExecutor(executorService)

  override def stop(): Unit = {
    logger.info("Shutting down the Kamon CloudWatch reporter.")
    shipper.shutdown()
  }

  override def reconfigure(config: Config): Unit = {
    val current = configuration.get
    if (configuration.compareAndSet(current, Configuration.fromConfig(config))) {
      shipper.reconfigure(configuration.get)
      logger.info("Configuration reloaded successfully.")
    } else {
      logger.debug("Configuration hasn't changed from the last reload")
    }
  }

  override def reportPeriodSnapshot(snapshot: PeriodSnapshot): Unit = {
    val config = configuration.get
    val metrics = datums(
      snapshot,
      CloudWatchReporter.environmentTags(config)
    ).grouped(config.batchSize)

    Future.traverse(metrics)(shipper.shipMetrics).onComplete {
      case Success(_) =>
        logger.debug("Metrics shipment has completed successfully.")

      case Failure(exception) =>
        logger.error("Unexpected error shipping metrics to CloudWatch!", exception)
    }
  }

}

object CloudWatchReporter {

  private def environmentTags(config: Configuration): TagSet =
    if (config.includeEnvironmentTags) Kamon.environment.tags else TagSet.Empty

}
