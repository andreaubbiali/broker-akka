package it.brokerakka.brokerakka.mqtt

import org.eclipse.paho.client.mqttv3.*

class PublisherMQTT {

    val mqttClient: MqttClient = MqttClient("tcp://broker.emqx.io:1883", "prova")

    init {

        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                // no implementation
            }

            override fun connectionLost(cause: Throwable?) {
                println("MQTT client losed connection")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // no implementation
            }
        })

        val options = MqttConnectOptions()
        options.password = "dfgv".toCharArray()
        options.userName = ""
        options.isAutomaticReconnect = true

        try {
            mqttClient.connect(options)
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}
