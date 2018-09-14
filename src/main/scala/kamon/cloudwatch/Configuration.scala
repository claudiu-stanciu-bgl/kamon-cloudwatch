package kamon.cloudwatch

import com.typesafe.config.Config
import kamon.util.EnvironmentTagBuilder

case class Configuration(region: String, namespace: String, storageResolution: Int, defaultTags: kamon.Tags)

object Configuration {

  def readConfiguration(config: Config): Configuration = {
    val cloudWatchConfig = config.getConfig("kamon.cloudwatch")
    val environmentTags = EnvironmentTagBuilder.create(config.getConfig("kamon.additional-tags"))

    Configuration(
      region = cloudWatchConfig.getString("region"),
      namespace = cloudWatchConfig.getString("namespace"),
      storageResolution = cloudWatchConfig.getInt("storage-resolution"),
      defaultTags = environmentTags
    )
  }

}
