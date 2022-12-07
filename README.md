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
kamon {
  cloudwatch {

    # namespace is the AWS Metrics custom namespace
    namespace = <application name>
    
    # (Optional) AWS region, on ec2 region is fetched by getCurrentRegion command
    region = eu-west-1

    # batch size of data when send to Cloudwatch. Default: 20
    batch-size = 20

    # how many threads will be assigned to the pool that does the shipment of metrics. Default: 5
    async-threads = 5
    
    # whether to add Kamon environment tags to each of the metrics. Default: false
    include-environment-tags = false

    # explicit aws access key and secret definition (optional)
    # if not specified values fetched from one of the followings
    #   * AWS_PROFILE env variable
    #   * AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY env variables
    #   * Amazon EC2 Instance Metadata
    # access-key-id = ""
    # secret-access-key = ""

  }
}
```


# License
- [Apache V2](https://github.com/pl.iterators/kamon-cloudwatch/blob/master/LICENSE "MIT")
