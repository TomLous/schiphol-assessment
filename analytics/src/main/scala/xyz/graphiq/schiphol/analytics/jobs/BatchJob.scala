package xyz.graphiq.schiphol.analytics.jobs

import org.apache.spark.sql.{Dataset, SaveMode}
import xyz.graphiq.schiphol.analytics.config.BatchJobConfig
import xyz.graphiq.schiphol.analytics.transformer.TopAirportsTransformer
import xyz.graphiq.schiphol.analytics.util.{SchemaLoader, SparkJob}
import xyz.graphiq.schiphol.model.Route
import xyz.graphiq.schiphol.transformer.RouteRawTransformer


object BatchJob extends App with SparkJob{

  // For advanced logging add: -DLogLevel=DEBUG (or INFO / ERROR / WARNING)
  logger.debug("Run start")

  // To specify the correct config use -Dconfig.resource=application.???.conf
  val config = BatchJobConfig()

  logger.info(s"Reading schema from resources: ${config.schemaFile}")

  // Get the raw schema, just assume resources is the only place to load them from right now
  val rawSchema = SchemaLoader.fromAvscResource(config.schemaFile) match {
    case Right(schema) => schema
    case Left(t) => logger.error(t.getMessage); null // Nulls are not pretty, but this is correctly handled in the API @see DataFrameReader#schema
  }

  logger.info(s"Reading csv from file: ${config.inputFile}")

  val routes: Dataset[Route] = spark
    .read
    .format("csv")
    .schema(rawSchema)
    .load(config.inputFile)
    .transform(RouteRawTransformer())

  val top10sources = routes.transform(
    TopAirportsTransformer(10, TopAirportsTransformer.Source)
  )

  top10sources
    .repartition(1)
    .write
    .format("csv")
    .mode(SaveMode.Overwrite)
    .save(config.outputPath)

  logger.info(s"Wrote csv to: ${config.outputPath}")

  logger.debug("Run end")
}
