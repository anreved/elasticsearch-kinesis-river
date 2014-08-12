package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings

/**
 * Created by JohnDeverna on 8/8/14.
 */
case class KinesisRiverConfig(awsConfig: AwsConfig,
                              streamConfig: StreamConfig,
                              elasticsearchConfig: ElasticsearchConfig,
                              parserConfig: ParserConfig)

object KinesisRiverConfig {

  def apply(settings: RiverSettings) = {
    new KinesisRiverConfig(
         AwsConfig(settings),
         StreamConfig(settings),
         ElasticsearchConfig(settings),
         ParserConfig(settings)
    )
  }
}