package org.elasticsearch.plugin.river.kinesis.exception

/**
 * Exception denoting that data is not properly formatted or cannot be parsed
 * @param msg the message
 * @param e the underlying exceptino
 *
 * Created by JohnDeverna on 8/9/14.
 */
class PoorlyFormattedDataException(msg: String, e: Exception) extends RuntimeException(msg, e)

/**
 * PoorlyFormattedDataException Companino
 */
object PoorlyFormattedDataException {

  /**
   * Default constructor
   * @return The exception
   */
  def apply() = new PoorlyFormattedDataException("Poorly formatted data", null)

  /**
   * Constructor
   * @param msg The message
   * @return the exception
   */
  def apply(msg: String) = new PoorlyFormattedDataException(msg, null)

  /**
   * Constructor
   * @param e the underlying exception
   * @return the exception
   */
  def apply(e: Exception) = new PoorlyFormattedDataException(e.getMessage, e)
}