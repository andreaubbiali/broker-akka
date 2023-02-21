package it.brokerakka.brokerakka.akka_hello_world

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

/**
 * Note here we have a counter, we do not need  any guards such as synchronizer or atomicInteger
 * since the instance processes one message at a time
 */

class GreeterBot private constructor(context: ActorContext<Greeter.Greeted>, private val max: Int) :
    AbstractBehavior<Greeter.Greeted>(context) {

    private var counter = 0

    override fun createReceive(): Receive<Greeter.Greeted> {
        // Call [onGreeted] when receiving [Greeted] messages
        return newReceiveBuilder().onMessage(Greeter.Greeted::class.java, ::onGreeted).build()
    }

    private fun onGreeted(message: Greeter.Greeted): Behavior<Greeter.Greeted> {
        counter++
        context.log.info("Greeting $counter for ${message.whom}")
        if(counter == max) {
            // Stops the actor
            return Behaviors.stopped()
        } else {
            // Send a new message to the sender of the original message ([message.from])
            // Set [replyTo] as [context.self] so the other actor has a reference to this actor
            message.from.tell(Greeter.Greet(message.whom, context.self))
        }
        return this
    }

    companion object {
        // [Behaviors.setup] is a factory method to define the behavior of an actor
        fun create(max: Int): Behavior<Greeter.Greeted> = Behaviors.setup { GreeterBot(it, max) }
    }
}