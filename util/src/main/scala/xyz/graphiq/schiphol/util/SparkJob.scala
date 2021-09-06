package xyz.graphiq.schiphol.util

import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

trait SparkJob {

  // Configuration may be overridden. Override this function before using spark val
  def sparkConf: SparkConf = {
    new SparkConf()
      .setIfMissing("spark.master", "local[*]")
      .setIfMissing("spark.sql.streaming.forceDeleteTempCheckpointLocation","true")
  }

  lazy val appName: String = this.getClass.getSimpleName.replace("$", "")
  lazy val className: String = getClass.getName.replace("$", "")

  // The implicit SparkSession available for all Spark Jobs
  implicit lazy val spark: SparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .appName(appName)
    .getOrCreate()

  // define logger
  @transient implicit lazy val logger: Logger = org.apache.log4j.LogManager.getLogger(className)
}
