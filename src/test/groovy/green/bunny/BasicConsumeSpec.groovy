package green.bunny

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Envelope
import green.bunny.consumers.DeliveryCatcher

import java.util.concurrent.CountDownLatch

class BasicConsumeSpec extends IntegrationSpec {
  def "adding a consumer as object to server-named queue w/o provided consumer tag"(int n) {
    given: "server-named queue"
    def q = ch.queue()

    and:
    "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds a consumer"
    def cons = new DeliveryCatcher(ch, l)
    def tag = q.subscribeWith(cons)

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

    and:
    "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds a consumer"
    def cons = new DeliveryCatcher(ch, l)
    def tag = q.subscribeWith(cons)

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

    and:
    "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds a consumer"
    def cons = new DeliveryCatcher(ch, l)
    def tag = q.subscribeWith(cons, consumerTag: consumerTag)

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

  def "adding a lambda consumer to server-named queue w/o provided consumer tag"(int n) {
    given: "server-named queue"
    def q = ch.queue()

    and:
    "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds a consumer"
    def cons = q.subscribe { Channel ch, Envelope envelope, AMQP.BasicProperties properties, byte[] body ->
      l.countDown()
    }

    and: "client publisher a message"
    n.times { q.publish("hello") }

    then: "operation succeeds"
    !(cons.consumerTag == null)
    awaitOn(l)

    cleanup:
    // works around a race condition between basic.ack and channel shutdown
    Thread.sleep(100)
    cons.cancel()
    q.delete()

    where:
    n << (1..5)
  }

  def "adding a lambda consumer to server-named queue using manual acknowledgements"(int n) {
    given: "server-named queue"
    def q = ch.queue()

    and:
    "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds an acking consumer"
    def cons = q.subscribe({ Channel ch, Envelope envelope, AMQP.BasicProperties properties, byte[] body ->
          l.countDown()
          ch.basicAck(envelope.deliveryTag)
        },
        { Channel ch, String consumerTag ->
          // no-op
        },
        autoAck: false)

    and: "client publisher a message"
    n.times { q.publish("hello") }

    then: "operation succeeds"
    !(cons.consumerTag == null)
    awaitOn(l)

    cleanup:
    Thread.sleep(100)
    cons.cancel()
    q.delete()

    where:
    n << (1..5)
  }
}
