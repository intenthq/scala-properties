package com.intenthq.properties.apache

import java.net.URI

import com.intenthq.properties.{PropertyReader, SystemPropertyReader}
import org.apache.commons.configuration.{AbstractConfiguration, PropertiesConfiguration}
import uscala.result.Result

class ApachePropertyReader(config: AbstractConfiguration) extends PropertyReader {
  override def getAsString(name: String): Option[String] = Option(config.getString(name))
}

object PropertyFileReader {
  val PropertyFile = "property.file"

  def apply(configUrl: Option[String]): Result[String, ApachePropertyReader] =
    for {
      urlString <- configUrl.fold(new SystemPropertyReader().getAsStringRequired(PropertyFile))(Result.ok)
      url <- Result.attempt(URI.create(urlString).toURL).leftMap(_.getMessage)
    } yield new ApachePropertyReader(new PropertiesConfiguration(url))
}
