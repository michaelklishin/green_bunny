package green.bunny

import com.rabbitmq.client.AMQP.Queue.DeclareOk    as QDeclareOk
import com.rabbitmq.client.AMQP.Queue.DeleteOk     as QDeleteOk
import com.rabbitmq.client.AMQP.Exchange.DeclareOk as EDeclareOk
import com.rabbitmq.client.AMQP.Exchange.DeleteOk  as EDeleteOk

class Channel {
  protected com.rabbitmq.client.Channel delegate
  protected Exchange defaultExchange

  Channel(com.rabbitmq.client.Channel delegate) {
    this.delegate = delegate

    this.defaultExchange = exchange("", "direct", durable: true, autoDelete: false)
  }

  //
  // Open, close
  //

  def boolean isOpen() {
    delegate.isOpen()
  }
  def boolean getIsOpen() {
    isOpen()
  }

  def close() {
    delegate.close()
  }

  def boolean isClosed() {
    !isOpen()
  }
  def boolean getIsClosed() {
    isClosed()
  }

  //
  // Channel #
  //

  def int getNumber() {
    delegate.channelNumber
  }

  //
  // High-level API
  //

  def Queue queue() {
    def q = new Queue(this)
    q.performDeclare()
    // TODO: caching, book keeping for recovery
    q
  }

  def Queue queue(String name) {
    queue([:], name)
  }

  def Queue queue(Map opts, String name) {
    def q = new Queue(this, name,
        (opts.get("durable")     ?: false) as boolean,
        (opts.get("exclusive")   ?: false) as boolean,
        (opts.get("autoDelete")  ?: false) as boolean,
        (opts.get("arguments")   ?: [:]) as Map<String, Object>)
    q.performDeclare()
    // TODO: caching, book keeping for recovery
    q
  }

  def Exchange exchange(Map opts, String name, String type) {
    Exchange.validateType(type)

    def e = new Exchange(this, name, type,
      (opts.get("durable")     ?: false) as boolean,
      (opts.get("autoDelete")  ?: false) as boolean,
      (opts.get("arguments")   ?: [:]) as Map<String, Object>)
    e.maybePerformDeclare()
    // TODO: caching, book keeping for recovery
    e
  }

  def defaultExchange() {
    this.defaultExchange
  }

  def Exchange fanout(String name) {
    fanout([:], name)
  }

  def Exchange fanout(Map opts, String name) {
    exchange(opts, name, "fanout")
  }

  def Exchange topic(String name) {
    topic([:], name)
  }

  def Exchange topic(Map opts, String name) {
    exchange(opts, name, "topic")
  }

  def Exchange direct(String name) {
    if(name.isEmpty()) {
      defaultExchange
    } else {
      direct([:], name)
    }
  }

  def Exchange direct(Map opts, String name) {
    exchange(opts, name, "direct")
  }

  def Exchange headers(Map opts, String name) {
    exchange(opts, name, "headers")
  }

  //
  // Lower-level API
  //

  def String queueDeclare() {
    delegate.queueDeclare().queue
  }

  def QDeclareOk queueDeclare(String name, boolean durable, boolean exclusive,
                             boolean autoDelete, Map<String, Object> arguments) {
    delegate.queueDeclare(name, durable, exclusive, autoDelete, arguments)
  }

  def QDeclareOk queueDeclarePassive(String name) {
    delegate.queueDeclarePassive(name)
  }

  def QDeleteOk queueDelete(String name) {
    delegate.queueDelete(name)
  }

  def EDeclareOk exchangeDeclare(String name, String type, boolean durable,
                                 boolean autoDelete, Map<String, Object> arguments) {
    delegate.exchangeDeclare(name, type, durable, autoDelete, arguments)
  }

  def EDeleteOk exchangeDelete(String name) {
    delegate.exchangeDelete(name)
  }

  def EDeclareOk exchangeDeclarePassive(String name) {
    delegate.exchangeDeclarePassive(name)
  }
}
