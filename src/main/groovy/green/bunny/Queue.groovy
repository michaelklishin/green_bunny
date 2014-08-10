package green.bunny

import com.rabbitmq.client.Consumer

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

  //
  // Consumers
  //

  def String subscribeWith(Consumer consumer) {
    // TODO: add consumer to the map of consumers
    this.channel.basicConsume(this, consumer)
  }

  def subscribe(deliveryHandler) {

  }

  //
  // Deletion
  //

  def delete() {
    this.channel.queueDelete(this.name)
  }

  //
  // Publishing (uses default exchange)
  //

  def publish(String payload) {
    this.channel.defaultExchange.publish(payload, routingKey: this.name)
  }

  //
  // Implementation
  //

  def performDeclare() {
    this.name = this.channel.queueDeclare(name, durable, exclusive, autoDelete, arguments).queue
    this.name
  }
}
