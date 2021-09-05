package xyz.graphiq.schiphol.transformer

import org.apache.spark.SparkException
import org.apache.spark.sql.DataFrame
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import xyz.graphiq.schiphol.util.SparkTestJob

import scala.util.{Failure, Success, Try}

class RouteRawTransformerTest extends AnyFlatSpec with Matchers with SparkTestJob{

  private def readCsvToDF(fileName:String):DataFrame =
    spark
      .read
      .option("header", "false")
      .option("delimiter", ",")
      .option("inferSchema", "true")
      .option("treatEmptyValuesAsNulls", "true")
      .option("nullValue", """\N""")
      .csv(s"$testDataPath/$fileName")
      .toDF("airlineCode", "airlineID", "sourceAirport", "sourceAirportID", "destinationAirport", "destinationAirportID", "codeShare", "stops", "equipment")


  "RouteRawTransformer" should "transform a correct csv" in {

    val df = readCsvToDF("routes-test1.dat").transform(RouteRawTransformer())
    val routes = df.collect().toList

    assert(routes.length === 7)
  }

  "RouteRawTransformer" should "fail an incorrect csv" in {

    val df = readCsvToDF("routes-test2.dat").transform(RouteRawTransformer())
    assertThrows[SparkException](df.collect())
  }



}
