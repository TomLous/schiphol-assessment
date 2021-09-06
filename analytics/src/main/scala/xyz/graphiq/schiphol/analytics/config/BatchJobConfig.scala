package xyz.graphiq.schiphol.analytics.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._

case class BatchJobConfig(
                           inputFile: String,
                           schemaFile: String,
                           outputPath: String
                         )

object BatchJobConfig {

  def apply(): BatchJobConfig =
    ConfigSource.default.at("batch-job").loadOrThrow[BatchJobConfig]

}
