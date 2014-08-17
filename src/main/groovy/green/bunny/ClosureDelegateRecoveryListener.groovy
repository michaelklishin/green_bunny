package green.bunny

import com.rabbitmq.client.Recoverable
import com.rabbitmq.client.RecoveryListener

class ClosureDelegateRecoveryListener implements RecoveryListener {
  final Closure fn

  def ClosureDelegateRecoveryListener(Closure fn) {
    this.fn = fn
  }

  @Override
  void handleRecovery(Recoverable recoverable) {
    this.fn.call(recoverable)
  }
}
