package org.elasticsearch.plugin.river.kinesis.util

import com.amazonaws.{AmazonClientException, ClientConfiguration}
import com.amazonaws.regions.{RegionUtils, Region}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.model.{ResourceNotFoundException, DescribeStreamResult}
import java.util.concurrent.TimeUnit
import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentials}
import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import org.elasticsearch.common.inject.{Singleton, Provider, Inject}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import java.util.UUID

/**
 * Wraps much of the Kinesis setup and interaction
 * @param credentials The AWS credentials provider
 * @param riverConfig The river config
 */
@Singleton
class KinesisUtil @Inject() (credentials: Provider[AWSCredentials],
                             riverConfig: KinesisRiverConfig) extends Logging {

  /**
   * The basic Kinesis client config
   */
  private val clientConfig = configureUserAgent(new ClientConfiguration())

  /**
   * The name of the kinesis stream
   */
  private val streamName = riverConfig.streamConfig.streamName

  /**
   * The number of shards in the stream
   */
  private val streamNumShards = riverConfig.streamConfig.numShards

  /**
   * The kinesis client (must be initialized before use)
   */
  var kinesisClient: AmazonKinesisClient = _

  /**
   * Initialize the kinesis client
   *
   * We do it this way so we don't automatically connect
   * unless explicitly called (we don't need unit tests to connect)
   */
  def initKinesisClient = {
    kinesisClient = new AmazonKinesisClient(credentials.get(), clientConfig)
    kinesisClient.setRegion(parseRegion(riverConfig.streamConfig.region))
  }

  /**
   * Creates a new client configuration with a uniquely identifiable value for this application.
   *
   * @param clientConfig The client configuration to copy.
   * @return A new client configuration based on the provided one with its user agent overridden.
   */
  def configureUserAgent(clientConfig: ClientConfiguration): ClientConfiguration = {

    val newConfig = new ClientConfiguration(clientConfig);

    newConfig.setUserAgent(s"${ClientConfiguration.DEFAULT_USER_AGENT} elasticsearch-kinesis-river-plugin/0.1");

    newConfig;
  }

  /**
   * Creates a Region object corresponding to the AWS Region. If an invalid region is passed in
   * then the JVM is terminated with an exit code of 1.
   *
   * @param regionStr the common name of the region for e.g. 'us-east-1'.
   * @return A Region object corresponding to regionStr.
   */
  def parseRegion(regionStr: String): Region = {
    val region = RegionUtils.getRegion(regionStr);

    if (region == null) {
      Log.error("{} is not a valid AWS region.", regionStr);
      throw new RuntimeException
    }

    region;
  }

  /**
   * Create a stream if it doesn't already exist.

   * @throws AmazonServiceException Error communicating with Amazon Kinesis.
   */
  @throws[AmazonClientException]("Any AWS client exception")
  def createStreamIfNotExists(attempt: Int = 0): Unit = {

    if(attempt > KinesisUtil.MAX_RETRIES-1) {
        throw new RuntimeException("Unable to get kinesis stream")
    }

    def createAndTryAgain(doCreate: Boolean = false) = {
      if(doCreate && riverConfig.streamConfig.createIfNotExist && attempt == 0) {
        // create the stream since it wasn't found
        Log.info("Creating stream {} with {} shard(s)", streamName, streamNumShards.toString)
        kinesisClient.createStream(streamName, streamNumShards)
      }

      // try waiting so we can check the status again
      try {
        Thread.sleep(KinesisUtil.DELAY_BETWEEN_STATUS_CHECKS_IN_SECONDS)
      }
      catch {
        case ie: InterruptedException => {
          Log.error("Interrupted while waiting for stream '{}' to become active.  Aborting", streamName)
          throw ie;
        }
      }

      // call again to see if it's active
      createStreamIfNotExists(attempt + 1)
    }

    isActive(kinesisClient.describeStream(streamName)).fold(
      success => success match {
        case true => Log.debug("Kinesis stream {} is active", streamName)
        case false => createAndTryAgain()
      },

      exp => exp match {
        case r: ResourceNotFoundException => createAndTryAgain(true)
        case _ => throw new RuntimeException(exp)
      }
    )
  }

  /**
   * Does the result of a describe stream request indicate the stream is ACTIVE?
   *
   * @param r The describe stream result to check for ACTIVE status.
   */
  def isActive(r: DescribeStreamResult): Either[Boolean, Exception] = {
    try {
      Left("ACTIVE".equals(r.getStreamDescription().getStreamStatus()))
    }
    catch {
      case r: ResourceNotFoundException => Right(r)
    }
  }


  /**
   * Generates the client library config for kinesis - needed by the kinesis worker factory
   * @return the client library config
   */
  def createClientLibraryConfig: KinesisClientLibConfiguration = {
    val kclConfig = new KinesisClientLibConfiguration(
      riverConfig.streamConfig.applicationName,
      riverConfig.streamConfig.streamName,
      new AWSCredentialsProvider {
        override def refresh() = Unit
        override def getCredentials = credentials.get()
      },
      UUID.randomUUID().toString
    )

    kclConfig.withCommonClientConfig(clientConfig)
      .withRegionName(riverConfig.streamConfig.region)
      .withInitialPositionInStream(riverConfig.streamConfig.initialPosition);
  }
}


/**
 * Simple 'static' variable values
 */
object KinesisUtil {
  val DELAY_BETWEEN_STATUS_CHECKS_IN_SECONDS = TimeUnit.SECONDS.toMillis(30)

  val MAX_RETRIES = 3;
}