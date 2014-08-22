package green.bunny

import com.rabbitmq.client.BlockedListener
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
      new Channel(this, ch)
    }
  }

  def createChannel(int n) {
    def ch = delegate.createChannel(n)
    if(ch == null) {
      null
    } else {
      new Channel(this, ch)
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

  def boolean isAutomaticRecoveryEnabled() {
    cf.automaticRecoveryEnabled
  }

  def boolean isTopologyRecoveryEnabled() {
    cf.topologyRecoveryEnabled
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

  def void removeRecoveryListener(RecoveryListener listener) {
    if(cf.automaticRecoveryEnabled) {
      ((AutorecoveringConnection)this.delegate).removeRecoveryListener(listener)
    }
  }

  def BlockedListener addBlockedListener(BlockedListener listener) {
    this.delegate.addBlockedListener(listener)
    listener
  }

  def void removeBlockedListener(BlockedListener listener) {
    this.delegate.removeBlockedListener(listener)
  }

  def BlockedListener addBlockedListener(Closure onBlocked, Closure onUnblocked) {
    final listener = new ClosureDelegateBlockedListener(onBlocked, onUnblocked)
    this.delegate.addBlockedListener(listener)
    listener
  }

  def BlockedListener addBlockedListener(Closure fn) {
    final listener = new ClosureDelegateBlockedListener(fn, Fn.noOpFn())
    this.delegate.addBlockedListener(listener)
    listener
  }

  def BlockedListener addUnblockedListener(Closure fn) {
    final listener = new ClosureDelegateBlockedListener(Fn.noOpFn(), fn)
    this.delegate.addBlockedListener(listener)
    listener
  }
}
