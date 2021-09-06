package xyz.graphiq.schiphol.analytics.transformer

import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import xyz.graphiq.schiphol.model.{Route, TopAirports}

import scala.collection.immutable.HashMap

object TopAirportsTransformer {
  trait TopType {
    def field: Route => String
  }

  case object Source extends TopType {
    override val field: Route => String = _.sourceAirport.code
  }

  case object Destination extends TopType {
    override val field: Route => String = _.destinationAirport.code
  }

  def windowedAggregator(routes: Iterator[Route], top: Int = 10, topType: TopAirportsTransformer.TopType):List[TopAirports] = {
    routes
      .toStream
      .map(route => topType match {
        case Source => (route.sourceAirport.code, 1)
        case Destination => (route.destinationAirport.code, 1)
      })
      .groupBy(_._1)
      .mapValues(_.length)
      .map{
        case (code, count) => TopAirports(code, count)
      }
      .toList
      .sortBy(- _.count)
      .take(top)
  }
}


case class TopAirportsTransformer(top: Int = 10, topType: TopAirportsTransformer.TopType)(implicit spark: SparkSession) extends Function[Dataset[Route], Dataset[TopAirports]] {

  import spark.implicits._

  override def apply(v1: Dataset[Route]):  Dataset[TopAirports] = {
    v1
      .map(topType.field)
      .withColumnRenamed("value", "airportCode")
      .groupBy("airportCode")
      .count()
      .orderBy('count.desc)
      .limit(top)
      .as[TopAirports]
  }
}

