package it.brokerakka.brokerakka.akka_hello_world

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

/**
 * Spawn the Greeter and GreeterBot and starts the interaction
 * This actor is the GUARDIAN ACTOR of the ActorSystem.
 */

class GreeterMain private constructor(context: ActorContext<Start>, private val greeter: ActorRef<Greeter.Greet>) :
    AbstractBehavior<GreeterMain.Start>(context) {

    data class Start(val name: String)

    override fun createReceive(): Receive<Start> {
        // Call [onStart] when receiving [Start] messages
        return newReceiveBuilder().onMessage(Start::class.java, ::onStart).build()
    }

    private fun onStart(command: Start): Behavior<Start> {
        // [ActorContext.spawn] creates a new [HelloWorldBot] actor
        val replyTo: ActorRef<Greeter.Greeted> = context.spawn(GreeterBot.create(3), command.name)
        // The new actor has a new message placed on its queue
        greeter.tell(Greeter.Greet(command.name, replyTo))
        return this
    }

    companion object {
        /**
         * [Behaviors.setup] is a factory method to define the behavior of an actor
         * [ActorContext.spawn] creates a new [Greeter] actor with the name "greeter"
         * The [Greeter] actor is created through [Greeter.create]
         */
        fun create(): Behavior<Start> = Behaviors.setup { context -> GreeterMain(context, context.spawn(Greeter.create(), "greeter")) }
    }
}