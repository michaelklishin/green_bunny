package green.bunny

class Connection {
  def com.rabbitmq.client.Connection delegate

  Connection(com.rabbitmq.client.Connection delegate) {
    this.delegate = delegate
  }

  def createChannel() {
    def ch = delegate.createChannel()
    if(ch == null) {
      return null
    } else {
      return new Channel(ch)
    }
  }

  def Boolean isOpen() {
    return delegate.isOpen()
  }
  def Boolean getIsOpen() {
    return isOpen()
  }

  def close() {
    delegate.close()
  }

  def Boolean isClosed() {
    return !isOpen()
  }
  def Boolean getIsClosed() {
    return isClosed()
  }
}
