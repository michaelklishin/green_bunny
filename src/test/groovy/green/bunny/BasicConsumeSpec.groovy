package green.bunny

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Envelope

import java.util.concurrent.CountDownLatch

class BasicConsumeSpec extends IntegrationSpec {
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
      this.latch.countDown()
      this.latestDeliveryEnvelope = envelope
      this.latestDeliveryProperties = properties
      this.latestDeliveryBody = body
    }
  }
  def "adding a consumer as object to server-named queue"(int n) {
    given: "server-named queue"
    def q = ch.queue()

    and: "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds a consumer"
    def cons = new DeliveryCatcher(ch, l)
    def tag  = q.subscribeWith(cons)

    and: "client publisher a message"
    n.times { q.publish("hello") }

    then: "operation succeeds"
    !(tag == null)
    cons.channel == ch
    awaitOn(cons.latch)

    cleanup:
    cons.cancel()
    q.delete()

    where:
    n << (1..100)
  }
}
