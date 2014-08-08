package green.bunny

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

  def "declaring a non-durable, exclusive, non-auto-delete server-named queue"() {
    when: "the queue is declared"
    def q = ch.queue("", exclusive: true)

    then: "the queue is declared and retains its attributes"
    ensureDeclared(ch, q)
    ensureServerNamed(q)
    !q.isAutoDelete
    !q.isDurable
    q.isExclusive

    cleanup:
    q.delete()
  }

  def "declaring a non-durable, non-exclusive, auto-delete server-named queue"() {
    when: "the queue is declared"
    def q = ch.queue("", autoDelete: true, durable: false, exclusive: false)

    then: "the queue is declared and retains its attributes"
    ensureDeclared(ch, q)
    ensureServerNamed(q)
    q.isAutoDelete
    !q.isDurable
    !q.isExclusive

    cleanup:
    q.delete()
  }

  //
  // Matchers
  //

  void ensureServerNamed(Queue q) {
    assert q.name =~ /^amq\./
    assert q.isServerNamed
  }

  void ensureDeclared(Channel ch, Queue q) {
    assert q.channel == ch
    assert ch.queueDeclarePassive(q.name)
  }
}
