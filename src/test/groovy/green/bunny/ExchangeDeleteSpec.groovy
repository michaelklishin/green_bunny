package green.bunny

class ExchangeDeleteSpec extends IntegrationSpec {

  //
  // High-level API
  //

  def "deleting an exchange"() {
    given: "exchange"
    def x = ch.topic(randomExchangeName(), durable: false)

    when: "exchange is deleted"
    x.delete()

    then: "operation succeeds"
    ensureExchangeDeleted(x)
  }


  //
  // Lower-level API
  //

  def "deleting an exchange using exchangeDelete"() {
    given: "exchange"
    def x = ch.topic(randomExchangeName(), durable: false)

    when: "exchange is deleted"
    ch.exchangeDelete(x.name)

    then: "operation succeeds"
    ensureExchangeDeleted(x)
  }
}
