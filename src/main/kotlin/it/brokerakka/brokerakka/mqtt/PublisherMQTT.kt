package it.brokerakka.brokerakka.mqtt

import org.eclipse.paho.client.mqttv3.*

class PublisherMQTT {

    val mqttClient: MqttClient = MqttClient("tcp://test.mosquitto.org:1883", "prova")

    init {

        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                println("PRIMA CALLBACK")
            }

            override fun connectionLost(cause: Throwable?) {
                println("MQTT client losed connection")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // no implementation
            }
        })

        val options = MqttConnectOptions()
//        options.password = "dfgv".toCharArray()
//        options.userName = ""
        options.isAutomaticReconnect = true

        try {
            mqttClient.connect(options)
            mqttClient.subscribe("/prova", 1, CallBack2())
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        val msg = MqttMessage("ciao".toByteArray())

        mqttClient.publish("/prova", msg)

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

    class CallBack2 : IMqttMessageListener {
        override fun messageArrived(topic: String?, message: MqttMessage?) {
            println("MESSAGGIO ARRIVATO 2")
        }

    }

}
