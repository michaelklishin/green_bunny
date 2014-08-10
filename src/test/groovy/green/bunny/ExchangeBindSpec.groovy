package green.bunny

class ExchangeBindSpec extends IntegrationSpec {
  def "binding an exchange to another exchange"(int n) {
    given: "a queue and three exchanges"
    def q  = ch.queue()
    def x1 = ch.fanout("x1", durable: false)
    def x2 = ch.fanout("x2", durable: false)
    def x3 = ch.fanout("x3", durable: false)

    when: "client binds them all in a chain"
    q.bind(x3)
    x3.bind(x2)
    x2.bind(x1)

    and: "publishes a number of messages"
    n.times { x1.publish("hello") }
    Thread.sleep(200)

    then: "all message reach the queue"
    q.messageCount() == n

    cleanup:
    [x1, x2, x3].each { it.delete() }

    where:
    n << (1..10)
  }
}
