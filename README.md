# Properties Reader Scala Library
[![Build Status](https://travis-ci.org/intenthq/scala-properties.svg?branch=master)](https://travis-ci.org/intenthq/scala-properties)

Scala library for working with configuration sources.

This library has a number of modules:

|Name|Description|Download|
|---|---|---|
|**Core** | Provides `PropertyReader` trait, string conversions and basic property readers for extracting configuration from System Properties or a simple `Map[String, String]` | [ ![Download](https://api.bintray.com/packages/janstenpickle/maven/scala-properties-core/images/download.svg) ](https://bintray.com/janstenpickle/maven/scala-properties-core/_latestVersion) |
| **Apache** | Provides a property reader instance for reading from [Apache Commons Configuration](https://commons.apache.org/proper/commons-configuration/) sources | [ ![Download](https://api.bintray.com/packages/janstenpickle/maven/scala-properties-apache/images/download.svg) ](https://bintray.com/janstenpickle/maven/scala-properties-apache/_latestVersion) |
| **Hadoop** | Provides a property reader instance for reading from Hadoop configuration sources | [ ![Download](https://api.bintray.com/packages/janstenpickle/maven/scala-properties-hadoop/images/download.svg) ](https://bintray.com/janstenpickle/maven/scala-properties-hadoop/_latestVersion) |
| **Vault** | Provides property reader instances for reading from [Hashicorp Vault](http://vaultproject.io) | [ ![Download](https://api.bintray.com/packages/janstenpickle/maven/scala-properties-vault/images/download.svg) ](https://bintray.com/janstenpickle/maven/scala-properties-vault/_latestVersion) |

## Install with SBT
Add the following to your sbt `project/plugins.sbt` file:
```scala
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
```
Then add the following to your `build.sbt`
```scala
resolvers += Resolver.bintrayRepo("janstenpickle", "maven")
libraryDependencies += "com.intenthq.properties" %% "scala-properties-core" % "0.1.2"
libraryDependencies += "com.intenthq.properties" %% "scala-properties-apache" % "0.1.2"
libraryDependencies += "com.intenthq.properties" %% "scala-properties-hadoop" % "0.1.2"
libraryDependencies += "com.intenthq.properties" %% "scala-properties-vault" % "0.1.2"
```

## Usage

The property reader extensively uses functionality of the uscala `Result` type. More information on this can be found [here](https://github.com/albertpastrana/uscala).

```scala
import com.intenthq.properties.MapPropertyReader
import com.intenthq.properties.stringconversions._ // built in implicit string conversions (String => Result[String, T])

val reader = new MapPropertyReader(Map("key" -> "value"))

reader.readSafe("key") // returns Option[String]

reader.orError("key") // returns Result[String, String]

reader.required[Int]("key") // returns Result[String, Int], failing if the property is missing or parsing to the supplied type failed

reader.optional[Int]("key") // returns Result[String, Option[Int]], failing if parsing to the supplied type failed and `Ok[None]` if the property was missing
```

## Provided Implementations

|Name|Module|Description|
|---|---|---|
|**MapPropertyReader**| scala-properties-core | Reads properties from `Map[String, String]` |
|**SystemPropertyReader** | scala-properties-core | Reads from Java System Properties, can also be provided with a `java.util.Properties` to read from |
|**CombinedPropertyReader** | scala-properties-core | Allows multiple property readers to be provided so many sources can be queried |
|**ApachePropertyReader** | scala-properties-apache | Reads from any implementation of `org.apache.commons.configuration.AbstractConfiguration` |
|**PropertyFileReader** | scala-properties-apache | Given an optional URL for locating a properties file will read properties from that location. If the URL is not supplied it will read the location of the file from a System Property (`property.file`) |
|**VaultSecretsPropertyReader** | scala-properties-vault | Reads properties from the `secrets` storage in [Vault](http://vaultproject.io) |
|**VaultCubbyHolePropertyReader** | scala-properties-vault | Reads properties from the `cubbyhole` storage in [Vault](http://vaultproject.io) |
|**VaultSecretsNestedPropertyReader** | scala-properties-vault | Reads properties from the `secrets` storage in [Vault](http://vaultproject.io), where all the available properties are nested under a single key |
|**VaultCubbyHoleNestedPropertyReader** | scala-properties-vault | Reads properties from the `cubbyhole` storage in [Vault](http://vaultproject.io), where all the available properties are nested under a single key |