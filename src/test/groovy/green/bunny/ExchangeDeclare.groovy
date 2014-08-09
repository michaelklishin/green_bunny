package green.bunny

class ExchangeDeclare extends IntegrationSpec {

  //
  // High-level API
  //

  def "accessing default exchange"() {
    when: "default exchange (singleton) is accessed"
    def e = ch.defaultExchange()
    println(e)

    then: "durable, non-auto-delete, predefined direct exchange is returned"
    e.isPredefined
    e.isDurable
    !e.isAutoDelete
    e.type == "direct"
  }

  def "declaring a fanout exchange with all defaults"() {
    when: "exchange is declared"
    def e = ch.fanout(s)

    then: "operation succeeds"
    ensureDeclared(ch, e)
    !e.isPredefined
    !e.isDurable
    !e.isAutoDelete

    cleanup:
    e.delete()

    where:
    s << (0..100).collect { UUID.randomUUID().toString() }
  }
}
