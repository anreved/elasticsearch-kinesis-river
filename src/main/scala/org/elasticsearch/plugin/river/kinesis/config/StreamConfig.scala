package org.elasticsearch.plugin.river.kinesis.config

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import org.elasticsearch.river.RiverSettings

/**
 * Created by JohnDeverna on 8/12/14.
 */
case class StreamConfig(region: String = "us-east",
                        applicationName: String = "elasticsearch-kinesis-river",
                        initialPosition: InitialPositionInStream = InitialPositionInStream.LATEST,
                        streamName: String = "elasticsearch-stream",
                        numShards: Int = 1,
                        createIfNotExist: Boolean = true)

object StreamConfig extends Config[StreamConfig] {

  def apply(settings: RiverSettings) = {

    settings.settings().containsKey("kinesis") match {
      case true => {
        val es = settings.settings().get("kinesis").asInstanceOf[Map[String, AnyRef]]

        new StreamConfig(
          region = getAsOrElse(es, "region", "us-east"),
          applicationName = getAsOrElse(es, "applicationName", "elasticsearch-kinesis-river"),
          initialPosition = getAsOrElse(es, "initialPosition", "latest") match {
            case s if (s.equalsIgnoreCase("latest")) => InitialPositionInStream.LATEST
            case _ => InitialPositionInStream.TRIM_HORIZON
          },
          streamName = getAsOrElse(es, "streamName", "elasticsearch-stream"),
          numShards = getAsOrElse(es, "numShards", 1),
          createIfNotExist = getAsOrElse(es, "createIfNotExists", true)
        )
      }
      case _ => new StreamConfig()
    }
  }
}