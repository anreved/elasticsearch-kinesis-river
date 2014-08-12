package org.elasticsearch.plugin.river.kinesis.parser

import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import org.elasticsearch.plugin.river.kinesis.parser.impl.PassThruDataParser

/**
 * Created by JohnDeverna on 8/9/14.
 */
case class KinesisDataParserFactory(riverConfig: KinesisRiverConfig) {

  def getParser : KinesisDataParser = {
    new PassThruDataParser(riverConfig)
  }
}