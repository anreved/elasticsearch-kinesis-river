package org.elasticsearch.plugin.river.kinesis.util

import org.elasticsearch.common.logging.Loggers

/**
 * Created by JohnDeverna on 8/8/14.
 *
 * Simple trait that wraps the ES logger functionality
 */
trait Logging {

  val kinesisLogger = Loggers.getLogger(getClass)

  object Log {
    def info(msg: String, params: AnyRef*) = kinesisLogger.info(msg, params:_*)

    def debug(msg: String, params: AnyRef*) = kinesisLogger.debug(msg, params:_*)

    def warn(msg: String, params: AnyRef*) = kinesisLogger.warn(msg, params:_*)

    def error(msg: String, params: AnyRef*) = kinesisLogger.error(msg, params:_*)
  }

}