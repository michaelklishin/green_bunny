package green.bunny

class PublisherConfirmsSpec extends IntegrationSpec {
  def "using waitForConfirms"(long n) {
    given: "a queue bound to a fanout exchange"
    def x = ch.fanout("x1", durable: false)
    def q = ch.queue()
    q.bind(x)

    and: "publisher confirms enabled on the channel"
    ch.confirmSelect()

    when: "several messages are published"
    n.times { x.publish("hello") }

    and: "client waits for all outstanding publisher confirms"
    def res = ch.waitForConfirms()

    then: "all published messages are confirmed"
    res
    q.messageCount() == n

    cleanup:
    q.delete()
    x.delete()

    where:
    n << (1..20)
  }
}
