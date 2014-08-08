package green.bunny

import com.rabbitmq.client.AuthenticationFailureException

import java.security.SecureRandom

class ConnectionSpec extends IntegrationSpec {
  @Override
  def Connection connect() {
    // no-op in this spec, return null to silence
    // warnings from groovyc
    null
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

  def "connecting to localhost with invalid credentials"() {
    given: "invalid credentials to a localhost node"
    def u = UUID.randomUUID().toString()
    def p = UUID.randomUUID().toString()

    when: "client connects"
    conn = GreenBunny.connect(["username": u, "password": p, "vhost": "bunny_testbed"])

    then: "connection fails"
    thrown(AuthenticationFailureException)
    conn == null
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

  def "connecting with the wrong hostname"() {
    given: "a hostname that does not resolve"
    def s = UUID.randomUUID().toString()

    when: "client connects"
    conn = GreenBunny.connect(["hostname": s])

    then: "connection fails"
    thrown(IOException)
  }

  def "connecting with the wrong port"() {
    given: "a port that is not bound to"
    int n = portInRange(10000, 60000)

    when: "client connects"
    conn = GreenBunny.connect(["port": n])

    then: "connection fails"
    thrown(IOException)
  }
}
