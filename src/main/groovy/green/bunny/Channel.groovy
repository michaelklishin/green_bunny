package green.bunny

class Channel {
  def com.rabbitmq.client.Channel delegate

  Channel(com.rabbitmq.client.Channel delegate) {
    this.delegate = delegate
  }

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

  def int getNumber() {
    delegate.channelNumber
  }
}
