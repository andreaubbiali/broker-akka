package it.brokerakka.brokerakka.amqp


import com.rabbitmq.client.*

private const val USERNAME = ""
private const val PASSWORD = ""
private const val QUEUE = "queueTest"

class ConsumerAQMP {

    private val factory = ConnectionFactory()
        .apply {
            username = USERNAME
            password = PASSWORD
            host = ConnectionFactory.DEFAULT_HOST
            virtualHost = ConnectionFactory.DEFAULT_VHOST
            port = ConnectionFactory.DEFAULT_AMQP_PORT
        }

    /**
     * Primary constructor
     */
    init {
        val channel: Channel = factory
            .newConnection()
            .createChannel()
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: ByteArray) {
                val message = String(body, charset("UTF-8"))
                println(" [x] Received '$message'")
                channel.basicAck(envelope.deliveryTag, false)
            }
        }
        channel.basicConsume(QUEUE, false, consumer)
    }

}