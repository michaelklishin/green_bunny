package green.bunny

import java.util.concurrent.CountDownLatch

class ConnectionRecoverySpec extends IntegrationSpec {
  @Override
  def setup() {
    // no-op
  }

  @Override
  def cleanup() {
    // no-op
  }

  def "connection recovery"() {
    given: "an open connection that automatically recovers"
    final conn   = connect(true, true)

    when: "the connection is forcefully closed"
    closeAndWaitForRecovery(conn)

    then: "connection is opened again"
    assert conn.isOpen
    assert !conn.isClosed

    cleanup:
    if(conn.isOpen) {
      conn.close()
    }
  }

  def "connection recovery with disabled topology recovery"() {
    given: "an open connection that automatically recovers but does not recover topology"
    final conn = connect(true, false)
    final ch   = conn.createChannel()
    final q    = ch.queue("java-client.test.recovery.q2", durable: false, autoDelete: true, exclusive: true);
    ch.queueDeclarePassive(q.name)

    when: "connection is force-closed and recovers"
    closeAndWaitForRecovery(conn)

    then: "the queue is not recovered"
    try {
      assert conn.isOpen
      ch.queueDeclarePassive(q.name)
      // we expect passive declare to throw
      assert false
    } catch (IOException ignored) {
      // expected
    } finally {
      conn.abort()
    }
  }

  def "shutdown listeners recovery on connection"() {
    given: "an open connection with a shutdown listener"
    final conn  = connect(true, true)
    final latch = new CountDownLatch(2)
    conn.addShutdownListener { latch.countDown() }

    when: "connection is force-closed and recovers"
    closeAndWaitForRecovery(conn)
    conn.close()

    then: "the listener is recovered"
    assert awaitOn(latch)
  }

  def "shutdown listeners recovery on channel"() {
    given: "an open channel with a shutdown listener"
    final conn  = connect(true, true)
    final latch = new CountDownLatch(2)
    final ch    = conn.createChannel()
    ch.addShutdownListener { latch.countDown() }

    when: "its connection is force-closed and recovers"
    closeAndWaitForRecovery(conn)
    ch.close()

    then: "the listener is recovered"
    assert awaitOn(latch)

    cleanup:
    conn.close()
  }

  def "channel recovery"() {
    given: "an open connection with two channels"
    final conn = connect(true, true)
    final ch1  = conn.createChannel()
    final ch2  = conn.createChannel()

    assert ch1.isOpen
    assert ch2.isOpen

    when: "connection is force-closed and recoveres"
    closeAndWaitForRecovery(conn)

    then: "both channels are recovered"
    ch1.isOpen
    ch2.isOpen

    cleanup:
    conn.close()
  }

  protected void closeAndWaitForRecovery(Connection conn) {
    final latch1 = prepareShutdownLatch(conn)
    final latch2 = prepareRecoveryLatch(conn)
    closeAllConnections()
    assert !conn.isOpen
    assert awaitOn(latch1)
    assert awaitOn(latch2)
  }

  protected CountDownLatch prepareRecoveryLatch(Connection conn) {
    final latch = new CountDownLatch(1)
    final rl    = conn.addRecoveryListener { latch.countDown() }
    assert rl != null
    latch
  }

  protected CountDownLatch prepareShutdownLatch(Connection conn) {
    final latch = new CountDownLatch(1)
    conn.addShutdownListener { latch.countDown() }

    latch
  }
}
