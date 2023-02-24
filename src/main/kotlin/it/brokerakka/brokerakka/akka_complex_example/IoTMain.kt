package it.brokerakka.brokerakka.akka_complex_example

import akka.actor.typed.ActorSystem

object IoTMain {
    @JvmStatic
    fun main(args: Array<String>) {
        // Create ActorSystem and top level supervisor
        ActorSystem.create(IotSupervisor.create(), "iot-system")
    }
}