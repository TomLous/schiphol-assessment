package xyz.graphiq.schiphol.model

import java.sql.Timestamp

case class TopTemporalAirports(
                                windowStart: Timestamp,
                                windowEnd: Timestamp,
                                topAirports: List[TopAirports]
                              )
