package xyz.graphiq.schiphol.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SchemaLoaderTest extends AnyFlatSpec with Matchers {

  "SchemaLoader" should "read avsc correctly" in {

    val schemaEither = SchemaLoader.fromAvscResource(s"SchemaLoaderTest/correct.avsc")

    assert(schemaEither.isRight)
  }

  it should "not parse an incorrect avsc " in {

    val schemaEither = SchemaLoader.fromAvscResource(s"SchemaLoaderTest/incorrect.avsc")

    assert(schemaEither.isLeft)
  }

}
