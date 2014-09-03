package org.elasticsearch.plugin.river.kinesis

import org.elasticsearch.plugins.AbstractPlugin
import org.elasticsearch.river.RiversModule

/**
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

object KinesisRiverPlugin {

  def apply() = new KinesisRiverPlugin

}