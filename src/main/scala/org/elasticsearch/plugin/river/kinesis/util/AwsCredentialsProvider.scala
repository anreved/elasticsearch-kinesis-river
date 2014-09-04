package org.elasticsearch.plugin.river.kinesis.util

import com.amazonaws.auth.{InstanceProfileCredentialsProvider, BasicAWSCredentials, PropertiesCredentials, AWSCredentials}
import org.elasticsearch.common.inject.{Inject, Provider}
import java.io.File
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig

/**
 * Get the AWS credentials so we can connect to Kinesis
 *
 * We support multiple methods of getting the credentials passed in - the order of precedence is:
 * 1) Property file name passed in (should contain props for "accessKey" and "secretKey"
 * 2) Defined in the River configuration
 * 3) Defined as system properties (use the awsKeyPropertyPrefix config)
 * @return the AWSCredentials
 */
class AwsCredentialsProvider @Inject() (riverConfig: KinesisRiverConfig) extends Provider[AWSCredentials] {

  /**
   * Get the credentials
   * @return the AWSCredentials instance
   */
  override def get(): AWSCredentials = {

    val r = riverConfig.awsConfig

    (r.keyFile, r.accessKey, r.secretKey, r.keyPropertyPrefix) match {

      // property file defined
      case (Some(f: String), _, _, _) => new PropertiesCredentials(new File(f))

      // access and secret key passed in
      case (_, Some(a: String), Some(s: String), _) => new BasicAWSCredentials(a, s)

      // property prefix found, look in system properties
      case (_, _, _, Some(prefix: String)) => {
        val accessKey = System.getProperty(prefix + ".accessKey")
        val secretKey = System.getProperty(prefix + ".secretKey")

        if (accessKey == null || secretKey == null) {
          throw new RuntimeException("Credentials prefix defined but no keys found in System properties")
        }

        // if we get here, just return the credentials
        new BasicAWSCredentials(accessKey, secretKey)
      }

      // try to use the system credentials
      case _ => new InstanceProfileCredentialsProvider().getCredentials
    }
  }
}