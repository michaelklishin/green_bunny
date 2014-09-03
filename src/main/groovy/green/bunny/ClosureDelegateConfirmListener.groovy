package green.bunny

import com.rabbitmq.client.ConfirmListener
import groovy.transform.TypeChecked

@TypeChecked
class ClosureDelegateConfirmListener implements ConfirmListener {
  final Closure<Void> onAck
  final Closure<Void> onNack

  ClosureDelegateConfirmListener(Closure<Void> onAck, Closure<Void> onNack) {
    this.onAck  = onAck
    this.onNack = onNack
  }

  @Override
  void handleAck(long deliveryTag, boolean multiple) throws IOException {
    this.onAck.call(deliveryTag, multiple)
  }

  @Override
  void handleNack(long deliveryTag, boolean multiple) throws IOException {
    this.onNack.call(deliveryTag, multiple)
  }
}
