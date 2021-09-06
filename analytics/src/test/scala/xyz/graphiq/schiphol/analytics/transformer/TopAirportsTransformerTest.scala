package xyz.graphiq.schiphol.analytics.transformer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import xyz.graphiq.schiphol.model._
import xyz.graphiq.schiphol.util.SparkTestJob

class TopAirportsTransformerTest extends AnyFlatSpec with Matchers with SparkTestJob{

  import spark.implicits._

  private val airline = Airline(None, "A")
  private val airports = List(
    Airport(None, "A"),
    Airport(Some(1), "A"),
    Airport(None, "B"),
    Airport(Some(2), "B"),
    Airport(None, "C"),
    Airport(Some(2), "C")
  )

  private val routes = List(
    Route(airline = airline, sourceAirport = airports(0), destinationAirport = airports(0), codeShare = false, stops = 0, equipment = Nil), // A -> A
    Route(airline = airline, sourceAirport = airports(1), destinationAirport = airports(1), codeShare = false, stops = 0, equipment = Nil), // A -> A
    Route(airline = airline, sourceAirport = airports(2), destinationAirport = airports(0), codeShare = false, stops = 0, equipment = Nil), // B -> A
    Route(airline = airline, sourceAirport = airports(3), destinationAirport = airports(1), codeShare = false, stops = 0, equipment = Nil), // B -> A
    Route(airline = airline, sourceAirport = airports(4), destinationAirport = airports(0), codeShare = false, stops = 0, equipment = Nil), // C -> A
    Route(airline = airline, sourceAirport = airports(5), destinationAirport = airports(1), codeShare = false, stops = 0, equipment = Nil), // C -> A
    Route(airline = airline, sourceAirport = airports(2), destinationAirport = airports(3), codeShare = false, stops = 0, equipment = Nil), // B -> B
    Route(airline = airline, sourceAirport = airports(3), destinationAirport = airports(2), codeShare = false, stops = 0, equipment = Nil), // B -> B
    Route(airline = airline, sourceAirport = airports(4), destinationAirport = airports(3), codeShare = false, stops = 0, equipment = Nil), // C -> B
    Route(airline = airline, sourceAirport = airports(5), destinationAirport = airports(2), codeShare = false, stops = 0, equipment = Nil), // C -> B
    Route(airline = airline, sourceAirport = airports(5), destinationAirport = airports(0), codeShare = false, stops = 0, equipment = Nil), // C -> A
    Route(airline = airline, sourceAirport = airports(0), destinationAirport = airports(5), codeShare = false, stops = 0, equipment = Nil), // A -> C
  )

  // Total source A: 3, B: 4, C: 5
  // Total dest A: 7, B: 4, C: 1

  "TopAirportsTransformer" should "create a correct top 3 from Source routes using Dataset Transformers" in {

    val top3SourcesCalc = routes.toDS()
      .transform(TopAirportsTransformer(3, TopAirportsTransformer.Source))
      .collect()
      .toList

    val top3SourceComp = List(
      TopAirports("C", 5),
      TopAirports("B", 4),
      TopAirports("A", 3)
    )

    top3SourcesCalc should equal(top3SourceComp)

  }

  it should "create a correct top 3 from Destination routes using Dataset Transformers" in {

    val top3DestCalc = routes.toDS()
      .transform(TopAirportsTransformer(3, TopAirportsTransformer.Destination))
      .collect()
      .toList

    val top3DestinationComp = List(
      TopAirports("A", 7),
      TopAirports("B", 4),
      TopAirports("C", 1)
    )

    top3DestCalc should equal(top3DestinationComp)

  }

  it should "create a correct top 3 from Source routes using Window Iterators" in {
    val top3SourcesCalc = TopAirportsTransformer.windowedAggregator(routes.toIterator, 3, TopAirportsTransformer.Source)

    val top3SourceComp = List(
      TopAirports("C", 5),
      TopAirports("B", 4),
      TopAirports("A", 3)
    )

    top3SourcesCalc should equal(top3SourceComp)
  }



}
