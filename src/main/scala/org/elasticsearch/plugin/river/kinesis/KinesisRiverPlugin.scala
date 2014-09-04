package org.elasticsearch.plugin.river.kinesis

import org.elasticsearch.plugins.AbstractPlugin
import org.elasticsearch.river.RiversModule

/**
 * The Kinesis plugin
 *
 * Created by JohnDeverna on 8/8/14.
 */
class KinesisRiverPlugin extends AbstractPlugin {

  /**
   * {@inheritdoc
   */
  override def name = "river-kinesis"

  /**
   * {@inheritdoc
   */
  override def description = "Kinesis River Plugin"

  /**
   * Register the kinesis river with the rivers module
   * @param module the rivers module
   */
  def onModule(module: RiversModule) = {
    module.registerRiver("kinesis", classOf[KinesisRiverModule])
  }
}

/**
 * River companion object
 */
object KinesisRiverPlugin {

  /**
   * Constructor
   * @return a new Kinesis River plugin
   */
  def apply() = new KinesisRiverPlugin
}