package org.elasticsearch.plugin.river.kinesis.config

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import org.elasticsearch.river.RiverSettings
import scala.collection.JavaConversions._

/**
 * Created by JohnDeverna on 8/12/14.
 */
case class StreamConfig(region: String,
                        applicationName: String,
                        initialPosition: InitialPositionInStream,
                        streamName: String,
                        numShards: Int,
                        createIfNotExist: Boolean)

object StreamConfig extends Config[StreamConfig] {

  val defaultRegion = "us-east-1"
  val defaultAppName = "elasticsearch-kinesis-river"
  val defaultInitialPosition = "latest"
  val defaultStreamName = "elasticsearch-stream"
  val defaultNumShards = "1"
  val defaultCreateIfNotExists = "true"

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
        numShards = Integer.valueOf(getAsOrElse(es, "numShards", defaultNumShards)),
        createIfNotExist = getAsOrElse(es, "createIfNotExists", defaultCreateIfNotExists).equals("true")
      )
      case _ => new StreamConfig(
        defaultRegion,
        defaultAppName,
        InitialPositionInStream.LATEST,
        defaultStreamName,
        Integer.valueOf(defaultNumShards),
        defaultCreateIfNotExists.equals("true")
      )
    }
  }
}