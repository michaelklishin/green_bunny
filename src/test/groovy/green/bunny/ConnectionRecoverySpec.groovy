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
    conn.isOpen

    cleanup:
    if(conn.isOpen) {
      conn.close()
    }
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
