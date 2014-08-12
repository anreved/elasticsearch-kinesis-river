package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings

/**
 * Created by JohnDeverna on 8/12/14.
 */
trait Config[T] {

  def apply(settings: RiverSettings): T

  def getAsOrElse[A, B, T](map: Map[A, B], key: A, default: T) = {
    map.get(key) match {
      case Some(v) => v.asInstanceOf[T]
      case _ => default
    }
  }

}
