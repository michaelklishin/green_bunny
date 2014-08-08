package green.bunny

import spock.lang.Specification

class IntegrationSpec extends Specification {
  def Connection conn

  def setup() {
    conn = connect()
  }

  protected Connection connect() {
    GreenBunny.connect()
  }

  def cleanup() {
    if (conn != null && conn.isOpen) {
      conn.close()
    }
    conn = null
  }
}
