package org.elasticsearch.plugin.river.kinesis.config

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import org.elasticsearch.river.RiverSettings
import scala.collection.JavaConversions._


/**
 * The Kinesis specific configuration
 * @param region the AWS region
 * @param applicationName The application name
 * @param initialPosition The initial position in stream
 * @param streamName the name of the Kinesis stream
 * @param numShards the number of shards
 * @param createIfNotExist True to create the stream if it doesn't already exist
 *
 * Created by JohnDeverna on 8/12/14.
 */
case class StreamConfig(region: String,
                        applicationName: String,
                        initialPosition: InitialPositionInStream,
                        streamName: String,
                        numShards: Int,
                        createIfNotExist: Boolean)


/**
 * The StreamConfig companion
 */
object StreamConfig extends Config[StreamConfig] {

  val defaultRegion = "us-east-1"
  val defaultAppName = "elasticsearch-kinesis-river"
  val defaultInitialPosition = "latest"
  val defaultStreamName = "elasticsearch-stream"
  val defaultNumShards = 1
  val defaultCreateIfNotExists = true

  /**
   * Constructor
   * @param settings the river settings
   * @return a Stream Config instance
   */
  def apply(settings: RiverSettings) = {

    getConfigMap(settings, "kinesis") match {
      case Some(es) => new StreamConfig(
        region = getAsOrElse(es, "region", defaultRegion),
        applicationName = getAsOrElse(es, "applicationName", defaultAppName),
        initialPosition = getAsOrElse(es, "initialPosition", defaultInitialPosition) match {
          case s if (s.equalsIgnoreCase("latest")) => InitialPositionInStream.LATEST
          case _ => InitialPositionInStream.TRIM_HORIZON
        },
        streamName = getAsOrElse(es, "streamName", defaultStreamName),
        numShards = getAsOrElse(es, "numShards", defaultNumShards),
        createIfNotExist = getAsOrElse(es, "createIfNotExists", defaultCreateIfNotExists)
      )
      case _ => new StreamConfig(
        defaultRegion,
        defaultAppName,
        InitialPositionInStream.LATEST,
        defaultStreamName,
        defaultNumShards,
        defaultCreateIfNotExists
      )
    }
  }
}