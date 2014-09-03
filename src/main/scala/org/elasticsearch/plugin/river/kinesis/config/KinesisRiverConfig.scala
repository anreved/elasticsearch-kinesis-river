package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings
import org.elasticsearch.common.inject.Inject

/**
 * Created by JohnDeverna on 8/8/14.
 */
class KinesisRiverConfig @Inject()(settings: RiverSettings) {

  val awsConfig: AwsConfig = AwsConfig(settings)

  val streamConfig: StreamConfig = StreamConfig(settings)

  val elasticsearchConfig: ElasticsearchConfig = ElasticsearchConfig(settings)

  val parserConfig: ParserConfig = ParserConfig(settings)
}