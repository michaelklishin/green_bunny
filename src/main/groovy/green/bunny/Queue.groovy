package green.bunny

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

  def boolean isServerNamed() {
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
  // Implementation
  //

  def performDeclare() {
    this.name = this.channel.queueDeclare(name, durable, exclusive, autoDelete, arguments).queue
    this.name
  }
}
