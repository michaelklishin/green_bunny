package green.bunny.consumers

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Envelope
import green.bunny.Channel
import green.bunny.SingleQueueConsumer

import java.util.concurrent.CountDownLatch

class DeliveryCatcher extends SingleQueueConsumer {
  protected CountDownLatch latch

  Envelope latestDeliveryEnvelope
  AMQP.BasicProperties latestDeliveryProperties
  byte[] latestDeliveryBody

  DeliveryCatcher(Channel ch, CountDownLatch latch) {
    super(ch)
    this.latch = latch
  }

  def CountDownLatch getLatch() {
    this.latch
  }

  @Override
  void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    this.latch.countDown()
    this.latestDeliveryEnvelope = envelope
    this.latestDeliveryProperties = properties
    this.latestDeliveryBody = body
  }
}
