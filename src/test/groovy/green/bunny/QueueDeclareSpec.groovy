package green.bunny

class QueueDeclareSpec extends IntegrationSpec {
  def "declaring an auto-delete, non-durable, non-exclusive server-named queue"() {
    when: "the queue is declared"
    def q = ch.queue("", autoDelete: true, durable: false, exclusive: false)

    then: "the queue is declared and retains its attributes"
    q.channel == ch
    q.name =~ /^amq\./
    q.isAutoDelete
    !q.isDurable
    !q.isExclusive
    ch.queueDeclarePassive(q.name)
  }
}
