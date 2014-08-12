package org.elasticsearch.plugin.river.kinesis.exception

/**
 * Created by JohnDeverna on 8/9/14.
 */
class PoorlyFormattedDataException(msg: String, e: Exception) extends RuntimeException(msg, e) {

}


object PoorlyFormattedDataException {
  def apply() = new PoorlyFormattedDataException("Poorly formatted data", null)

  def apply(msg: String) = new PoorlyFormattedDataException(msg, null)

  def apply(e: Exception) = new PoorlyFormattedDataException(e.getMessage, e)
}