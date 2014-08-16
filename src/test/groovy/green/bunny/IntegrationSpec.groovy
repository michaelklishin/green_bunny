package green.bunny

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Envelope
import spock.lang.Specification

import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class IntegrationSpec extends Specification {
  public static final int STANDARD_WAITING_PERIOD = 3
  def Connection conn
  def Channel ch

  def setup() {
    conn = connect()
    ch = openChannel()
  }

  protected Connection connect() {
    GreenBunny.connect()
  }

  protected Channel openChannel() {
    conn.createChannel()
  }

  def cleanup() {
    if (conn != null && conn.isOpen) {
      conn.close()
    }
    conn = null
  }

  //
  // Helpers
  //

  protected int portInRange(int min, int max) {
    // port range
    def sr = new SecureRandom()
    def n = sr.nextInt(max - min) + min
    n
  }

  //
  // Matchers
  //

  void ensureServerNamed(Queue q) {
    assert q.name =~ /^amq\./
    assert q.isServerNamed
  }

  void ensureServerNamed(String q) {
    assert q =~ /^amq\./
  }

  void ensureClientNamed(Queue q) {
    assert !(q.name =~ /^amq\./)
    assert !q.isServerNamed
  }

  void ensureDeclared(Channel ch, Queue q) {
    assert q.channel == ch
    assert ch.queueDeclarePassive(q.name)
  }

  void ensureDeclared(Channel ch, Exchange e) {
    assert e.channel == ch
    assert ch.exchangeDeclarePassive(e.name)
  }

  void ensureDeclared(String q) {
    assert ch.queueDeclarePassive(q)
  }

  void ensureQueueDeleted(Queue q) {
    ensureQueueDeleted(q.name)
  }

  void ensureQueueDeleted(String q) {
    def tmpCh = conn.createChannel()
    try {
      tmpCh.queueDeclarePassive(q)
      assert false
    } catch (IOException ignored) {
      // expected
    }
  }

  void ensureExchangeDeleted(Exchange x) {
    ensureExchangeDeleted(x.name)
  }

  void ensureExchangeDeleted(String x) {
    def tmpCh = conn.createChannel()
    try {
      tmpCh.exchangeDeclarePassive(x)
      assert false
    } catch (IOException ignored) {
      // expected
    }
  }

  String randomQueueName() {
    "q" + UUID.randomUUID().toString()
  }

  String randomExchangeName() {
    "x" + UUID.randomUUID().toString()
  }

  def boolean awaitOn(CountDownLatch latch) {
    latch.await(STANDARD_WAITING_PERIOD, TimeUnit.SECONDS)
  }

  def republishTo(Queue q) {
    return { Channel ch, Envelope envelope,
             AMQP.BasicProperties properties, byte[] body ->
      q.publish(body)
    }
  }
}
