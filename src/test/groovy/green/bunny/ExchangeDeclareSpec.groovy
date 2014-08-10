package green.bunny

class ExchangeDeclareSpec extends IntegrationSpec {

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

  def "declaring a fanout exchange with overridden attributes"(String s, boolean durable, boolean autoDelete) {
    when: "exchange is declared"
    def e = ch.fanout(s, durable: durable, autoDelete: autoDelete)

    then: "operation succeeds"
    ensureDeclared(ch, e)
    !e.isPredefined
    e.isDurable == durable
    e.isAutoDelete == autoDelete

    cleanup:
    e.delete()

    where:
    s                            | durable | autoDelete
    UUID.randomUUID().toString() |   true  |    true
    UUID.randomUUID().toString() |   false |    false
    UUID.randomUUID().toString() |   false |    true
    UUID.randomUUID().toString() |   true  |    false
  }

  def "declaring a topic exchange with all defaults"() {
    when: "exchange is declared"
    def e = ch.topic(s)

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

  def "declaring a topic exchange with overridden attributes"(String s, boolean durable, boolean autoDelete) {
    when: "exchange is declared"
    def e = ch.topic(s, durable: durable, autoDelete: autoDelete)

    then: "operation succeeds"
    ensureDeclared(ch, e)
    !e.isPredefined
    e.isDurable == durable
    e.isAutoDelete == autoDelete

    cleanup:
    e.delete()

    where:
    s                            | durable | autoDelete
    UUID.randomUUID().toString() |   true  |    true
    UUID.randomUUID().toString() |   false |    false
    UUID.randomUUID().toString() |   false |    true
    UUID.randomUUID().toString() |   true  |    false
  }

}
