package green.bunny

import spock.lang.Specification

class IntegrationSpec extends Specification {
  def Connection conn

  def cleanup() {
    if (conn != null && conn.isOpen) {
      conn.close()
    }
    conn = null
  }
}
