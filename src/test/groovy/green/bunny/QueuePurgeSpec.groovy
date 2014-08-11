package green.bunny

class QueuePurgeSpec extends IntegrationSpec {
  def "purging a queue"(long n) {
    given: "a queue with messages"
    def q = ch.queue()
    n.times { q.publish("msg") }

    when: "client purges the queue"
    q.purge()

    then: "the queue is empty"
    q.isEmpty

    cleanup:
    q.delete()

    where:
    n << (0..20)
  }
}
