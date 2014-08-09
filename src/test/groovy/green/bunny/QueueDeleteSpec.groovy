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
}
