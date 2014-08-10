package green.bunny

class ExchangeDeclareSpec extends IntegrationSpec {

  //
  // High-level API
  //

  def "accessing default exchange"() {
    when: "default exchange (singleton) is accessed"
    def e = ch.defaultExchange()

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

  def "declaring a direct exchange with all defaults"() {
    when: "exchange is declared"
    def e = ch.direct(s)

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

  def "declaring a direct exchange with overridden attributes"(String s, boolean durable, boolean autoDelete) {
    when: "exchange is declared"
    def e = ch.direct(s, durable: durable, autoDelete: autoDelete)

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

  def "declaring an exchange with overridden attributes"(String s, String type, boolean durable, boolean autoDelete) {
    when: "exchange is declared"
    def e = ch.exchange(s, type, durable: durable, autoDelete: autoDelete)

    then: "operation succeeds"
    ensureDeclared(ch, e)
    e.type == type
    !e.isPredefined
    e.isDurable == durable
    e.isAutoDelete == autoDelete

    cleanup:
    e.delete()

    where:
    s                            |   type   | durable | autoDelete
    UUID.randomUUID().toString() | "fanout" |   true  |    true
    UUID.randomUUID().toString() | "fanout" |   false |    false
    UUID.randomUUID().toString() | "fanout" |   false |    true
    UUID.randomUUID().toString() | "fanout" |   true  |    false
    UUID.randomUUID().toString() | "topic"  |   true  |    true
    UUID.randomUUID().toString() | "topic"  |   false |    false
    UUID.randomUUID().toString() | "topic"  |   false |    true
    UUID.randomUUID().toString() | "topic"  |   true  |    false
    UUID.randomUUID().toString() | "direct" |   true  |    true
    UUID.randomUUID().toString() | "direct" |   false |    false
    UUID.randomUUID().toString() | "direct" |   false |    true
    UUID.randomUUID().toString() | "direct" |   true  |    false
  }
}
