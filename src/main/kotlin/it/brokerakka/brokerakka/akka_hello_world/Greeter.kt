package it.brokerakka.brokerakka.akka_hello_world


import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

/**
 * Are defined two message types:
 * - Greet: to greet someone
 * - Greeted: to confirm that it has done
 */

/**
 * This behavior only accept messages of class Greeter
 * Usually is used an interface that specify all the message it can accept
 */
class Greeter private constructor(context: ActorContext<Greet>) : AbstractBehavior<Greeter.Greet>(context){

    /**
     * @param whom who to greet
     * @param replyTo the actor who requested to send the greet
     */
    data class Greet(val whom: String, val replyTo: ActorRef<Greeted>)

    /**
     * @param whom who to send the message that say that the greet has been sent
     * @param from myself
     */
    data class Greeted(val whom: String, val from: ActorRef<Greet>)

    companion object {
        fun create(): Behavior<Greet> = Behaviors.setup(::Greeter)
    }

    override fun createReceive(): Receive<Greet> {
        return newReceiveBuilder().onMessage(Greet::class.java, ::onGreet).build()
    }

    private fun onGreet(command: Greet): Behavior<Greet> {
        context.log.info("Hello ${command.whom}!")
        command.replyTo.tell(Greeted(command.whom, context.self))
        return this
    }
}