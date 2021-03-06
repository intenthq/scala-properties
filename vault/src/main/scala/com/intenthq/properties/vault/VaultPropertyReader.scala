package com.intenthq.properties.vault

import com.intenthq.properties.{PropertyReader, StringConversion}
import janstenpickle.vault.core.{Secrets, VaultConfig}
import uscala.result.Result
import uscala.result.Result.{Fail, Ok}

import scala.concurrent.ExecutionContext.Implicits.global

trait Vault {
  def config: VaultConfig
  def backend: String
  lazy val secrets: Secrets = Secrets(config, backend)
}

trait VaultSecrets extends Vault {
  override val backend: String = "secret"
}

trait VaultCubbyHole extends Vault {
  override val backend: String = "cubbyhole"
}

trait VaultPropertyReader extends Vault with PropertyReader {

  override def getAsString(name: String): Option[String] = secrets.get(name).attemptRun(_.getMessage).toOption

  override def getAsStringRequired(name: String): Result[String, String] = secrets.get(name).attemptRun(_.getMessage)

  override def get[T](name: String)(implicit conversion: StringConversion[T]): Result[String, Option[T]] =
    secrets.get(name).
      attemptRun.
      fold[Result[String, Option[T]]](ex => Fail(ex.getMessage),
                                      _.toOption.map(conversion.parse).fold[Result[String, Option[T]]](Ok(None))(_.map(Some(_))))

}

trait VaultNestedPropertyReader extends Vault with PropertyReader {
  def key: String

  override def getAsString(name: String): Option[String] = secrets.get(key, name).attemptRun(_.getMessage).toOption

  override def getAsStringRequired(name: String): Result[String, String] = secrets.get(key, name).attemptRun(_.getMessage)

  override def get[T](name: String)(implicit conversion: StringConversion[T]): Result[String, Option[T]] =
    secrets.get(key, name).
      attemptRun.
      fold[Result[String, Option[T]]](ex => Fail(ex.getMessage),
                                      _.toOption.map(conversion.parse).fold[Result[String, Option[T]]](Ok(None))(_.map(Some(_))))
}

class VaultSecretsPropertyReader(override val config: VaultConfig) extends VaultPropertyReader with VaultSecrets
class VaultSecretsNestedPropertyReader(override val config: VaultConfig,
                                       override val key: String) extends VaultNestedPropertyReader with VaultSecrets

class VaultCubbyHolePropertyReader(override val config: VaultConfig) extends VaultPropertyReader with VaultCubbyHole
class VaultCubbyHoleNestedPropertyReader(override val config: VaultConfig,
                                         override val key: String) extends VaultNestedPropertyReader with VaultCubbyHole
