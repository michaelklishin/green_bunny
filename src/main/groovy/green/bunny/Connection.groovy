package green.bunny

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.RecoveryListener
import com.rabbitmq.client.ShutdownListener
import com.rabbitmq.client.ShutdownSignalException
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection
import groovy.transform.TypeChecked

@TypeChecked
class Connection {
  def com.rabbitmq.client.Connection delegate
  def ConnectionFactory cf

  Connection(ConnectionFactory cf, com.rabbitmq.client.Connection delegate) {
    this.cf       = cf
    this.delegate = delegate
  }

  def createChannel() {
    def ch = delegate.createChannel()
    if(ch == null) {
      null
    } else {
      new Channel(ch)
    }
  }

  def createChannel(int n) {
    def ch = delegate.createChannel(n)
    if(ch == null) {
      null
    } else {
      new Channel(ch)
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

  def abort() {
    delegate.abort()
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

  def ShutdownListener addShutdownListener(Closure fn) {
    final listener = new ClosureDelegateShutdownListener(fn)
    this.delegate.addShutdownListener(listener)

    listener
  }

  def void removeShutdownListener(ShutdownListener listener) {
    this.delegate.removeShutdownListener(listener)
  }

  def RecoveryListener addRecoveryListener(Closure fn) {
    if(this.cf.automaticRecoveryEnabled) {
      final listener = new ClosureDelegateRecoveryListener(fn)
      (this.delegate as AutorecoveringConnection).addRecoveryListener(listener)

      listener
    } else {
      null
    }
  }
}
