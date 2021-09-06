package xyz.graphiq.schiphol.reader

import org.apache.spark.sql.DataFrameReader
import org.apache.spark.sql.streaming.DataStreamReader


object  RouteRawReader {

  def readCsv(dfr:DataFrameReader) : DataFrameReader =
    dfr
      .format("csv")
      .option("header", "false")
      .option("delimiter", ",")
      .option("inferSchema", "true")
      .option("treatEmptyValuesAsNulls", "true")
      .option("nullValue", """\N""")

  def readCsv(dsr:DataStreamReader) : DataStreamReader =
    dsr
      .format("csv")
      .option("header", "false")
      .option("delimiter", ",")
      .option("inferSchema", "true")
      .option("treatEmptyValuesAsNulls", "true")
      .option("nullValue", """\N""")
}
