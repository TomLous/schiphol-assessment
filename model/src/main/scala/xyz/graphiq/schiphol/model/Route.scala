package xyz.graphiq.schiphol.model

/**
 * @see route-raw.avsc
 */
case class Route(
                  airline: Airline,
                  sourceAirport: Airport,
                  destinationAirport: Airport,
                  codeShare: Boolean,
                  stops: Int,
                  equipment: List[Equipment]
                )


