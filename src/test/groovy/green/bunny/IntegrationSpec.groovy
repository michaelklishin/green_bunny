package green.bunny

import spock.lang.Specification

import java.security.SecureRandom

class IntegrationSpec extends Specification {
  def Connection conn
  def Channel ch

  def setup() {
    conn = connect()
    ch   = openChannel()
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

  protected int portInRange(int min, int max) {
    // port range
    def sr = new SecureRandom()
    def n = sr.nextInt(max - min) + min
    n
  }
}
