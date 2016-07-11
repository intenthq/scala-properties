package com.intenthq.properties

import org.scalacheck.{Gen, Prop}
import org.specs2.matcher.MatchResult
import org.specs2.{ScalaCheck, Specification}
import uscala.result.Result
import uscala.result.specs2.ResultMatchers

trait PropertyReaderSpec extends Specification with ScalaCheck with ResultMatchers {
  import PropertyReaderSpec._
  import stringconversions._

  val identityMatch: MatchResult[Any] = true must beTrue

  override def is =
    s2"""
        Can read a string value which is present $canReadString
        Cannot read a string value which is not present $cannotReadString

        Can format a valid Long ${canFormat[Long](Gen.posNum[Long].map(_.toString), _.toLong)}
        Can format a valid Int ${canFormat[Int](Gen.posNum[Int].map(_.toString), _.toInt)}
        Can format a valid Double ${canFormat[Double](Gen.posNum[Double].map(_.toString), _.toDouble)}
        Can format a valid Boolean ${canFormat[Boolean](Gen.oneOf(true, false).map(_.toString), _.toBoolean)}
        Can format a valid String ${canFormat[String](stringGen, identity)}

        Cannot format an invalid Long ${cannotFormat[Long]}
        Cannot format an invalid Int ${cannotFormat[Int]}
        Cannot format an invalid Double ${cannotFormat[Double]}
        Cannot format an invalid Boolean ${cannotFormat[Boolean]}

        Does not find a non-existent Long ${notPresent[Long]}
        Does not find a non-existent Int ${notPresent[Long]}
        Does not find a non-existent Boolean ${notPresent[Boolean]}
        Does not find a non-existent Double ${notPresent[Double]}
        Does not find a non-existent String ${notPresent[String]}
      """

  def initReader: Map[String, String] => PropertyReader
  def missingError(key: String): String = PropertyReader.MissingValueFormat.format(key)

  def canReadString = Prop.forAllNoShrink(mapGen()) { props =>
    val reader = initReader(props)
    props.keys.foldLeft[MatchResult[Any]](identityMatch)((acc, key) =>
      acc and
      (reader.readSafe(key) must beSome.like{ case v => v === props(key) }) and
      (reader.orError(key) must beOk.like{ case v => v === props(key) })
    )
  }

  def cannotReadString = Prop.forAllNoShrink(mapGen(), listGen()) { (props, keys) =>
    val badKeys = keys.diff(props.keys.toSeq)
    val reader = initReader(props)

    badKeys.foldLeft[MatchResult[Any]](identityMatch)((acc, key) =>
      acc and
      (reader.readSafe(key) must beNone) and
      (reader.orError(key) must beFail.like{ case err => err === missingError(key) })
    )
  }

  def canFormat[T](gen: Gen[String], convertValue: String => T)(implicit convert: String => Result[String, T]) =
    Prop.forAllNoShrink(mapGen(gen)) { props =>
      val reader = initReader(props)

      props.keys.foldLeft[MatchResult[Any]](identityMatch)((acc, key) =>
        acc and
        (reader.required[T](key) must beOk.like{ case v => v === convertValue(props(key)) }) and
        (reader.optional[T](key) must beOk.like{ case v => v must beSome.like { case a => a === convertValue(props(key)) } })
      )
    }

  def cannotFormat[T](implicit convert: String => Result[String, T]) =
    Prop.forAllNoShrink(mapGen()) { props =>
      val reader = initReader(props)

      props.keys.foldLeft[MatchResult[Any]](identityMatch)((acc, key) =>
        acc and
        (reader.required[T](key) must beFail) and
        (reader.optional[T](key) must beFail)
      )
    }

  def notPresent[T](implicit convert: String => Result[String, T]) =
    Prop.forAllNoShrink(mapGen(), listGen()) { (props, keys) =>
      val badKeys = keys.diff(props.keys.toSeq)
      val reader = initReader(props)

      badKeys.foldLeft[MatchResult[Any]](identityMatch)((acc, key) =>
        acc and
        (reader.optional[T](key) must beOk[Option[T]].like { case v => v must beNone })
      )
    }
}

object PropertyReaderSpec {
  val stringGen = Gen.alphaStr.suchThat(_.nonEmpty)
  def listGen(gen: Gen[String] = stringGen) = Gen.listOf(gen).suchThat(_.nonEmpty)
  def mapGen(valuesGen: Gen[String] = stringGen): Gen[Map[String, String]] = for {
    keys <- listGen()
    values <- listGen(valuesGen)
  } yield keys.zip(values).toMap
}
