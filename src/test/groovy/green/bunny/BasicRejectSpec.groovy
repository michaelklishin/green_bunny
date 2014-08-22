package green.bunny

import green.bunny.consumers.RejectingDeliveryCatcher

import java.util.concurrent.CountDownLatch

class BasicRejectSpec extends IntegrationSpec {
  def "adding a rejecting consumer as object to server-named queue"(int n) {
    given: "server-named queue"
    def q = ch.queue()

    and:
    "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds a consumer"
    def cons = new RejectingDeliveryCatcher(ch, l)
    def tag = q.subscribeWith(cons, autoAck: false)

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

  def "rejecting a message with re-queue = true"(int n) {
    given: "server-named queue"
    def q = ch.queue()

    and:
    "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client basic.gets $n messages"
    n.times { q.publish("hello") }
    def xs = []
    n.times {
      xs.add(q.get(false).envelope.deliveryTag)
    }

    and: "re-queues them all"
    xs.each { long it -> ch.basicReject(it, true) }

    then: "there are $n messages ready in the queue"
    Thread.sleep(100)
    q.messageCount() == n

    cleanup:
    q.delete()

    where:
    n << (1..5)
  }
}
