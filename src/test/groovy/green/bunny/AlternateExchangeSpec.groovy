package green.bunny

class AlternateExchangeSpec extends IntegrationSpec {
  def "publishing unroutable message to exchange that has an AE configured"() {
    given: "an exchange with AE configured"
    def ae = ch.fanout("x-ae")
    def x  = ch.fanout("x", arguments: ["alternate-exchange": ae.name])

    and: "a queue bound to the AE"
    def q  = ch.queue().bind(ae)

    when: "client publishes a message to the exchange"
    x.publish("hello")

    then: "the message is routed to the queue via AE"
    q.messageCount() == 1

    cleanup:
    q.delete()
    x.delete()
    ae.delete()
  }
}
