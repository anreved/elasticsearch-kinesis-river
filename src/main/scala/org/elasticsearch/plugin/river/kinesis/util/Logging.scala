package org.elasticsearch.plugin.river.kinesis.util

import org.elasticsearch.common.logging.Loggers

/**
 * Simple trait that wraps the ES logger functionality
 *
 * Created by JohnDeverna on 8/8/14.
 */
trait Logging {

  val kinesisLogger = Loggers.getLogger(getClass)

  /**
   * The Log object, simply wraps calls to the ES Logger
   */
  object Log {
    def info(msg: String, params: AnyRef*) = kinesisLogger.info(msg, params:_*)

    def info(msg: String, cause: Throwable, params: AnyRef*) = kinesisLogger.info(msg, cause, params:_*)

    def debug(msg: String, params: AnyRef*) = kinesisLogger.debug(msg, params:_*)

    def debug(msg: String, cause: Throwable, params: AnyRef*) = kinesisLogger.debug(msg, cause, params:_*)

    def warn(msg: String, params: AnyRef*) = kinesisLogger.warn(msg, params:_*)

    def warn(msg: String, cause: Throwable, params: AnyRef*) = kinesisLogger.warn(msg, cause, params:_*)

    def error(msg: String, params: AnyRef*) = kinesisLogger.error(msg, params:_*)

    def error(msg: String, cause: Throwable, params: AnyRef*) = kinesisLogger.error(msg, cause, params:_*)
  }
}

/**
 * Object that implements the Logging trait.  Now Logging can be mixed in as a trait,
 * or imported directly if mixin is not possible
 */
object Logging extends Logging