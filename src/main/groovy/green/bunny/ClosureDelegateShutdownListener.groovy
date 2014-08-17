package green.bunny

import com.rabbitmq.client.ShutdownListener
import com.rabbitmq.client.ShutdownSignalException

class ClosureDelegateShutdownListener implements ShutdownListener {
  final Closure fn;
  def ClosureDelegateShutdownListener(Closure fn) {
    this.fn = fn;
  }

  @Override
  void shutdownCompleted(ShutdownSignalException cause) {
    this.fn.call(cause)
  }
}
