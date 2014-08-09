package green.bunny

class QueueDeclareSpec extends IntegrationSpec {

  //
  // High-level API
  //

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

  def "declaring a client-named queue with all defaults"() {
    when: "the queue is declared"
    def q = ch.queue(s)

    then: "the queue is declared and retains its attributes"
    ensureDeclared(ch, q)
    ensureClientNamed(q)
    !q.isAutoDelete
    !q.isDurable
    !q.isExclusive

    cleanup:
    q.delete()

    where:
    s << (0..100).collect { UUID.randomUUID().toString() }
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

  def "declaring a client-named queue"(boolean durable, boolean exclusive, boolean autoDelete, String s) {
    given: "a client-specified name"

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
    durable | exclusive | autoDelete | s
    false   | false     | false      | UUID.randomUUID().toString()
    true    | false     | false      | UUID.randomUUID().toString()
    true    | true      | false      | UUID.randomUUID().toString()
    true    | true      | true       | UUID.randomUUID().toString()
    false   | false     | true       | UUID.randomUUID().toString()
    false   | true      | true       | UUID.randomUUID().toString()
    false   | true      | false      | UUID.randomUUID().toString()
    true    | false     | true       | UUID.randomUUID().toString()
  }


  //
  // Lower-level API
  //

  def "declaring a server-named queue with all defaults using lower-level API"() {
    when: "the queue is declared"
    def q = ch.queueDeclare()

    then: "the queue is declared and retains its attributes"
    ensureDeclared(q)
    ensureServerNamed(q)

    cleanup:
    ch.queueDelete(q)
  }
}
