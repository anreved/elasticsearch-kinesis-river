package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings
import org.elasticsearch.plugin.river.kinesis.parser.KinesisDataParser
import org.elasticsearch.plugin.river.kinesis.parser.impl.PassThruDataParser


/**
 * Created by JohnDeverna on 8/12/14.
 */
case class ParserConfig(parserClass: Class[_ <: KinesisDataParser] = classOf[PassThruDataParser],
                        encoding: String = "UTF-8")

object ParserConfig extends Config[ParserConfig] {
  def apply(settings: RiverSettings) = {

    settings.settings().containsKey("parser") match {
      case true => {
        val es = settings.settings().get("parser").asInstanceOf[Map[String, AnyRef]]

        new ParserConfig(
          parserClass = Class.forName(
            getAsOrElse(es, "class", "org.elasticsearch.plugin.river.kinesis.parser.impl.PassThruDataParser")
          ),
          encoding = getAsOrElse(es, "encoding", "")
        )
      }
      case _ => new ParserConfig()
    }
  }
}