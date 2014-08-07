package green.bunny

import spock.lang.Specification

class ConnectionSpec extends Specification {
  def Connection conn

  def cleanup() {
    if(conn != null && conn.isOpen) {
      conn.close()
    }
    conn = null
  }

  def "connecting with all defaults"() {
    given: "all default connection parameters"
    when: "client connects"
    conn = Bunny.connect()

    then: "connection succeeds"
    conn.isOpen
  }

  def "connecting to localhost with valid credentials"() {
    given: "valid credentials to a localhost node"
    def u = "green_bunny"
    def p = "green_bunny_password"

    when: "client connects"
    conn = Bunny.connect(["username": u, "password": p, "vhost": "bunny_testbed"])

    then: "connection succeeds"
    conn.isOpen
    !conn.isClosed
  }

  def "closing a connection"() {
    given: "an open connection"
    def conn = Bunny.connect()

    when: "client closes connection"
    conn.close()

    then: "the connection is no longer open"
    !conn.isOpen
    conn.isClosed
  }
}
