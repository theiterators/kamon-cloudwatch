package kamon.cloudwatch

import com.typesafe.config.Config

final case class Configuration(
  nameSpace: String,
  batchSize: Int,
  cloudwatchEndpointOverride: Option[String],
  includeEnvironmentTags: Boolean,
  executionContextThreads: Int
)

object Configuration {
  final val Namespace = "kamon.cloudwatch"

  private object settings {
    val Namespace                  = "namespace"
    val BatchSize                  = "batch-size"
    val CloudwatchEndpointOverride = "cloudwatch-endpoint-override"
    val IncludeEnvironmentTags     = "include-environment-tags"
    val ExecutionContextThreads    = "execution-context-threads"
  }

  def fromConfig(topLevelCfg: Config): Configuration = {
    val config = topLevelCfg.getConfig(Namespace)

    def opt[A](path: String, f: Config => String => A): Option[A] =
      if (config.hasPath(path)) Option(f(config)(path))
      else None

    val nameSpace               = config.getString(settings.Namespace)
    val batchSize               = config.getInt(settings.BatchSize)
    val endpoint                = opt(settings.CloudwatchEndpointOverride, _.getString).filterNot(_.isEmpty)
    val includeEnvTags          = opt(settings.IncludeEnvironmentTags, _.getBoolean).getOrElse(false)
    val executionContextThreads = config.getInt(settings.ExecutionContextThreads)


    Configuration(
      nameSpace,
      batchSize,
      endpoint,
      includeEnvTags,
      executionContextThreads
    )
  }

}
