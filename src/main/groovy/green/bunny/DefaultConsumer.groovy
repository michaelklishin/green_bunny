package green.bunny

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.ShutdownSignalException
import groovy.transform.TypeChecked

/**
 * This is a version of {@link com.rabbitmq.client.DefaultConsumer} that is aware
 * of Green Bunny's Channel class.
 */
@TypeChecked
class DefaultConsumer implements Consumer {

  //
  // Fields
  //

  protected Channel _channel

  //
  // Constructors
  //

  /**
   * Constructs a new instance and records its association to the passed-in channel.
   * @param channel the channel to which this consumer is attached
   */
  DefaultConsumer(Channel ch) {
    this._channel = ch
  }

  //
  // API
  //

  Channel getChannel() {
    this._channel
  }

  /**
   * Called when the consumer is registered by a call to any of the
   * {@link Channel#basicConsume} methods.
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   */

  @Override
  void handleConsumeOk(String consumerTag) {
    // no-op
  }

  /**
   * Called when the consumer is cancelled by a call to {@link Channel#basicCancel}.
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   */
  @Override
  void handleCancelOk(String consumerTag) {
    // no-op
  }

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
    // no-op
  }

  /**
   * Called when either the channel or the underlying connection has been shut down.
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   * @param sig a {@link ShutdownSignalException} indicating the reason for the shut down
   */
  @Override
  void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
    // no-op
  }

  /**
   * Called when a <code><b>basic.recover-ok</b></code> is received
   * in reply to a <code><b>basic.recover</b></code>. All messages
   * received before this is invoked that haven't been <i>ack</i>'ed will be
   * re-delivered. All messages received afterwards won't be.
   * @param consumerTag the <i>consumer tag</i> associated with the consumer
   */
  @Override
  void handleRecoverOk(String consumerTag) {
    // no-op
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
  void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    // no-op
  }
}
