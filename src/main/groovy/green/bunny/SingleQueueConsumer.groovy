package green.bunny

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

  def String getConsumerTag() {
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
}
