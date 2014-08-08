package green.bunny

import com.rabbitmq.client.ConnectionFactory

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
    cf.port        = (map.get("port")    ?: ConnectionFactory.DEFAULT_AMQP_PORT) as Integer
    cf.username    = map.get("username") ?: ConnectionFactory.DEFAULT_USER
    cf.password    = map.get("password") ?: ConnectionFactory.DEFAULT_PASS
    cf.virtualHost = map.get("vhost")    ?: ConnectionFactory.DEFAULT_VHOST

    cf.requestedHeartbeat = (map.get("requested_heartbeat") ?: ConnectionFactory.DEFAULT_HEARTBEAT) as Integer
    cf.connectionTimeout  = (map.get("connection_timeout")  ?: ConnectionFactory.DEFAULT_CONNECTION_TIMEOUT) as Integer

    cf
  }
}
