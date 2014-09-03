package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings
import org.elasticsearch.plugin.river.kinesis.parser.KinesisDataParser
import org.elasticsearch.plugin.river.kinesis.parser.impl.PassThruDataParser
import org.elasticsearch.plugin.river.kinesis.util.Logging

/**
 * Configuration for the data parser
 * @param parserClass the parser class
 * @param additionalConfig the map of configs for the specific parser being used
 *
 * Created by JohnDeverna on 8/12/14.
 */
case class ParserConfig(parserClass: Class[_ <: KinesisDataParser],
                        additionalConfig: Map[String, AnyRef])

object ParserConfig extends Config[ParserConfig] with Logging {

  lazy val defaultParserClass = classOf[PassThruDataParser]

  /**
   * Constructor
   * @param settings the river settings
   * @return a Config instance
   */
  def apply(settings: RiverSettings) = {

    getConfigMap(settings, "parser") match {
      case Some(es) => new ParserConfig(
        parserClass = getAsOpt(es, "class") match {

          // a class was specified in the config
          case Some(p: String) => {

            Log.info("Looking for parser of type: {}", p)

            val c = Class.forName(p)

            if (!classOf[KinesisDataParser].isAssignableFrom(c)) {
              throw new RuntimeException(s"Invalid parser class ${c.getName}")
            }

            c.asInstanceOf[Class[KinesisDataParser]]
          }

          // no class specified -- use the default
          case _ => classOf[PassThruDataParser]
        },
        additionalConfig = es
      )
      case _ => new ParserConfig(defaultParserClass, Map[String, AnyRef]())
    }
  }
}