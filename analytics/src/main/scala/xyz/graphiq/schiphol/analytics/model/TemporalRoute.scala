package xyz.graphiq.schiphol.analytics.model


import xyz.graphiq.schiphol.model.Route

import java.sql.Timestamp

case class TemporalRoute(
                          timestamp: Timestamp,
                          window: TemporalRoute.Window,
                          route: Route
                        )

object TemporalRoute{
  case class Window(start: Timestamp, end: Timestamp)
}