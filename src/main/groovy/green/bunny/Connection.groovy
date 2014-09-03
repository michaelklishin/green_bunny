package green.bunny

import com.rabbitmq.client.BlockedListener
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.RecoveryListener
import com.rabbitmq.client.ShutdownListener
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection
import groovy.transform.TypeChecked

@TypeChecked
class Connection {
  com.rabbitmq.client.Connection delegate
  ConnectionFactory cf

  Connection(ConnectionFactory cf, com.rabbitmq.client.Connection delegate) {
    this.cf = cf
    this.delegate = delegate
  }

  Channel createChannel() {
    def ch = delegate.createChannel()
    if (ch == null) {
      null
    } else {
      new Channel(this, ch)
    }
  }

  def createChannel(int n) {
    def ch = delegate.createChannel(n)
    if (ch == null) {
      null
    } else {
      new Channel(this, ch)
    }
  }

  boolean isOpen() {
    delegate.isOpen()
  }

  boolean getIsOpen() {
    isOpen()
  }

  void close() {
    delegate.close()
  }

  void abort() {
    delegate.abort()
  }

  boolean isClosed() {
    !isOpen()
  }

  boolean getIsClosed() {
    isClosed()
  }

  boolean isAutomaticRecoveryEnabled() {
    cf.automaticRecoveryEnabled
  }

  boolean isTopologyRecoveryEnabled() {
    cf.topologyRecoveryEnabled
  }

  int getRequestedHeartbeat() {
    cf.requestedHeartbeat
  }

  int getConnectionTimeout() {
    cf.connectionTimeout
  }

  ShutdownListener addShutdownListener(Closure fn) {
    final listener = new ClosureDelegateShutdownListener(fn)
    this.delegate.addShutdownListener(listener)

    listener
  }

  void removeShutdownListener(ShutdownListener listener) {
    this.delegate.removeShutdownListener(listener)
  }

  RecoveryListener addRecoveryListener(Closure fn) {
    if (this.cf.automaticRecoveryEnabled) {
      final listener = new ClosureDelegateRecoveryListener(fn)
      (this.delegate as AutorecoveringConnection).addRecoveryListener(listener)

      listener
    } else {
      null
    }
  }

  void removeRecoveryListener(RecoveryListener listener) {
    if (cf.automaticRecoveryEnabled) {
      ((AutorecoveringConnection) this.delegate).removeRecoveryListener(listener)
    }
  }

  BlockedListener addBlockedListener(BlockedListener listener) {
    this.delegate.addBlockedListener(listener)
    listener
  }

  void removeBlockedListener(BlockedListener listener) {
    this.delegate.removeBlockedListener(listener)
  }

  BlockedListener addBlockedListener(Closure onBlocked, Closure onUnblocked) {
    final listener = new ClosureDelegateBlockedListener(onBlocked, onUnblocked)
    this.delegate.addBlockedListener(listener)
    listener
  }

  BlockedListener addBlockedListener(Closure fn) {
    final listener = new ClosureDelegateBlockedListener(fn, Fn.noOpFn())
    this.delegate.addBlockedListener(listener)
    listener
  }

  BlockedListener addUnblockedListener(Closure fn) {
    final listener = new ClosureDelegateBlockedListener(Fn.noOpFn(), fn)
    this.delegate.addBlockedListener(listener)
    listener
  }
}
