package green.bunny

import com.rabbitmq.client.BlockedListener
import groovy.transform.TypeChecked

@TypeChecked
class ClosureDelegateBlockedListener implements BlockedListener {
  final Closure onBlocked
  final Closure onUnblocked

  ClosureDelegateBlockedListener(Closure onBlocked, Closure onUnblocked) {
    this.onBlocked   = onBlocked
    this.onUnblocked = onUnblocked
  }

  @Override
  void handleBlocked(String reason) throws IOException {
    this.onBlocked.call(reason)
  }

  @Override
  void handleUnblocked() throws IOException {
    this.onUnblocked.call()
  }
}
