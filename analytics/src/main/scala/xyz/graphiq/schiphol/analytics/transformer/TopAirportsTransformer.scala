package xyz.graphiq.schiphol.analytics.transformer

import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import xyz.graphiq.schiphol.model.{Route, TopAirports}

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

