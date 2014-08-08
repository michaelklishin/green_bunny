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
    conn = GreenBunny.connect()

    then: "connection succeeds"
    conn.isOpen
  }

  def "connecting to localhost with valid credentials"() {
    given: "valid credentials to a localhost node"
    def u = "green_bunny"
    def p = "green_bunny_password"

    when: "client connects"
    conn = GreenBunny.connect(["username": u, "password": p, "vhost": "bunny_testbed"])

    then: "connection succeeds"
    conn.isOpen
    !conn.isClosed
  }

  def "closing a connection"() {
    given: "an open connection"
    def conn = GreenBunny.connect()

    when: "client closes connection"
    conn.close()

    then: "the connection is no longer open"
    !conn.isOpen
    conn.isClosed
  }

  def "connecting with an overridden heartbeat interval"() {
    given: "heartbeat timeout of 10 seconds"
    def n = 10

    when: "client connects"
    conn = GreenBunny.connect(["requested_heartbeat": n])

    then: "connection succeeds"
    conn.isOpen
    conn.requestedHeartbeat == n
  }

  def "connecting with an overridden connection timeout"() {
    given: "connection timeout of 2 seconds"
    def n = 2

    when: "client connects"
    conn = GreenBunny.connect(["connection_timeout": n])

    then: "connection succeeds"
    conn.isOpen
    conn.connectionTimeout == n
  }
}
