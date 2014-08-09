package green.bunny

import spock.lang.Specification

import java.security.SecureRandom

abstract class IntegrationSpec extends Specification {
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

  void ensureDeclared(String q) {
    assert ch.queueDeclarePassive(q)
  }

  void ensureDeleted(Queue q) {
    ensureDeleted(q.name)
  }

  void ensureDeleted(String q) {
    def tmpCh = conn.createChannel()
    try {
      tmpCh.queueDeclarePassive(q)
      assert false
    } catch (IOException ignored) {
      // expected
    }
  }

  String randomQueueName() {
    UUID.randomUUID().toString()
  }
}
