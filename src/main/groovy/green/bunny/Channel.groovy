package green.bunny

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AMQP.Queue.DeclareOk    as QDeclareOk
import com.rabbitmq.client.AMQP.Queue.DeleteOk     as QDeleteOk
import com.rabbitmq.client.AMQP.Exchange.DeclareOk as EDeclareOk
import com.rabbitmq.client.AMQP.Exchange.DeleteOk  as EDeleteOk
import com.rabbitmq.client.AMQP.Queue.BindOk       as QBindOk
import com.rabbitmq.client.AMQP.Exchange.BindOk    as EBindOk
import com.rabbitmq.client.ConfirmListener
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.GetResponse
import com.rabbitmq.client.RecoveryListener
import com.rabbitmq.client.ReturnListener
import com.rabbitmq.client.ShutdownListener
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection
import groovy.transform.TypeChecked

@TypeChecked
class Channel {
  public static final String DEFAULT_CHARSET = "UTF-8"
  public static final int PERSISTENT_DELIVERY_MODE = 2
  public static final int TRANSIENT_DELIVERY_MODE = 1

  //
  // Fields
  //

  protected com.rabbitmq.client.Channel delegate
  protected Exchange defaultExchange
  protected Connection connection

  Channel(Connection conn, com.rabbitmq.client.Channel delegate) {
    this.connection = conn
    this.delegate   = delegate

    this.defaultExchange = exchange("", "direct", durable: true, autoDelete: false)
  }

  //
  // Open, close
  //

  boolean isOpen() {
    delegate.isOpen()
  }
  boolean getIsOpen() {
    isOpen()
  }

  void close() {
    delegate.close()
  }

  boolean isClosed() {
    !isOpen()
  }
  boolean getIsClosed() {
    isClosed()
  }

  //
  // Channel #
  //

  int getNumber() {
    delegate.channelNumber
  }

  //
  // High-level API
  //

  Queue queue() {
    def q = new Queue(this)
    q.performDeclare()
    // TODO: caching, book keeping for recovery
    q
  }

  Queue queue(String name) {
    queue([:], name)
  }

  Queue queue(Map opts, String name) {
    def q = new Queue(this, name,
        (opts.get("durable")     ?: false) as boolean,
        (opts.get("exclusive")   ?: false) as boolean,
        (opts.get("autoDelete")  ?: false) as boolean,
        (opts.get("arguments")   ?: [:]) as Map<String, Object>)
    q.performDeclare()
    // TODO: caching, book keeping for recovery
    q
  }

  Exchange exchange(Map opts, String name, String type) {
    Exchange.validateType(type)

    def e = new Exchange(this, name, type,
      (opts.get("durable")     ?: false) as boolean,
      (opts.get("autoDelete")  ?: false) as boolean,
      (opts.get("arguments")   ?: [:]) as Map<String, Object>)
    e.maybePerformDeclare()
    // TODO: caching, book keeping for recovery
    e
  }

  Exchange defaultExchange() {
    this.defaultExchange
  }

  Exchange getDefaultExchange() {
    this.defaultExchange
  }

  Exchange fanout(String name) {
    fanout([:], name)
  }

  Exchange fanout(Map opts, String name) {
    exchange(opts, name, "fanout")
  }

  Exchange topic(String name) {
    topic([:], name)
  }

  Exchange topic(Map opts, String name) {
    exchange(opts, name, "topic")
  }

  Exchange direct(String name) {
    if(name.isEmpty()) {
      defaultExchange
    } else {
      direct([:], name)
    }
  }

  Exchange direct(Map opts, String name) {
    exchange(opts, name, "direct")
  }

  Exchange headers(Map opts, String name) {
    exchange(opts, name, "headers")
  }

  void confirmSelect() {
    delegate.confirmSelect()
  }

  boolean waitForConfirms() {
    delegate.waitForConfirms()
  }

  boolean waitForConfirms(long timeout) {
    delegate.waitForConfirms(timeout)
  }

  ReturnListener addReturnListener(Closure fn) {
    final listener = new ClosureDelegateReturnListener(fn)
    delegate.addReturnListener(listener)
    listener
  }

  void removeReturnListener(ReturnListener listener) {
    this.delegate.removeReturnListener(listener)
  }

