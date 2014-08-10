package green.bunny

import green.bunny.consumers.ConfirmingDeliveryCatcher

import java.util.concurrent.CountDownLatch

class BasicAckSpec extends IntegrationSpec {
  def "adding an acking consumer as object to server-named queue"(int n) {
    given: "server-named queue"
    def q = ch.queue()

    and:
    "$n messages to deliver"
    def l = new CountDownLatch(n)

    when: "client adds a consumer"
    def cons = new ConfirmingDeliveryCatcher(ch, l)
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
}
