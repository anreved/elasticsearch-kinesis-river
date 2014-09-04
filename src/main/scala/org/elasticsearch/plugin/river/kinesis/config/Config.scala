package org.elasticsearch.plugin.river.kinesis.config

import org.elasticsearch.river.RiverSettings
import scala.collection.JavaConversions._

/**
 * Provides common functionality for all Config instances
 *
 * Created by JohnDeverna on 8/12/14.
 */
trait Config[T] {

  implicit def stringToBoolean(s: String): Boolean = s.equals("true")
  implicit def stringToInteger(s: String): Int = Integer.valueOf(s)

  /**
   * Force implementation of the constructor
   * @param settings the river settings
   * @return a Config instance
   */
  def apply(settings: RiverSettings): T

  /**
   * Gets the settings with the given key, or returns None if key is not configured
   * @param settings the settings
   * @param key the key we're looking for
   * @return the Option[Map] for the given key
   */
  def getConfigMap(settings: RiverSettings, key: String): Option[Map[String, AnyRef]] = {
    settings.settings().containsKey(key) match {
      case true => {
        val m = mapAsScalaMap[String, AnyRef](settings.settings().get(key).asInstanceOf[java.util.HashMap[String, Object]])

        // return it as an immutable map
        Some(m.toMap)
      }
      case _ => None
    }
  }

  /**
   * Gets a property value, or returns a default value
   * @param map the map
   * @param key the key we're looking for
   * @param default the default value
   * @tparam T the return type
   * @return The found value cast as T, or the default value
   */
  def getAsOrElse[T](map: Map[String, AnyRef], key: String, default: T) : T = {
    map.get(key) match {
      // types match, just cast and return
      case Some(v) if(v.getClass.equals(default.getClass)) => v.asInstanceOf[T]

      // types don't match
      case Some(v) => default.getClass match {
        case c if(c.equals(classOf[java.lang.Boolean])) => ("true".equals(v.toString)).asInstanceOf[T]
        case c if(c.equals(classOf[java.lang.Integer])) => Integer.valueOf(v.toString).asInstanceOf[T]
        case _ => v.asInstanceOf[T]
      }

      case _ => default
    }
  }

  /**
   * Default getAsOpt method, assumes the expected type is String
   * @param map the properties map
   * @param key the key we're looking for
   * @return the Option[String] for the key we're looking for
   */
  def getAsOpt(map: Map[String, AnyRef], key: String) : Option[String] = getAsOpt(map, key, classOf[String])

  /**
   * Looks for a map entry for the specified key.  If not found, will return None
   * @param map the map
   * @param key the key we're looking for
   * @param expectedType the expected type of the return value - Option[X]
   * @tparam T the return type
   * @return an Option[T] if the key exists
   */
  def getAsOpt[T](map: Map[String, AnyRef], key: String, expectedType: Class[T]) : Option[T] = {
    map.get(key) match {
      // types match, just cast and return
      case Some(v) if(v.getClass.equals(expectedType)) => Some(v.asInstanceOf[T])

      // types don't match
      case Some(v) => expectedType match {
        case c if(c.equals(classOf[java.lang.Boolean])) => Some(("true".equals(v.toString)).asInstanceOf[T])
        case c if(c.equals(classOf[java.lang.Integer])) => Some(Integer.valueOf(v.toString).asInstanceOf[T])
        case _ => Some(v.asInstanceOf[T])
      }

      // no value found
      case _ => None
    }
  }
}