package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings

/**
 * Created by JohnDeverna on 8/12/14.
 */
case class AwsConfig(accessKey: Option[String] = None,
                     secretKey: Option[String] = None,
                     keyFile: Option[String] = None,
                     keyPropertyPrefix: Option[String] = None)


object AwsConfig extends Config[AwsConfig] {

  def apply(settings: RiverSettings) = {
    settings.settings().containsKey("aws") match {
      case true => {
        val aws = settings.settings().get("aws").asInstanceOf[Map[String, String]]

        new AwsConfig(
          accessKey = aws.get("accessKey"),
          secretKey = aws.get("secretKey"),
          keyFile = aws.get("keyFile"),
          keyPropertyPrefix = aws.get("propertyPrefix")
        )
      }
      case _ => new AwsConfig()
    }
  }
}