package green.bunny

class Channel {
  def com.rabbitmq.client.Channel delegate

  Channel(com.rabbitmq.client.Channel delegate) {
    this.delegate = delegate
  }


}
