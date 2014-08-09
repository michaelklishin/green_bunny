package green.bunny

import com.rabbitmq.client.AMQP.Queue.DeclareOk as DeclareOk
import com.rabbitmq.client.AMQP.Queue.DeleteOk  as DeleteOk

class Channel {
  def com.rabbitmq.client.Channel delegate

  Channel(com.rabbitmq.client.Channel delegate) {
    this.delegate = delegate
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

  //
  // Lower-level API
  //

  def String queueDeclare() {
    delegate.queueDeclare().queue
  }

  def DeclareOk queueDeclare(String name, boolean durable, boolean exclusive,
                             boolean autoDelete, Map<String, Object> arguments) {
    delegate.queueDeclare(name, durable, exclusive, autoDelete, arguments)
  }

  def DeclareOk queueDeclarePassive(String name) {
    delegate.queueDeclarePassive(name)
  }

  def DeleteOk queueDelete(String name) {
    delegate.queueDelete(name)
  }
}
