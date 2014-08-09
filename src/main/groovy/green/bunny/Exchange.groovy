package green.bunny

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

  def String getName() {
    this.name
  }

  def boolean getIsPredefined() {
    this.name.isEmpty() || this.name.startsWith("amq.")
  }

  def boolean getType() {
    this.type
  }

  def Channel getChannel() {
    this.channel
  }

  def boolean getIsDurable() {
    this.durable
  }

  def boolean getIsAutoDelete() {
    this.autoDelete
  }

  def Map<String, Object> getArguments() {
    this.arguments
  }

  //
  // Deletion
  //

  def delete() {
    this.channel.exchangeDelete(this.name)
  }

  //
  // Implementation
  //

  def maybePerformDeclare() {
    if(!isPredefined) {
      this.channel.exchangeDeclare(name, type, durable, autoDelete, arguments)
    }
  }

  static validateType(String s) {
    if(!(KNOWN_EXCHANGE_TYPES.contains(s) ||
        s.startsWith("x-"))) {
      throw new IllegalArgumentException("Invalid exchange type: " + s.toString())
    }
  }
}
