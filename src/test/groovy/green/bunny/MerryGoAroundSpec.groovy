package green.bunny

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Envelope

import java.util.concurrent.CountDownLatch

/**
 * The message flow in this integration test is:
 *
 * x => qn => ... => q3 => q2 => q1 => xs (results)
 */
class MerryGoAroundSpec extends IntegrationSpec {
  @Override
  def setup() {
    // no-op
  }

  @Override
  def cleanup() {
    // no-op
  }

  def "4 stage ring topology of queues on 4 connections"() {
    given: "4 queues/consumers on 4 separate connections"
    final n = 50000
    def  xs = []

    def c1 = GreenBunny.connect()
    def c2 = GreenBunny.connect()
    def c3 = GreenBunny.connect()
    def c4 = GreenBunny.connect()

    final latch    = new CountDownLatch(n)
    final appender = { Channel ch, Envelope envelope,
                       AMQP.BasicProperties properties, byte[] body ->
      xs.add(new String(body, "UTF-8"))
      body
    }

    final ch1 = c1.createChannel()
    final q1  = ch1.queue()
    q1.subscribe({ byte[] body -> latch.countDown() } << appender)

    final ch2 = c2.createChannel()
    final q2  = ch2.queue()
    q2.subscribe(republishTo(q1))

    final ch3 = c3.createChannel()
    final q3  = ch3.queue()
    q3.subscribe(republishTo(q2))

    final ch4 = c4.createChannel()
    final q4  = ch4.queue()
    q4.subscribe(republishTo(q3))

    when: "a message is published to the beginning of pipeline"
    n.times { q4.publish("msg $it") }

    then: "it reaches the end"
    awaitOn(latch)
    xs.size() == n
    xs.last() == "msg ${n - 1}"

    cleanup:
    [c1, c2, c3, c4].each { it.close() }
  }
}
