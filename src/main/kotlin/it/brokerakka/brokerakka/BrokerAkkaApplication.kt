package it.brokerakka.brokerakka

import akka.actor.typed.ActorSystem
import it.brokerakka.brokerakka.akka_hello_world.GreeterMain
import it.brokerakka.brokerakka.mqtt.PublisherMQTT
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.IOException

@SpringBootApplication
class BrokerAkkaApplication

fun main(args: Array<String>) {
	runApplication<BrokerAkkaApplication>(*args)

	/**
	 * Create the [GreeterMain] actor
	 */
	val greeterMain: ActorSystem<GreeterMain.Start> = ActorSystem.create(GreeterMain.create(), "hello-akka")
	greeterMain.tell(GreeterMain.Start("Charles"))

	try {
		println(">>> Press ENTER to exit <<<")
		System.`in`.read()
	} catch (ignored: IOException) {
	} finally {
		greeterMain.terminate()
	}


    val pub = PublisherMQTT()
    println("Publisher started!")
//    val cons = ConsumerAQMP()
//    println("Consumer started!")
}
