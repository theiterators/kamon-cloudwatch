package kamon.cloudwatch

import java.time.Instant
import java.util.Date
import kamon.metric.MeasurementUnit
import kamon.tag.TagSet
import kamon.testkit.MetricSnapshotBuilder

import scala.jdk.CollectionConverters._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.services.cloudwatch.model.{Dimension, StandardUnit}

class DatumConversionSpec extends AnyFlatSpec with Matchers {

  "datums" must "ignore distributions without samples" in {
    val snapshot = PeriodSnapshotBuilder()
      .withHistogram(MetricSnapshotBuilder.histogram("foo", TagSet.Empty)(List.empty[Long]: _*))
      .build()

    val convertedDatums = datums(snapshot, TagSet.Empty)

    convertedDatums shouldBe Vector.empty
  }

  it must "use the 'to' instant as the timestamp of the datum" in {
    val givenInstant = Instant.ofEpochMilli(98439)

    val snapshot = PeriodSnapshotBuilder()
      .to(givenInstant)
      .withCounter(MetricSnapshotBuilder.counter("foo", TagSet.Empty, 2))
      .build()

    val convertedDatums = datums(snapshot, TagSet.Empty)
    convertedDatums.size shouldBe 1
    convertedDatums(0).timestamp() shouldBe Date.from(givenInstant).toInstant
  }

  it must "populate percentages" in {
    val snapshot = PeriodSnapshotBuilder()
      .withCounter(
        MetricSnapshotBuilder
          .counter("foo", "", TagSet.Empty, MeasurementUnit.percentage, 39)
      )
      .build()

    val convertedDatums = datums(snapshot, TagSet.Empty)
    convertedDatums.size shouldBe 1
    convertedDatums(0).unit shouldBe StandardUnit.PERCENT
  }

  it must "attach user tags" in {
    val snapshot = PeriodSnapshotBuilder()
      .withCounter(
        MetricSnapshotBuilder
          .counter("bar", TagSet.from(Map("tag" -> "tagValue")), 10)
      )
      .build()

    val expectedDimensions = List(
      Dimension.builder().name("tag").value("tagValue").build()
    )

    val convertedDatums = datums(snapshot, TagSet.Empty)
    val dimensions =
      convertedDatums.map(_.dimensions().asScala).reduceRight(_ ++ _).toList

    dimensions shouldBe expectedDimensions
  }

  it must "attach base (environment) tags" in {
    val snapshot = PeriodSnapshotBuilder()
      .withCounter(MetricSnapshotBuilder.counter("foo", TagSet.Empty, 10))
      .build()

    val expectedDimensions = List(
      Dimension.builder().name("quxx").value("bar").build()
    )

    val convertedDatums = datums(snapshot, TagSet.from(Map("quxx" -> "bar")))
    val dimensions =
      convertedDatums.map(_.dimensions().asScala).reduceRight(_ ++ _).toList

    dimensions shouldBe expectedDimensions
  }

}
