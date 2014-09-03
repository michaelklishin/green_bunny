package green.bunny

import com.rabbitmq.client.Consumer
import com.rabbitmq.client.GetResponse
import groovy.transform.TypeChecked

@TypeChecked
class Queue {
  public static final String SERVER_NAMED = ""

  protected boolean durable
  protected boolean autoDelete
  protected boolean exclusive
  protected Map<String, Object> arguments

  protected String name
  protected Channel channel
  protected boolean serverNamed

  //
  // Constructors
  //

  Queue(Channel ch) {
    this(ch, SERVER_NAMED,
         false, false, false, [:])
  }

  Queue(Channel ch, String name) {
    this(ch, name, false, false, false, [:])
  }

  Queue(Channel ch, String name,
        boolean durable, boolean exclusive, boolean autoDelete,
        Map<String, Object> arguments) {
    this.channel     = ch
    this.name        = name
    this.serverNamed = name.isEmpty()

    this.durable     = durable
    this.exclusive   = exclusive
    this.autoDelete  = autoDelete
    this.arguments   = arguments
  }

  //
  // Properties
  //

  String getName() {
    this.name
  }

  boolean getIsServerNamed() {
    this.serverNamed
  }

  Channel getChannel() {
    this.channel
  }

  boolean getIsDurable() {
    this.durable
  }

  boolean getIsExclusive() {
    this.exclusive
  }

  boolean getIsAutoDelete() {
    this.autoDelete
  }

  Map<String, Object> getArguments() {
    this.arguments
  }

  long messageCount() {
    channel.queueDeclarePassive(this.name).messageCount
  }

  long consumerCount() {
    channel.queueDeclarePassive(this.name).consumerCount
  }

  @Override
  String toString() {
    "<name = $name, durable = $isDurable, exclusive = $isExclusive, autoDelete = $isAutoDelete, arguments = $arguments>"
  }

  //
  // Consumers
  //

  String subscribeWith(Consumer consumer) {
    subscribeWith([:], consumer)
  }

  String subscribeWith(Map<String, Object> opts, Consumer consumer) {
    // TODO: add consumer to the map of consumers
    this.channel.basicConsume(this.name,
        opts.get("autoAck", true) as boolean,
        opts.get("consumerTag", "") as String,
        opts.get("exclusive", false) as boolean,
        opts.get("arguments") as Map<String, Object>,
        consumer)
  }

  ClosureDelegateConsumer subscribe(Closure deliveryHandler) {
    def cons = new ClosureDelegateConsumer(this.channel, deliveryHandler)
    subscribeWith(cons)
    cons
  }

  ClosureDelegateConsumer subscribe(Map<String, Object> opts, Closure deliveryHandler) {
    def cons = new ClosureDelegateConsumer(this.channel, deliveryHandler)
    subscribeWith(opts, cons)
    cons
  }

  ClosureDelegateConsumer subscribe(Map<String, Object> opts, Closure deliveryHandler, Closure cancelHandler) {
    def cons = new ClosureDelegateConsumer(this.channel, deliveryHandler, cancelHandler)
    subscribeWith(opts, cons)
    cons
  }

  //
  // basic.get ("pull API")
  //

  GetResponse get() {
    get(true)
  }
  GetResponse get(boolean autoAck) {
    this.channel.basicGet(this.name, autoAck)
  }

  //
  // Bindings
  //

  Queue bind(Exchange x) {
    this.channel.queueBind(this.name, x.name, "")
    this
  }

  Queue bind(Map<String, Object> opts, Exchange x) {
    this.channel.queueBind(this.name, x.name,
        opts.get("routingKey") as String,
        opts.get("arguments") as Map<String, Object>)
    this
  }

  //
  // Purging
  //

  Queue purge() {
    this.channel.queuePurge(this.name)
    this
  }

  boolean getIsEmpty() {
    messageCount() == 0
  }

  //
  // Deletion
  //

  void delete() {
    this.channel.queueDelete(this.name)
  }

  //
  // Publishing (uses default exchange)
  //

  void publish(String payload) {
    publish(payload.getBytes("UTF-8"))
  }

  void publish(byte[] payload) {
    this.channel.basicPublish(["routingKey": this.name] as Map<String, Object>, "", payload)
  }

  //
  // Implementation
  //

  String performDeclare() {
    this.name = this.channel.queueDeclare(name, durable, exclusive, autoDelete, arguments).queue
    this.name
  }
}
