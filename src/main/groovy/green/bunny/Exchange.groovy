package green.bunny

import groovy.transform.TypeChecked

@TypeChecked
class Exchange {
  public static Set<String> KNOWN_EXCHANGE_TYPES =
      ["direct", "fanout", "topic", "headers"].toSet().asImmutable()

  protected boolean durable
  protected boolean autoDelete

  protected Map<String, Object> arguments

  protected String type
  protected String name
  protected Channel channel

  //
  // Constructors
  //

  Exchange(Channel ch, String name, String type) {
    this(ch, type, name, false, false, [:])
  }

  Exchange(Channel ch, String name, String type,
           boolean durable, boolean autoDelete,
          Map<String, Object> arguments) {
    validateType(type)

    this.channel    = ch
    this.name       = name
    this.type       = type
    this.durable    = durable
    this.autoDelete = autoDelete
    this.arguments  = arguments
  }

  //
  // Properties
  //

  String getName() {
    this.name
  }

  boolean getIsPredefined() {
    this.name.isEmpty() || this.name.startsWith("amq.")
  }

  String getType() {
    this.type
  }

  Channel getChannel() {
    this.channel
  }

  boolean getIsDurable() {
    this.durable
  }

  boolean getIsAutoDelete() {
    this.autoDelete
  }

  Map<String, Object> getArguments() {
    this.arguments
  }

  //
  // Publishing
  //

  void publish(String payload) {
    publish([:], payload)
  }

  void publish(byte[] payload) {
    publish([:], payload)
  }

  void publish(Map<String, Object> opts, String payload) {
    this.channel.basicPublish(opts, this.name, payload)
  }

  void publish(Map<String, Object> opts, byte[] payload) {
    this.channel.basicPublish(opts, this.name, payload)
  }

  //
  // Binding
  //

  Exchange bind(Exchange source) {
    this.channel.exchangeBind(this.name, source.name, "")
    this
  }

  Exchange bind(Map<String, Object> opts, Exchange source) {
    this.channel.exchangeBind(this.name, source.name,
        opts.get("routingKey").toString(),
        opts.get("arguments") as Map<String, Object>)
    this
  }

  //
  // Deletion
  //

  void delete() {
    this.channel.exchangeDelete(this.name)
  }

  //
  // Implementation
  //

  @Override
  String toString() {
    "<type = $type, name = $name, durable = $isDurable, autoDelete = $isAutoDelete>"
  }

  void maybePerformDeclare() {
    if(!isPredefined) {
      this.channel.exchangeDeclare(name, type, durable, autoDelete, arguments)
    }
  }

  static validateType(String s) {
    if(!(KNOWN_EXCHANGE_TYPES.contains(s) ||
        s.startsWith("x-"))) {
      throw new IllegalArgumentException("Invalid exchange type: $s")
    }
  }
}
