package green.bunny

import groovy.transform.TypeChecked

@TypeChecked
class SingleQueueConsumer extends DefaultConsumer {

  //
  // Fields
  //

  String _consumerTag

  //
  // API
  //

  /**
   * Constructs a new instance and records its association to the passed-in channel.
   * @param ch
   */

  SingleQueueConsumer(Channel ch) {
    super(ch)
  }

  String getConsumerTag() {
    this._consumerTag
  }

  /**
   * Called when the consumer is registered by a call to any of the
   * {@link Channel#basicConsume} methods.
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   */
  @Override
  void handleConsumeOk(String consumerTag) {
    this._consumerTag = consumerTag
  }

  //
  // Implementation
  //

  /**
   * Cancels
   * @return the <i>consumer tag</i> this consumer instance was previously using
   */
  String cancel() {
    // TODO: synchronize access to _consumerTag
    channel.basicCancel(this._consumerTag)
    this._consumerTag
  }
}
