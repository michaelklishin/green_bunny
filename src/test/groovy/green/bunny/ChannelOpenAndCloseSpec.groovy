package green.bunny

class ChannelOpenAndCloseSpec extends IntegrationSpec {
  def "opening a channel with an automatically allocated number"() {
    when: "client opens a channel without explicitly providing a channel number"
    def ch = conn.createChannel()

    then: "operation succeeds"
    ch.isOpen
  }

  def "opening a channel with a provided channel number"() {
    when: "client opens a channel explicitly providing a channel number"
    def n = 1024
    def ch = conn.createChannel(n)

    then: "operation succeeds"
    ch.isOpen
    ch.number == n
  }
}
