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

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
      this.latch.countDown()
      this.latestDeliveryEnvelope = envelope
      this.latestDeliveryProperties = properties
      this.latestDeliveryBody = body
    }
  }

  class ConfirmingDeliveryCatcher extends DeliveryCatcher {
    ConfirmingDeliveryCatcher(Channel ch, CountDownLatch latch) {
      super(ch, latch)
    }

    @Override
    void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
      this.channel.basicAck(envelope.deliveryTag)
      super.handleDelivery(consumerTag, envelope, properties, body)
    }
  }

  def "adding a consumer as object to server-named queue w/o provided consumer tag"(int n) {
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
    n << (1..5)
  }

  def "adding a consumer as object to client-named queue w/o provided consumer tag"(int n) {
    given: "client-named queue"
    def q = ch.queue(UUID.randomUUID().toString(), exclusive: true)

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
    n << (1..5)
  }

  def "adding a consumer as object to server-named queue with provided consumer tag"(String consumerTag, int n) {
    given: "server-named queue"
    def q = ch.queue()

    and: "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds a consumer"
    def cons = new DeliveryCatcher(ch, l)
    def tag  = q.subscribeWith(cons, consumerTag: consumerTag)

    and: "client publisher a message"
    n.times { q.publish("hello") }

    then: "operation succeeds"
    tag == consumerTag
    cons.channel == ch
    awaitOn(cons.latch)

    cleanup:
    ch.basicCancel(consumerTag)
    q.delete()

    where:
    consumerTag | n
    "tag1"      | 1
    "123123"    | 2
    "tag_2"     | 3
    "tag 3"     | 4
    "4 tag"     | 5
  }

  def "adding a consumer as object to server-named queue with manual acknowledgements"(int n) {
    given: "server-named queue"
    def q = ch.queue()

    and: "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds a consumer"
    def cons = new ConfirmingDeliveryCatcher(ch, l)
    def tag  = q.subscribeWith(cons, autoAck: false)

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
    n << (1..5)
  }
}
