package it.brokerakka.brokerakka.akka_complex_example

import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive


class IotSupervisor private constructor(context: ActorContext<Void>) :
    AbstractBehavior<Void>(context) {

    init {
        // log using akka logger
        context.log.info("IoT Application started")
    }

    // No need to handle any messages
    override fun createReceive(): Receive<Void> {
        return newReceiveBuilder().onSignal(
            PostStop::class.java
        ) { _: PostStop? -> onPostStop() }.build()
    }

    private fun onPostStop(): IotSupervisor {
        context.log.info("IoT Application stopped")
        return this
    }

    companion object {
        fun create(): Behavior<Void> {
            return Behaviors.setup { context: ActorContext<Void> ->
                IotSupervisor(
                    context
                )
            }
        }
    }
}

