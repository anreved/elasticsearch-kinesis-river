package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings
import org.elasticsearch.common.inject.Inject


/**
 * The Kinesis River Config  - this is just a wrapper around the 4 main configuration sections
 * @param settings The global RiverSettings
 *
 * Created by JohnDeverna on 8/8/14.
 */
class KinesisRiverConfig @Inject()(settings: RiverSettings) {

  val awsConfig: AwsConfig = AwsConfig(settings)

  val streamConfig: StreamConfig = StreamConfig(settings)

  val elasticsearchConfig: ElasticsearchConfig = ElasticsearchConfig(settings)

  val parserConfig: ParserConfig = ParserConfig(settings)
}