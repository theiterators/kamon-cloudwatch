# Kamon AWS Cloudwatch Extension


## Overview

A simple [Kamon](https://github.com/kamon-io/Kamon) extension to ship metrics data to Cloudwatch using Amazon's async client.

_**Note:** This project has been initially forked from [Timeout's kamon-cloudwatch](https://github.com/timeoutdigital/kamon-cloudwatch) but evolved separately as the original one has fallen out of maintenance._

## Version Compatibility Matrix

The following table maps Kamon core version with the version of this library:

| Kamon Core | Kamon CloudWatch | Scala | JDK  |
|-----------:|-----------------:|------:|-----:|
|      2.5.x |            2.5.x |  2.13 | 1.8+ |

## Getting Started

Add library dependency to your `build.sbt`

```scala
libraryDependencies += "pl.iterators" %% "kamon-cloudwatch" % "<version>"
```

The module will be loaded automatically and you should see "_Starting the Kamon CloudWatch extension_" message in your logs output.

> **Note:** Be sure the box in which this is going to be used, has the proper access credentials to send data to AWS CloudWatch. The preferred approach would be to either use an _InstanceProfile_ or roles in the case of ECS/Docker Containers. Another way would be to have the environment variables `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` set correctly.


## Configuration

You can configure the module behaviour by overriding any of the following settings in your `application.conf` file:

```
kamon.cloudwatch {
  # namespace is the AWS Metrics custom namespace
  namespace = kamon-cloudwatch

  # batch size of data when send to Cloudwatch
  batch-size = 20

  # Executor context threads.
  execution-context-threads = 4

  # You can override cloudwatch endpoint. Useful for testing against localstack
  # cloudwatch-endpoint-override = ""

  # Whether to include Kamon environmental tag to cloudwatch metrics. Default is false
  # include-environment-tags = false
}

kamon.modules.cloudwatch.enabled = true
```


# License
- [Apache V2](https://github.com/pl.iterators/kamon-cloudwatch/blob/master/LICENSE "MIT")
