package org.elasticsearch.plugin.river.kinesis.parser

import org.elasticsearch.plugin.river.kinesis.config.KinesisRiverConfig
import org.elasticsearch.common.inject.{Singleton, Inject, Injector, Provider}

/**
 * Created by JohnDeverna on 8/9/14.
 */
@Singleton
class KinesisDataParserProvider @Inject() (riverConfig: KinesisRiverConfig,
                                           injector: Injector) extends Provider[KinesisDataParser] {

  /**
   * have guice get an instance of our parser class for us
   * @return an instance of the parser class
   */
  override def get(): KinesisDataParser = {
    injector.getInstance(riverConfig.parserConfig.parserClass)
  }

}