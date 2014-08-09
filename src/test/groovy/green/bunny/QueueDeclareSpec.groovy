package green.bunny

import java.security.SecureRandom

class QueueDeclareSpec extends IntegrationSpec {
  def "declaring a server-named queue with all defaults"() {
    when: "the queue is declared"
    def q = ch.queue()

    then: "the queue is declared and retains its attributes"
    ensureDeclared(ch, q)
    ensureServerNamed(q)
    !q.isAutoDelete
    !q.isDurable
    !q.isExclusive

    cleanup:
    q.delete()
  }

  def "declaring a server-named queue"(boolean durable, boolean exclusive, boolean autoDelete) {
    when: "the queue is declared"
    def q = ch.queue("", exclusive: exclusive, durable: durable, autoDelete: autoDelete)

    then: "the queue is declared and retains its attributes"
    ensureDeclared(ch, q)
    ensureServerNamed(q)
    q.isAutoDelete == autoDelete
    q.isDurable == durable
    q.isExclusive == exclusive

    cleanup:
    q.delete()

    where:
    durable | exclusive | autoDelete
    false   | false     | false
    true    | false     | false
    true    | true      | false
    true    | true      | true
    false   | false     | true
    false   | true      | true
    false   | true      | false
    true    | false     | true
  }

  def "declaring a client-named queue"(boolean durable, boolean exclusive, boolean autoDelete) {
    given: "a client-specified name"
    def s = UUID.randomUUID().toString()

    when: "the queue is declared"
    def q = ch.queue(s, exclusive: exclusive, durable: durable, autoDelete: autoDelete)

    then: "the queue is declared and retains its attributes"
    ensureDeclared(ch, q)
    ensureClientNamed(q)
    q.isAutoDelete == autoDelete
    q.isDurable == durable
    q.isExclusive == exclusive

    cleanup:
    q.delete()

    where:
    durable | exclusive | autoDelete
    false   | false     | false
    true    | false     | false
    true    | true      | false
    true    | true      | true
    false   | false     | true
    false   | true      | true
    false   | true      | false
    true    | false     | true
  }


  //
  // Matchers
  //

  void ensureServerNamed(Queue q) {
    assert q.name =~ /^amq\./
    assert q.isServerNamed
  }

  void ensureClientNamed(Queue q) {
    assert !(q.name =~ /^amq\./)
    assert !q.isServerNamed
  }

  void ensureDeclared(Channel ch, Queue q) {
    assert q.channel == ch
    assert ch.queueDeclarePassive(q.name)
  }
}
