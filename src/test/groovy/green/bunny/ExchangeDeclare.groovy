package green.bunny

class ExchangeDeclare extends IntegrationSpec {

  //
  // High-level API
  //

  def "declaring a fanout exchange with all defaults"() {
    when: "exchange is declared"
    def e = ch.fanout(s)

    then: "operation succeeds"
    ensureDeclared(ch, e)
    !e.isPredefined
    !e.isDurable
    !e.isAutoDelete

    where:
    s << (0..100).collect { UUID.randomUUID().toString() }
  }
}
