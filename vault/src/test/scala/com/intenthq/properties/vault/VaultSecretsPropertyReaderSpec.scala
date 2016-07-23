package com.intenthq.properties.vault

import java.net.URL

import com.intenthq.properties.{PropertyReader, PropertyReaderSpec}
import janstenpickle.vault.core.{Secrets, VaultConfig, WSClient}
import uscala.concurrent.result.AsyncResult

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait VaultPropertyReaderSpec extends PropertyReaderSpec {
  val config = VaultConfig(WSClient(new URL("http://localhost:8200")), "test")
  override def missingError(key: String): String = "Not found"

  def stubSecrets(map: Map[String, String]) = new Secrets(config, "secret") {
    override def get(key: String, subKey: String = "value")
                    (implicit ec: ExecutionContext): AsyncResult[String, String] =
      map.get(key).fold[AsyncResult[String, String]](AsyncResult.fail("Not found"))(AsyncResult.ok)
  }
}

trait VaultNestedPropertyReaderSpec extends VaultPropertyReaderSpec {
  override def stubSecrets(map: Map[String, String]) = new Secrets(config, "secret") {
    override def get(key: String, subKey: String = "value")
                    (implicit ec: ExecutionContext): AsyncResult[String, String] =
      map.get(subKey).fold[AsyncResult[String, String]](AsyncResult.fail("Not found"))(AsyncResult.ok)
  }
}

class VaultSecretsPropertyReaderSpec extends VaultPropertyReaderSpec {
  override def initReader: (Map[String, String]) => PropertyReader = map =>
    new VaultSecretsPropertyReader(config) {
      override lazy val secrets = stubSecrets(map)
    }
}

class VaultCubbyHolePropertyReaderSpec extends VaultPropertyReaderSpec {
  override def initReader: (Map[String, String]) => PropertyReader = map =>
    new VaultCubbyHolePropertyReader(config) {
      override lazy val secrets = stubSecrets(map)
    }
}

class VaultSecretsNestedPropertyReaderSpec extends VaultNestedPropertyReaderSpec {
  override def initReader: (Map[String, String]) => PropertyReader = map =>
    new VaultSecretsNestedPropertyReader(config, "test") {
      override lazy val secrets = stubSecrets(map)
    }
}

class VaultCubbyHoleNestedPropertyReaderSpec extends VaultNestedPropertyReaderSpec {
  override def initReader: (Map[String, String]) => PropertyReader = map =>
    new VaultCubbyHoleNestedPropertyReader(config, "test") {
      override lazy val secrets = stubSecrets(map)
    }
}
