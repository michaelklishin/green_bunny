package green.bunny

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Envelope
import groovy.transform.TypeChecked

@TypeChecked
class ClosureDelegateConsumer extends SingleQueueConsumer {
  /**
   * Constructs a new instance and records its association to the passed-in channel.
   * @param ch
   */
  protected Closure deliveryHandler
  protected Closure cancelHandler

  ClosureDelegateConsumer(Channel ch, Closure deliveryHandler) {
    super(ch)
    this.deliveryHandler = deliveryHandler
  }

  ClosureDelegateConsumer(Channel ch, Closure deliveryHandler, Closure cancelHandler) {
    super(ch)
    this.deliveryHandler = deliveryHandler
    this.cancelHandler   = cancelHandler
  }

  //
  // API
  //

  /**
   * Called when the consumer is cancelled for reasons <i>other than</i> by a call to
   * {@link Channel#basicCancel}. For example, the queue has been deleted.
   * See {@link #handleCancelOk} for notification of consumer
   * cancellation due to {@link Channel#basicCancel}.
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   * @throws IOException
   */
  @Override
  void handleCancel(String consumerTag) throws IOException {
    this.cancelHandler.call(this.channel, consumerTag)
  }

  /**
   * Called when a <code><b>basic.deliver</b></code> is received for this consumer.
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   * @param envelope packaging data for the message
   * @param properties content header data for the message
   * @param body the message body (opaque, client-specific byte array)
   * @throws IOException if the consumer encounters an I/O error while processing the message
   * @see Envelope
   */
  @Override
  void handleDelivery(String consumerTag, Envelope envelope,
                      AMQP.BasicProperties properties, byte[] body)
      throws IOException {
    this.deliveryHandler.call(this.channel, envelope, properties, body)
  }
}