  ConfirmListener addConfirmListener(Closure fn) {
    final listener = new ClosureDelegateConfirmListener(fn, Fn.noOpFn())
    delegate.addConfirmListener(listener)
    listener
  }

  ConfirmListener addConfirmListener(Closure onAck, Closure onNack) {
    final listener = new ClosureDelegateConfirmListener(onAck, onNack)
    delegate.addConfirmListener(listener)
    listener
  }

  ConfirmListener addConfirmListener(ConfirmListener listener) {
    delegate.addConfirmListener(listener)
    listener
  }

  void removeConfirmListener(ConfirmListener listener) {
    delegate.removeConfirmListener(listener)
  }

  ShutdownListener addShutdownListener(Closure fn) {
    final listener = new ClosureDelegateShutdownListener(fn)
    this.delegate.addShutdownListener(listener)

    listener
  }

  void removeShutdownListener(ShutdownListener listener) {
    this.delegate.removeShutdownListener(listener)
  }

  RecoveryListener addRecoveryListener(Closure fn) {
    if(this.connection.automaticRecoveryEnabled) {
      final listener = new ClosureDelegateRecoveryListener(fn)
      (this.delegate as AutorecoveringConnection).addRecoveryListener(listener)

      listener
    } else {
      null
    }
  }

  //
  // Lower-level API
  //

  String queueDeclare() {
    delegate.queueDeclare().queue
  }

  QDeclareOk queueDeclare(String name, boolean durable, boolean exclusive,
                             boolean autoDelete, Map<String, Object> arguments) {
    delegate.queueDeclare(name, durable, exclusive, autoDelete, arguments)
  }

  QDeclareOk queueDeclarePassive(String name) {
    delegate.queueDeclarePassive(name)
  }

  QDeleteOk queueDelete(String name) {
    delegate.queueDelete(name)
  }

  EDeclareOk exchangeDeclare(String name, String type, boolean durable,
                                 boolean autoDelete, Map<String, Object> arguments) {
    delegate.exchangeDeclare(name, type, durable, autoDelete, arguments)
  }

  EDeleteOk exchangeDelete(String name) {
    delegate.exchangeDelete(name)
  }

  EDeclareOk exchangeDeclarePassive(String name) {
    delegate.exchangeDeclarePassive(name)
  }

  String basicConsume(String q, boolean autoAck, String consumerTag,
                          boolean exclusive, Map<String, Object> arguments,
                          Consumer consumer) {
    delegate.basicConsume(q, autoAck, consumerTag, false,
        exclusive, arguments, consumer)
  }

  void basicCancel(String consumerTag) {
    delegate.basicCancel(consumerTag)
  }

  GetResponse basicGet(String q) {
    delegate.basicGet(q, true)
  }

  GetResponse basicGet(String q, boolean autoAck) {
    delegate.basicGet(q, autoAck)
  }

  void basicAck(long deliveryTag) {
    basicAck(deliveryTag, false)
  }

  void basicAck(long deliveryTag, boolean multiple) {
    delegate.basicAck(deliveryTag, multiple)
  }

  void basicReject(long deliveryTag, boolean requeue) {
    delegate.basicReject(deliveryTag, requeue)
  }

  void basicPublish(Map<String, Object> opts, String exchange, String payload) {
    basicPublish(opts, exchange, payload.getBytes(DEFAULT_CHARSET))
  }

  void basicPublish(Map<String, Object> opts, String exchange, byte[] payload) {
    delegate.basicPublish(exchange,
        routingKeyFrom(opts),
        opts.get("mandatory", false) as boolean,
        basicPropertiesFrom(opts),
        payload)
  }

  QBindOk queueBind(String q, String x, String routingKey) {
    delegate.queueBind(q, x, routingKey)
  }

  QBindOk queueBind(String q, String x, String routingKey, Map<String, Object> arguments) {
    delegate.queueBind(q, x, routingKey, arguments)
  }

  EBindOk exchangeBind(String destination, String source, String routingKey) {
    delegate.exchangeBind(destination, source, routingKey)
  }

  EBindOk exchangeBind(String destination, String source, String routingKey,
                           Map<String, Object> arguments) {
    delegate.exchangeBind(destination, source, routingKey, arguments)
  }

  AMQP.Queue.PurgeOk queuePurge(String q) {
    delegate.queuePurge(q)
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
