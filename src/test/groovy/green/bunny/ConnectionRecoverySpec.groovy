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
    final latch1 = new CountDownLatch(1)
    final latch2 = new CountDownLatch(1)
    assert conn.isOpen
    conn.addShutdownListener { latch1.countDown() }
    final rl = conn.addRecoveryListener { latch2.countDown() }
    assert rl != null

    when: "the connection is forcefully closed"
    closeAllConnections()
    assert !conn.isOpen

    and: "recovery completes"
    assert awaitOn(latch1)
    assert awaitOn(latch2)

    then: "connection is opened again"
    conn.isOpen

    cleanup:
    if(conn.isOpen) {
      conn.close()
    }
  }
}
