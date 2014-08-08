package green.bunny

import com.rabbitmq.client.ConnectionFactory

import javax.net.ssl.SSLContext

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

    cf.host        = map.get("hostname") ?: (map.get("host") ?: ConnectionFactory.DEFAULT_HOST)
    cf.port        = (map.get("port")    ?: ConnectionFactory.DEFAULT_AMQP_PORT) as int
    cf.username    = map.get("username") ?: ConnectionFactory.DEFAULT_USER
    cf.password    = map.get("password") ?: ConnectionFactory.DEFAULT_PASS
    cf.virtualHost = map.get("vhost")    ?: ConnectionFactory.DEFAULT_VHOST

    cf = maybeEnableTLS(cf, map)

    cf.requestedHeartbeat = (map.get("requested_heartbeat") ?: ConnectionFactory.DEFAULT_HEARTBEAT) as Integer
    cf.connectionTimeout  = (map.get("connection_timeout")  ?: ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT) as Integer

    cf
  }

  static def maybeEnableTLS(ConnectionFactory cf, def map) {
    if(map.get("tls")) {
      cf.useSslProtocol()
    }

    def tls_protocol = map.get("tls_protocol") as String
    if(tls_protocol != null) {
      cf.useSslProtocol(tls_protocol)
    }

    def tls_context = map.get("tls_context") as SSLContext
    if(tls_context != null) {
      cf.useSslProtocol(tls_context)
    }

    cf
  }
}
