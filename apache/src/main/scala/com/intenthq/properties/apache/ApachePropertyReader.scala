package com.intenthq.properties.apache

import java.net.URI

import com.intenthq.properties.{PropertyReader, SystemPropertyReader}
import org.apache.commons.configuration.{AbstractConfiguration, PropertiesConfiguration, SystemConfiguration}
import uscala.result.Result

class ApachePropertyReader(config: AbstractConfiguration) extends PropertyReader {
  override def readSafe(name: String): Option[String] = Option(config.getString(name))
}

object PropertiesFileReader {
  val PropertyFile = "property.file"

  def apply(configUrl: Option[String]): Result[String, ApachePropertyReader] =
    for {
      urlString <- configUrl.fold(new SystemPropertyReader().orError(PropertyFile))(Result.ok)
      url <- Result.attempt(URI.create(urlString).toURL).leftMap(_.getMessage)
    } yield new ApachePropertyReader(new PropertiesConfiguration(url))
}
