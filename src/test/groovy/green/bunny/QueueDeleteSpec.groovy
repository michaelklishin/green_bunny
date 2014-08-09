package green.bunny

class QueueDeleteSpec extends IntegrationSpec {

  //
  // High-level API
  //

  def "deleting a queue"() {
    given: "queue"
    def q = ch.queue("greenbunny-q1", durable: false)

    when: "queue is deleted"
    q.delete()

    then: "operation succeeds"
    ensureDeleted(q)
  }


  //
  // Lower-level API
  //

  def "deleting a queue using queueDelete"() {
    given: "queue"
    def q = ch.queue("greenbunny-q1", durable: false)

    when: "queue is deleted"
    ch.queueDelete(q.name)

    then: "operation succeeds"
    ensureDeleted(q)
  }
}
