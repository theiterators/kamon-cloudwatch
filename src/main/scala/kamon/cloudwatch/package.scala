package kamon

import kamon.metric.{Metric, _}
import kamon.tag.{Tag, TagSet}
import software.amazon.awssdk.services.cloudwatch.model._

import java.util
import java.util.Date
import scala.jdk.CollectionConverters._

package object cloudwatch {

  /**
    * Produce the datums.
    * Code is taken from:
    * https://github.com/philwill-nap/Kamon/blob/master/kamon-cloudwatch/src/main/scala/kamon/cloudwatch/CloudWatchMetricsSender.scala
    * and adjust ;)
    */
  private[cloudwatch] def datums(snapshot: PeriodSnapshot, baseTags: TagSet): Vector[MetricDatum] = {
    def unitAndScale(unit: MeasurementUnit): (StandardUnit, Double) = {
      import MeasurementUnit.Dimension._
      import MeasurementUnit.{information, time}

      unit.dimension match {
        case Percentage => StandardUnit.PERCENT -> 1.0

        case Time if unit.magnitude == time.seconds.magnitude =>
          StandardUnit.SECONDS -> 1.0
        case Time if unit.magnitude == time.milliseconds.magnitude =>
          StandardUnit.MILLISECONDS -> 1.0
        case Time if unit.magnitude == time.microseconds.magnitude =>
          StandardUnit.MICROSECONDS -> 1.0
        case Time if unit.magnitude == time.nanoseconds.magnitude =>
          StandardUnit.MICROSECONDS -> 1E-3

        case Information if unit.magnitude == information.bytes.magnitude =>
          StandardUnit.BYTES -> 1.0
        case Information if unit.magnitude == information.kilobytes.magnitude =>
          StandardUnit.KILOBYTES -> 1.0
        case Information if unit.magnitude == information.megabytes.magnitude =>
          StandardUnit.MEGABYTES -> 1.0
        case Information if unit.magnitude == information.gigabytes.magnitude =>
          StandardUnit.GIGABYTES -> 1.0

        case _ => StandardUnit.COUNT -> 1.0
      }
    }

    def datum(name: String, tags: TagSet, unit: StandardUnit): MetricDatum = {
      val dimensions: util.Collection[Dimension] =
        (baseTags withTags tags)
          .iterator()
          .map { tag =>
            Dimension
              .builder()
              .name(tag.key)
              .value(Tag.unwrapValue(tag).toString)
              .build()
          }
          .toList.asJavaCollection

      MetricDatum
        .builder()
        .metricName(name)
        .timestamp(Date.from(snapshot.to).toInstant)
        .unit(unit)
        .dimensions(dimensions)
        .build()
    }

    def datumFromDistribution(
        distSnap: MetricSnapshot[Metric.Settings.ForDistributionInstrument, Distribution]
    ): Seq[MetricDatum] = {
      val (unit, scale) = unitAndScale(distSnap.settings.unit)
      distSnap.instruments.filter(_.value.count > 0).map { snap =>
        val statisticSet = StatisticSet
          .builder()
          .maximum(snap.value.max.toDouble * scale)
          .minimum(snap.value.min.toDouble * scale)
          .sampleCount(snap.value.count.toDouble)
          .sum(snap.value.sum.toDouble * scale)
          .build()

        datum(distSnap.name, snap.tags, unit)
          .toBuilder
          .statisticValues(statisticSet)
          .build()
      }
    }

    def datumFromValue[T](
        valueSnap: MetricSnapshot[Metric.Settings.ForValueInstrument, T]
    )(implicit T: Numeric[T]): Seq[MetricDatum] = {
      val (unit, scale) = unitAndScale(valueSnap.settings.unit)

      valueSnap.instruments.map { snap =>
        datum(valueSnap.name, snap.tags, unit)
          .toBuilder
          .value(T.toDouble(snap.value) * scale)
          .build()
      }
    }

    val allDatums =
      snapshot.histograms.view.flatMap(datumFromDistribution) ++
        snapshot.rangeSamplers.flatMap(datumFromDistribution) ++
        snapshot.gauges.view.flatMap(datumFromValue[Double]) ++
        snapshot.counters.view.flatMap(datumFromValue[Long])

    allDatums.toVector
  }

}
