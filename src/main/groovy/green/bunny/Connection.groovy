package green.bunny

import com.rabbitmq.client.ConnectionFactory

class Connection {
  def com.rabbitmq.client.Connection delegate
  def com.rabbitmq.client.ConnectionFactory cf

  Connection(ConnectionFactory cf, com.rabbitmq.client.Connection delegate) {
    this.cf       = cf
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

  def int getRequestedHeartbeat() {
    cf.requestedHeartbeat
  }

  def int getConnectionTimeout() {
    cf.connectionTimeout
  }
}
