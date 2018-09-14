package kamon.cloudwatch

import java.time.Instant

import com.amazonaws.ClientConfiguration
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest
import kamon.Kamon
import kamon.metric.{MetricDistribution, MetricValue, MetricsSnapshot, PeriodSnapshot}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class CloudWatchAPIReporterSpec extends FlatSpecLike with Matchers with MockitoSugar {

  trait TestFixture {
    val cwReporter = new CloudWatchAPIReporter(other = new ClientConfiguration(), f = _ => None)
    val cloudWatch = mock[AmazonCloudWatch]

  }

  it should "send metrics with dimensions to cloudwatch" in new TestFixture {

    Kamon.addReporter(cwReporter)

    val metricName = "test_metric"
    val metricValue = 5
    val clock = new kamon.util.Clock.Default()

    var lastInstant = Instant.now(clock)
    val currentInstant = Instant.now(clock)
    val metric = MetricDistribution(metricName, None, )
    val metricsSnapshot = MetricsSnapshot(Seq(MetricDistribution)
      histograms: ,
      rangeSamplers: Seq[MetricDistribution],
      gauges: Seq[MetricValue],
      counters: Seq[MetricValue]
    )
    val periodSnapshot = PeriodSnapshot(
      from = lastInstant,
      to = currentInstant,
      metrics = Seq(MetricDistribution)
    )

    Kamon.histogram(metricName).record(metricValue)
    cwReporter.reportPeriodSnapshot(periodSnapshot)
    val captor: ArgumentCaptor[PutMetricDataRequest] = ArgumentCaptor.forClass(classOf[PutMetricDataRequest])
    verify(cloudWatch, timeout(5000)).putMetricData(captor.capture())
    captor.getValue.getMetricData.get(0).getMetricName shouldEqual metricName
    captor.getValue.getMetricData.get(0).getValue shouldEqual metricValue
    captor.getValue.getMetricData.get(0).getDimensions.get(0).getName shouldEqual "service"
    captor.getValue.getMetricData.get(0).getDimensions.get(0).getValue shouldEqual Kamon.environment.service
  }
}