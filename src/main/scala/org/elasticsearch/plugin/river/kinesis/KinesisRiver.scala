package org.elasticsearch.plugin.river.kinesis

import org.elasticsearch.common.inject.Inject
import org.elasticsearch.river.{RiverSettings, RiverName, River, AbstractRiverComponent}
import org.elasticsearch.client.Client
import com.amazonaws.auth._
import java.io.File
import org.elasticsearch.plugin.river.kinesis.util.{Logging, KinesisUtil}
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{Worker, KinesisClientLibConfiguration}
import java.util.UUID
import scala.Some
import org.elasticsearch.plugin.river.kinesis.parser.KinesisDataParserFactory

/**
 * Created by JohnDeverna on 8/8/14.
 */
class KinesisRiver(@Inject riverName: RiverName,
                   @Inject settings: RiverSettings,
                   @Inject client: Client
                  ) extends AbstractRiverComponent(riverName, settings)
                    with River
                    with Logging {

  // parse out the configs and make sure we have everything we need
  val riverConfig = KinesisRiverConfig(settings)


  override def start() = {

    // get the AWS credentials
    val credentials = getAwsCredentials();

    // create a util instance
    val kinesisUtil = new KinesisUtil(credentials, riverConfig)

    // ensure the shard exists and is active
    kinesisUtil.createStreamIfNotExists()

    val kclConfig = new KinesisClientLibConfiguration(
      riverConfig.streamConfig.applicationName,
      riverConfig.streamConfig.streamName,
      new AWSCredentialsProvider {
        override def refresh() = Unit
        override def getCredentials = getAwsCredentials()
      },
      UUID.randomUUID().toString
    )

    kclConfig.withCommonClientConfig(kinesisUtil.clientConfig)
      .withRegionName(riverConfig.streamConfig.region)
      .withInitialPositionInStream(riverConfig.streamConfig.initialPosition);

    val dataParserFactory = KinesisDataParserFactory(riverConfig)
    val processorFactory = KinesisRecordProcessorFactory(client, riverConfig, dataParserFactory)

    val worker = new Worker(processorFactory, kclConfig);

    try {
      worker.run()
    }
    catch {
      case t: Throwable => {
        Log.error("Caught throwable while processing data.", t);
      }
    }

  }

  override def close() = {

    // shut it down

  }


  /**
   * Get the AWS credentials so we can connect to Kinesis
   *
   * We support multiple methods of getting the credentials passed in - the order of precedence is:
   * 1) Property file name passed in (should contain props for "accessKey" and "secretKey"
   * 2) Defined in the River configuration
   * 3) Defined as system properties (use the awsKeyPropertyPrefix config)
   * @return the AWSCredentials
   */
  private def getAwsCredentials() : AWSCredentials = {

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