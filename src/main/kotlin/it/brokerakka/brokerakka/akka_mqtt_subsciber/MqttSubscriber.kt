package it.brokerakka.brokerakka.akka_mqtt_subsciber

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import it.brokerakka.brokerakka.akka_hello_world.Greeter
import it.brokerakka.brokerakka.mqtt.PublisherMQTT
import org.eclipse.paho.client.mqttv3.*

class MqttSubscriber private constructor(context: ActorContext<Start>, private val greeter: ActorRef<Greeter.Greet>) :
    AbstractBehavior<MqttSubscriber.Start>(context) {

    data class Start(val name: String)
    val mqttClient: MqttClient = MqttClient("tcp://test.mosquitto.org:1883", "prova")

    override fun createReceive(): Receive<Start> {
        // Call [onStart] when receiving [Start] messages
        return newReceiveBuilder().onMessage(Start::class.java, ::onStart).build()
    }

    private fun onStart(command: Start): Behavior<Start> {

        val options = MqttConnectOptions()
        mqttClient.connect(options)
        mqttClient.setCallback(Callback())
        mqttClient.subscribe("/prova", 1, PublisherMQTT.CallBack2())

        return this
    }

    companion object {
        /**
         * [Behaviors.setup] is a factory method to define the behavior of an actor
         * [ActorContext.spawn] creates a new [Greeter] actor with the name "greeter"
         * The [Greeter] actor is created through [Greeter.create]
         */
//        fun create(): Behavior<Start> = Behaviors.setup { context -> GreeterMain(context, context.spawn(Greeter.create(), "greeter")) }
    }
}

class Callback : MqttCallback{
    override fun connectionLost(p0: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun messageArrived(p0: String?, p1: MqttMessage?) {
        println("ARRIVA MESSAGGIOOOO: $p1")
    }

    override fun deliveryComplete(p0: IMqttDeliveryToken?) {
        TODO("Not yet implemented")
    }

}