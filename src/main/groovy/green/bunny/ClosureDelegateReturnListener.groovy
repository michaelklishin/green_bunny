package green.bunny

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ReturnListener
import groovy.transform.TypeChecked

@TypeChecked
class ClosureDelegateReturnListener implements ReturnListener {
  final Closure fn

  ClosureDelegateReturnListener(Closure fn) {
    this.fn = fn
  }

  @Override
  void handleReturn(int replyCode,
                    String replyText,
                    String exchange,
                    String routingKey,
                    AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
    this.fn.call(replyCode, replyText, exchange, routingKey, properties, body)
  }
}
