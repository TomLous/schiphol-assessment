package xyz.graphiq.schiphol.analytics.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import scala.concurrent.duration._

case class StreamingJobConfig(
                               inputFile: String,
                               schemaFile: String,
                               outputPath: String,
                               checkpointLocation: String,
                               rowsPerSecond: Int,
                               windowLength: Duration,
                               slidingLength: Duration
                             )


object StreamingJobConfig {

  def apply(): StreamingJobConfig =
    ConfigSource.default.at("streaming-job").loadOrThrow[StreamingJobConfig]

}
