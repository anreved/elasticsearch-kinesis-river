package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings
import scala.collection.JavaConversions._

/**
 * Created by JohnDeverna on 8/12/14.
 */
case class AwsConfig(accessKey: Option[String] = None,
                     secretKey: Option[String] = None,
                     keyFile: Option[String] = None,
                     keyPropertyPrefix: Option[String] = None)


object AwsConfig extends Config[AwsConfig] {

  def apply(settings: RiverSettings) = {

    getConfigMap(settings, "aws") match {
      case Some(aws) => new AwsConfig(
        accessKey = getAsOpt(aws, "accessKey", classOf[String]),
        secretKey = getAsOpt(aws, "secretKey", classOf[String]),
        keyFile = getAsOpt(aws, "keyFile", classOf[String]),
        keyPropertyPrefix = getAsOpt(aws, "propertyPrefix", classOf[String])
      )
      case _ => new AwsConfig()
    }
  }
}