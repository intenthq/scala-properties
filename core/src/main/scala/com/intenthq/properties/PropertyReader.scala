package com.intenthq.properties

import java.util.Properties

import uscala.result.Result
import uscala.result.Result.{Fail, Ok}

trait PropertyReader {
  /**
    * Returns the value of the key with the given name as a string. Returns `None` if the key was not set, otherwise
    * returns `Some(value)`.
    */
  def getAsString(name: String): Option[String]

  /**
    * Returns the value of the key with the given name as a string. Returns `Result.Fail(err)` if the key was not set,
    * otherwise returns `Result.Ok(value)`.
    */
  def getAsStringRequired(name: String): Result[String, String] =
    Result.fromOption(getAsString(name), PropertyReader.MissingValueFormat.format(name))

  /**
    * Returns the value of the key with the given name, applying any conversions needed.
    *
    * Will return a failure only if the property had a value and converting it failed, otherwise an `Ok(None)`. This
    * allows you to show errors if a value is set incorrectly, otherwise provide a default value.
    */
  def get[T](name: String)(implicit conversion: StringConversion[T]): Result[String, Option[T]] =
    getAsString(name).map(conversion.parse).fold[Result[String, Option[T]]](Ok(None))(_.map(Some(_)))

  /**
    * Returns the value of the key with the given name, applying any conversions needed.
    *
    * This returns a failure if the property wasn't set, and if a property was set but it couldn't be converted. This
    * allows you to show errors if a value is set incorrectly or if it was never set at all. Useful when you require
    * a property to always be set.
    */
  def getRequired[T](name: String)(implicit conversion: StringConversion[T]): Result[String, T] =
    getAsStringRequired(name).flatMap(conversion.parse)
}

object PropertyReader {
  val MissingValueFormat = "'%s' is missing from the configuration"
}

class MapPropertyReader(val props: Map[String, String]) extends PropertyReader {
  override def getAsString(name: String): Option[String] = props.get(name)
}

class SystemPropertyReader(val props: Properties = System.getProperties) extends PropertyReader {
  override def getAsString(name: String): Option[String] = Option(props.getProperty(name)).filterNot(_ == "")
}

class CombinedReader (val readers: Set[PropertyReader]) extends PropertyReader {
  def collectErrors[T](results: Seq[(String, Result[String, T])]): Result[String, T] =
    results.find(_._2.isInstanceOf[Ok[_]]).fold[Result[String, T]](
      Fail(results.filter(_._2.isInstanceOf[Fail[_]]).foldLeft("")((acc, v) =>
        v._2.fold(err => s"$acc${v._1}: $err\n", _ => acc)
      ).trim)
    )(_._2)

  override def getAsString(name: String): Option[String] = readers.flatMap(reader => reader.getAsString(name)).headOption

  override def getAsStringRequired(name: String): Result[String, String] =
    collectErrors(readers.toSeq.map(r => (r.getClass.getSimpleName, r.getAsStringRequired(name))))

  override def get[T](name: String)(implicit conversion: StringConversion[T]): Result[String, Option[T]] = {
    val results = readers.toSeq.map(r => (r.getClass.getSimpleName, r.get(name)))
    val filtered =
      if (results.exists(_._2.toEither.isLeft)) results.filter(_._2.toEither.isLeft) // if there are any errors then make sure there are no results so the user can see the errors
      else if (results.exists(_._2.toOption.flatten.isDefined)) results.filter(_._2.toOption.flatten.isDefined) // if there are any valid results then filter out the `None`s
      else results // otherwise just return the results

    collectErrors(filtered)
  }
}

object CombinedReader {
  def apply(readers: PropertyReader*): CombinedReader = new CombinedReader(readers.toSet)
}
