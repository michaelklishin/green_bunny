package green.bunny.consumers

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Envelope
import green.bunny.Channel

import java.util.concurrent.CountDownLatch

class RejectingDeliveryCatcher extends DeliveryCatcher {
  RejectingDeliveryCatcher(Channel ch, CountDownLatch latch) {
    super(ch, latch)
  }

  @Override
  void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    this.channel.basicAck(envelope.deliveryTag)
    super.handleDelivery(consumerTag, envelope, properties, body)
  }
}
