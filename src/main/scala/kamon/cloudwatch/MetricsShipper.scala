package kamon.cloudwatch

import java.util.concurrent.atomic.AtomicReference
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.cloudwatch.model.{MetricDatum, PutMetricDataRequest}

import java.net.URI
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._
import scala.util.control.NonFatal

private[cloudwatch] class MetricsShipper(configuration: Configuration) {

  private val logger =
    LoggerFactory.getLogger(classOf[MetricsShipper].getPackage.getName)

  // Kamon 1.0+ requires to support hot-reconfiguration, which forces us to use an
  // AtomicReference here and hope for the best
  private val client: AtomicReference[CloudWatchAsyncClient] =
    new AtomicReference(buildClient(configuration))

  def reconfigure(configuration: Configuration): Unit = {
    val oldClient = client.getAndSet(buildClient(configuration))
    if (oldClient != null) {
      disposeClient(oldClient)
    }
  }

  def shutdown(): Unit = {
    val oldClient = client.getAndSet(null)
    if (oldClient != null) {
      disposeClient(oldClient)
    }
  }

  def shipMetrics(datums: Vector[MetricDatum])(
      implicit ec: ExecutionContext
  ): Future[Unit] = {
    logger.debug("Sending batch of {} metrics to CloudWatch {}", datums.size, configuration.nameSpace)
    val request = PutMetricDataRequest.builder()
      .namespace(configuration.nameSpace)
      .metricData(datums.asJava)
      .build();

    client.get
      .putMetricData(request)
      .asScala
      .map(result => logger.debug(s"Succeeded to push metric batch to Cloudwatch: $result"))
      .recover {
        case CloudWatchUnavailable =>
          logger.warn("Failed to send metric batch to Cloudwatch. Service temporarily unavailable.")
      }
  }

  private[this] def buildClient(configuration: Configuration): CloudWatchAsyncClient = {
      configuration.cloudwatchEndpointOverride.fold(
        CloudWatchAsyncClient.create()
      ) { endpoint =>
        CloudWatchAsyncClient.builder().endpointOverride(new URI(endpoint)).build()
      }
    }

  private[this] def disposeClient(client: CloudWatchAsyncClient): Unit =
    try {
      client.close()
    } catch {
      case NonFatal(_) => // ignore exception
    }

}
