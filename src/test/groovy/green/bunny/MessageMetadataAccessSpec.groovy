package green.bunny

class MessageMetadataAccessSpec extends IntegrationSpec {
  def "accessing message metadata"() {
    given: "an exchange"
    def x = ch.fanout("x1")
    and: "a queue bound to it"
    def q = ch.queue().bind(x)

    when: "a message with metadata filled out is published"
    def dt = new Date()
    def headers = [
        "coordinates": ["latitude": 59.35, "longitude": 18.066667],
        "participants": 11,
        "venue": "Stockholm",
        "true_field": true,
        "false_field": false,
        "null_field": null,
        "ary_field": ["one", 2.0, 3, ["abc": 123], [1, 2, 3]]
    ]
    def contentType = "x-green-bunny/data"
    def appId = "green.bunny.tests"
    def clusterId = "a.cluster"
    def contentEncoding = "zip/zap"
    def type = "kinda.checkin"
    def replyTo = "r-1"
    def correlationId = "c-1"
    def messageId = "m-1"
    def priority = 8
    x.publish("hello",
      appId: appId,
      clusterId: clusterId,
      contentEncoding: contentEncoding,
      contentType: contentType,
      timestamp: dt,
      type: type,
      priority: priority,
      headers: headers,
      replyTo: replyTo,
      correlationId: correlationId,
      messageId: messageId)
    then: "that metadata is preserved and can be accessed by the consumer"
    def resp = q.get()
    "hello".equals(new String(resp.body, "UTF-8"))
    resp.envelope.deliveryTag == 1
    !resp.envelope.redeliver
    resp.envelope.routingKey.isEmpty()
    resp.envelope.exchange == x.name
    resp.props.contentType == contentType
    resp.props.contentEncoding == contentEncoding
    resp.props.replyTo == replyTo
    resp.props.correlationId == correlationId
    resp.props.messageId == messageId
    resp.props.priority == priority
    def format = "EEE MMM dd kk:mm:ss z yyyy"
    resp.props.timestamp.format(format) == dt.format(format)
    resp.props.appId == appId
    resp.props.clusterId == clusterId

    resp.props.headers.get("latitude") == headers.get("latitude")
    resp.props.headers.get("true_field")
    !resp.props.headers.get("false_field")
    resp.props.headers.get("null_field") == null
    ((resp.props.headers.get("ary_field") as List<Object>).get(3) as Map).get("abc") == 123

    cleanup:
    x.delete()
    q.delete()
  }
}
