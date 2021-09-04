package xyz.graphiq.schiphol.analytics.util


import org.apache.avro.{Schema => AvroSchema}
import org.apache.spark.sql.avro.SchemaConverters
import org.apache.spark.sql.types.StructType

import scala.io.Source
import scala.util.Try

object SchemaLoader {

  def fromAvscResource(avscFile: String): Either[Throwable, StructType] =
    for {
      file <- Try(Source.fromResource(avscFile).mkString).toEither
      avroSchema <- Try(new AvroSchema.Parser().parse(file)).toEither
      schema <- SchemaConverters.toSqlType(avroSchema).dataType match {
        case t: StructType => Right(t)
        case _ => Left(new Exception(s"Avro schema cannot be converted to a Spark SQL StructType: ${avroSchema.toString(true)}"))
      }
    } yield schema
}
