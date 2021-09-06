package xyz.graphiq.schiphol.analytics.jobs

import org.apache.spark.sql.catalyst.plans.logical.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger.ProcessingTime
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode}
import xyz.graphiq.schiphol.analytics.config.{BatchJobConfig, StreamingJobConfig}
import xyz.graphiq.schiphol.analytics.model.TemporalRoute
import xyz.graphiq.schiphol.analytics.transformer.TopAirportsTransformer
import xyz.graphiq.schiphol.model.{Route, TopAirports, TopTemporalAirports}
import xyz.graphiq.schiphol.reader.RouteRawReader
import xyz.graphiq.schiphol.transformer.RouteRawTransformer
import xyz.graphiq.schiphol.util.{SchemaLoader, SparkJob}

import scala.concurrent.duration._

object StreamingJob extends App with SparkJob {

  import spark.implicits._

  // For advanced logging add: -DLogLevel=DEBUG (or INFO / ERROR / WARNING)
  logger.debug("Run start")

  // To specify the correct config use -Dconfig.resource=application.???.conf
  val config = StreamingJobConfig()

  logger.info(s"Reading schema from resources: ${config.schemaFile}")

  // Get the raw schema, just assume resources is the only place to load them from right now
  val rawSchema = SchemaLoader.fromAvscResource(config.schemaFile) match {
    case Right(schema) => schema
    case Left(t) => logger.error(t.getMessage); null // Nulls are not pretty, but this is correctly handled in the API @see DataFrameReader#schema
  }

  logger.info(s"Reading csv from file: ${config.inputFile}")


  // Streaming only works with timestamps, since this dataset doesn't have any we'll fabricate them
  val routesNumbered:DataFrame = RouteRawReader
    .readCsv(spark.read)
    .schema(rawSchema)
    .load(config.inputFile)
    .transform(RouteRawTransformer())
    .rdd
    .zipWithIndex()
    .toDF("route", "value")

  // create a rate limiter to micro batch the csv records based on nr/rows per second
  val rateStream = spark.readStream
    .format("rate")
    .option("rowsPerSecond", config.rowsPerSecond)
    .load()

  val routesStream = rateStream
    .join(broadcast(routesNumbered), "value")
    .drop("value")
    .withWatermark("timestamp", config.slidingLength.toString)
    .withColumn("window", window('timestamp, config.windowLength.toString, config.slidingLength.toString))
    .as[TemporalRoute]
    .groupByKey(_.window)
    .mapGroups((window, temporalRoutes) =>
      TopTemporalAirports(
        window.start,
        window.end,
        TopAirportsTransformer
          .windowedAggregator(temporalRoutes.map(_.route), 10, TopAirportsTransformer.Source))
    )

  logger.debug("Start writing streams to output")

  routesStream
    .repartition(1) // <- For easier inspection (less files)
    .writeStream
    .option("checkpointLocation", config.checkpointLocation)
    .format("parquet")
    .trigger(ProcessingTime(10.seconds))
    .option("path", config.outputPath)
    .outputMode("append")
    .start()
    .awaitTermination()



}
