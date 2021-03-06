package green.bunny

class QueueDeleteSpec extends IntegrationSpec {

  //
  // High-level API
  //

  def "deleting a queue"() {
    given: "queue"
    def q = ch.queue(randomQueueName(), durable: false)

    when: "queue is deleted"
    q.delete()

    then: "operation succeeds"
    ensureQueueDeleted(q)
  }


  //
  // Lower-level API
  //

  def "deleting a queue using queueDelete"() {
    given: "queue"
    def q = ch.queue(randomQueueName(), durable: false)

    when: "queue is deleted"
    ch.queueDelete(q.name)

    then: "operation succeeds"
    ensureQueueDeleted(q)
  }
}
