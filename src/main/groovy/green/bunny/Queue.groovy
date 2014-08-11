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

  def String getName() {
    this.name
  }

  def boolean getIsServerNamed() {
    this.serverNamed
  }

  def Channel getChannel() {
    this.channel
  }

  def boolean getIsDurable() {
    this.durable
  }

  def boolean getIsExclusive() {
    this.exclusive
  }

  def boolean getIsAutoDelete() {
    this.autoDelete
  }

  def Map<String, Object> getArguments() {
    this.arguments
  }

  def long messageCount() {
    channel.queueDeclarePassive(this.name).messageCount
  }

  def long consumerCount() {
    channel.queueDeclarePassive(this.name).consumerCount
  }

  @Override
  def String toString() {
    "<" +
        "name = " + name +
        ", durable = " + isDurable.toString() +
        ", exclusive = " + isExclusive.toString() +
        ", autoDelete = " + isAutoDelete.toString() +
        ", arguments = " + arguments.toString() +
    ">"
  }

  //
  // Consumers
  //

  def String subscribeWith(Consumer consumer) {
    subscribeWith([:], consumer)
  }

  def String subscribeWith(Map<String, Object> opts, Consumer consumer) {
    // TODO: add consumer to the map of consumers
    this.channel.basicConsume(this.name,
        opts.get("autoAck", true) as boolean,
        opts.get("consumerTag", "") as String,
        opts.get("exclusive", false) as boolean,
        opts.get("arguments") as Map<String, Object>,
        consumer)
  }

  def subscribe(deliveryHandler) {

  }

  //
  // basic.get ("pull API")
  //

  def GetResponse get() {
    get(true)
  }
  def GetResponse get(boolean autoAck) {
    this.channel.basicGet(this.name, autoAck)
  }

  //
  // Bindings
  //

  def Queue bind(Exchange x) {
    this.channel.queueBind(this.name, x.name, "")
    this
  }

  def Queue bind(Map<String, Object> opts, Exchange x) {
    this.channel.queueBind(this.name, x.name,
        opts.get("routingKey") as String,
        opts.get("arguments") as Map<String, Object>)
    this
  }

  //
  // Purging
  //

  def Queue purge() {
    this.channel.queuePurge(this.name)
    this
  }

  def boolean getIsEmpty() {
    messageCount() == 0
  }

  //
  // Deletion
  //

  def void delete() {
    this.channel.queueDelete(this.name)
  }

  //
  // Publishing (uses default exchange)
  //

  def void publish(String payload) {
    this.channel.basicPublish(["routingKey": this.name] as Map<String, Object>, "", payload)
  }

  //
  // Implementation
  //

  def String performDeclare() {
    this.name = this.channel.queueDeclare(name, durable, exclusive, autoDelete, arguments).queue
    this.name
  }
}
