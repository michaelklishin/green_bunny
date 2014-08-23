package green.bunny

import com.rabbitmq.client.AMQP

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

    when: "connection is force-closed and recovers"
    closeAndWaitForRecovery(conn)

    then: "both channels are recovered"
    ch1.isOpen
    ch2.isOpen

    cleanup:
    conn.close()
  }

  def "return listener recovery"() {
    given: "an open channel with a basic.return listener"
    final conn  = connect(true, true)
    final ch    = conn.createChannel()
    final latch = new CountDownLatch(1)
    final x     = ch.defaultExchange
    ch.addReturnListener { int replyCode,
                           String replyText,
                           String exchangeName,
                           String routingKey,
                           AMQP.BasicProperties props,
                           byte[] body ->
      latch.countDown()
    }

    when: "connection is force-closed and recovers"
    closeAndWaitForRecovery(conn)

    and: "an unroutable message is published"
    x.publish(UUID.randomUUID().toString(), mandatory: true)

    then: "the listener is recovered"
    assert awaitOn(latch)

    cleanup:
    conn.close()
  }

  def "confirm listener recovery"() {
    given: "an open channel with publisher confirms enabled and a recovery listener"
    final latch = new CountDownLatch(1)
    final conn  = connect(true, true)
    final ch    = conn.createChannel().with {
      it.confirmSelect()
      it.addConfirmListener { long deliveryTag, boolean multile ->
        latch.countDown()
      }
      it
    }
    final q     = ch.queue()

    when: "connection is force-closed and recovers"
    closeAndWaitForRecovery(conn)

    and: "a routable message is published"
    q.publish("payload")

    then: "the listener is recovered"
    assert awaitOn(latch)

    cleanup:
    conn.close()
  }

  def "exchange recovery"() {
    given: "an open connection and an exchange declared on it"
    final conn = connect(true, true)
    final ch   = conn.createChannel()
    final x    = ch.fanout("green.bunny.fanouts.1")

    when: "connection is force-closed and recovers"
    closeAndWaitForRecovery(conn)

    then: "the exchange is re-declared"
    ensureExchangeRecovered(ch, x)

    cleanup:
    x.delete()
    conn.close()
  }

  def "client-named queue recovery"() {
    given: "an open connection and a client-named queue declared on it"
    final conn = connect(true, true)
    final ch   = conn.createChannel()
    final q    = ch.queue("green.bunny.queues.1", durable: false, exclusive: false, autoDelete: false)

    when: "connection is force-closed and recovers"
    closeAndWaitForRecovery(conn)

    then: "the queue is re-declared"
    ensureQueueRecovered(ch, q)

    cleanup:
    q.delete()
    conn.close()
  }

  def "server-named queue recovery"() {
    given: "an open connection and a server-named queue declared on it"
    final conn = connect(true, true)
    final ch   = conn.createChannel()
    final q    = ch.queue("", durable: false, exclusive: false, autoDelete: false)
    final originalName = q.name
    ensureServerNamed(q)

    when: "connection is force-closed and recovers"
    closeAndWaitForRecovery(conn)

    then: "the queue is re-declared"
    ensureQueueRecovered(ch, q)
    ensureServerNamed(q)
    // TODO: we need Java client API adjustments to update
    //       queue name after recovery
    // q.name != originalName

    cleanup:
    q.delete()
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

  protected boolean ensureExchangeRecovered(Channel ch, Exchange x) {
    ch.confirmSelect()
    final q  = ch.queue()
    final rk = "routing-key"
    q.bind(x, routingKey: rk)
    x.publish("msg", routingKey: rk, mandatory: true)
    assert ch.waitForConfirms(500)

    ch.exchangeDeclarePassive(x.name)
    q.delete()
    true
  }

  protected boolean ensureQueueRecovered(Channel ch, Queue q) {
    final n = q.messageCount()
    ch.confirmSelect()
    final x = ch.fanout("green.bunny.fanouts.0")
    q.bind(x)
    x.publish("msg")
    ch.waitForConfirms(500)
    assert q.messageCount() == (n + 1)
    x.delete()
    true
  }
}
