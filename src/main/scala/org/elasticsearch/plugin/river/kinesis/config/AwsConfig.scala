package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings
import scala.collection.JavaConversions._


/**
 * Configuration specific to AWS credentials
 *
 * @param accessKey The optional access key
 * @param secretKey The optional secret key
 * @param keyFile   The optional file containing an access and secret key
 * @param keyPropertyPrefix The prefix for System properties -- parsed as prefix.accessKey and prefix.secretKey
 *
 * Created by JohnDeverna on 8/12/14.
 */
case class AwsConfig(accessKey: Option[String] = None,
                     secretKey: Option[String] = None,
                     keyFile: Option[String] = None,
                     keyPropertyPrefix: Option[String] = None)

/**
 * AWS Config companion
 */
object AwsConfig extends Config[AwsConfig] {

  /**
   * Constructor
   * @param settings the river settings
   * @return an AWS Config instance
   */
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