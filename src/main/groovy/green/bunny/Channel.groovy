package green.bunny

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AMQP.Queue.DeclareOk    as QDeclareOk
import com.rabbitmq.client.AMQP.Queue.DeleteOk     as QDeleteOk
import com.rabbitmq.client.AMQP.Exchange.DeclareOk as EDeclareOk
import com.rabbitmq.client.AMQP.Exchange.DeleteOk  as EDeleteOk
import com.rabbitmq.client.AMQP.Queue.BindOk       as QBindOk

import com.rabbitmq.client.Consumer

class Channel {
  public static final String DEFAULT_CHARSET = "UTF-8"
  public static final int PERSISTENT_DELIVERY_MODE = 2
  public static final int TRANSIENT_DELIVERY_MODE = 1

  //
  // Fields
  //

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

  def getDefaultExchange() {
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

  def void confirmSelect() {
    delegate.confirmSelect()
  }

  def boolean waitForConfirms() {
    delegate.waitForConfirms()
  }

  def boolean waitForConfirms(long timeout) {
    delegate.waitForConfirms(timeout)
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

  def String basicConsume(String q, boolean autoAck, String consumerTag,
                          boolean exclusive, Map<String, Object> arguments,
                          Consumer consumer) {
    delegate.basicConsume(q, autoAck, consumerTag, false,
        exclusive, arguments, consumer)
  }

  def void basicCancel(String consumerTag) {
    delegate.basicCancel(consumerTag)
  }

  def void basicAck(long deliveryTag) {
    basicAck(deliveryTag, false)
  }

  def void basicAck(long deliveryTag, boolean multiple) {
    delegate.basicAck(deliveryTag, multiple)
  }

  def void basicPublish(Map<String, Object> opts, String exchange, String payload) {
    basicPublish(opts, exchange, payload.getBytes(DEFAULT_CHARSET))
  }

  def void basicPublish(Map<String, Object> opts, String exchange, byte[] payload) {
    delegate.basicPublish(exchange, routingKeyFrom(opts), basicPropertiesFrom(opts), payload)
  }

  def QBindOk queueBind(String q, String x, String routingKey) {
    delegate.queueBind(q, x, routingKey)
  }

  def QBindOk queueBind(String q, String x, String routingKey, Map<String, Object> arguments) {
    delegate.queueBind(q, x, routingKey, arguments)
  }


  //
  // Implementation
  //

  AMQP.BasicProperties basicPropertiesFrom(Map<String, Object> opts) {
    def builder = new AMQP.BasicProperties.Builder()
    // TODO: use extension methods to add Map#getString()
    // TODO: can a bit of metaprogramming make this clearer? Worth investigating.
    builder.appId(opts.get("appId") as String)
    builder.clusterId(opts.get("clusterId") as String)
    builder.contentEncoding(opts.get("contentEncoding") as String)
    builder.correlationId(opts.get("correlationId") as String)
    builder.deliveryMode(deliveryModeFrom(opts))
    builder.expiration(opts.get("expiration") as String)
    builder.contentType(opts.get("contentType", "application/octet-stream") as String)
    builder.headers(opts.get("headers") as Map<String, Object>)
    builder.messageId(opts.get("messageId") as String)
    builder.replyTo(opts.get("replyTo") as String)
    builder.type(opts.get("type") as String)
    builder.priority(opts.get("priority") as Integer)
    builder.timestamp(timestampFrom(opts))

    builder.build()
  }

  protected String routingKeyFrom(Map opts) {
    opts.get("routingKey", "") as String
  }

  protected Integer deliveryModeFrom(Map<String, Object> opts) {
    if(opts.containsKey("deliveryMode")) {
      return opts.get("deliveryMode") as Integer
    }

    if(opts.containsKey("persistent")) {
      if(opts.get("persistent") as boolean) {
        return PERSISTENT_DELIVERY_MODE
      } else {
        return TRANSIENT_DELIVERY_MODE
      }
    }

    TRANSIENT_DELIVERY_MODE
  }

  protected Date timestampFrom(Map<String, Object> opts) {
    // TODO: support Joda Time types, e.g. DateTime
    opts.get("timestamp") as Date
  }
}
