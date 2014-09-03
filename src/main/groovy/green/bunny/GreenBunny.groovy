package green.bunny

import com.rabbitmq.client.ConnectionFactory
import groovy.transform.TypeChecked

import javax.net.ssl.SSLContext

@TypeChecked
class GreenBunny {
  static Connection connect() {
    def cf = new ConnectionFactory()
    new Connection(cf, cf.newConnection())
  }

  static Connection connect(Map opts) {
    def cf = connectionFactoryFor(opts)

    new Connection(cf, cf.newConnection())
  }

  static ConnectionFactory connectionFactoryFor(Map map) {
    def ConnectionFactory cf = new ConnectionFactory()

    cf.host        = map.hostname ?: (map.host ?: ConnectionFactory.DEFAULT_HOST)
    cf.port        = (map.port    ?: ConnectionFactory.DEFAULT_AMQP_PORT) as int
    cf.username    = map.username ?: ConnectionFactory.DEFAULT_USER
    cf.password    = map.password ?: ConnectionFactory.DEFAULT_PASS
    cf.virtualHost = map.vhost    ?: ConnectionFactory.DEFAULT_VHOST

    cf = maybeEnableTLS(cf, map)

    cf.requestedHeartbeat       = map.get("requested_heartbeat", ConnectionFactory.DEFAULT_HEARTBEAT) as Integer
    cf.connectionTimeout        = map.get("connection_timeout", ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT) as Integer

    cf.automaticRecoveryEnabled = map.get("automaticallyRecover", true) as boolean
    cf.topologyRecoveryEnabled  = map.get("recoverTopology", true) as boolean
    cf.networkRecoveryInterval  = map.get("networkRecoveryInterval", 5000) as int

    cf
  }

  static ConnectionFactory maybeEnableTLS(ConnectionFactory cf, Map<String, Object> map) {
    if(map.get("tls")) {
      cf.useSslProtocol()
    }

    def tls_protocol = map.get("tls_protocol") as String
    if(tls_protocol) {
      cf.useSslProtocol(tls_protocol)
    }

    def tls_context = map.get("tls_context") as SSLContext
    if(tls_context) {
      cf.useSslProtocol(tls_context)
    }

    cf
  }
}
