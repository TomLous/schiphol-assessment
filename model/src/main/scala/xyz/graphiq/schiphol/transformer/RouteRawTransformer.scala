package xyz.graphiq.schiphol.transformer

import org.apache.spark.sql.catalyst.expressions.objects.AssertNotNull
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.{Column, DataFrame, Dataset, Encoders, SparkSession}
import xyz.graphiq.schiphol.model._

// Transformer to map from a raw route dataframe to a typed Route
case class RouteRawTransformer(implicit spark: SparkSession) extends Function[DataFrame, Dataset[Route]] {

  import spark.implicits._

  private val nullIntTransformer: Column => Column = c => when(c === lit("""\N"""), null).otherwise(c).cast(IntegerType)
  private val notNullTransformer: Column => Column = c => new Column(AssertNotNull(c.expr))

  private val equipmentUdf: UserDefinedFunction = udf(
    (equipmentList: Seq[String]) =>
      if (equipmentList == null) {
        Seq.empty[Equipment]
      } else {
        equipmentList.map(Equipment)
      }
  )

  override def apply(v1: DataFrame): Dataset[Route] = {

    v1
      .withColumn("airline",
        struct(nullIntTransformer('airlineID).as("id"), upper(trim(notNullTransformer('airlineCode))).as("code"))
      )
      .withColumn("sourceAirport",
        struct(nullIntTransformer('sourceAirportID).as("id"), upper(trim(notNullTransformer('sourceAirport))).as("code"))
      )
      .withColumn("destinationAirport",
        struct(nullIntTransformer('destinationAirportID).as("id"), upper(trim(notNullTransformer('destinationAirport))).as("code"))
      )
      .withColumn("codeShare", when(upper('codeShare) === "Y", true).otherwise(false))
      .withColumn("stops", notNullTransformer('stops).cast(IntegerType))
      .withColumn("equipment", equipmentUdf(split(upper(trim('equipment)), " ")))
      .drop("airlineID", "airlineCode", "sourceAirportID", "destinationAirportID")
      .as[Route]
  }
}
