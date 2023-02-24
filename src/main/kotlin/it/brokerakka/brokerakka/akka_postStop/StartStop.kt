package it.brokerakka.brokerakka.akka_postStop

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import it.brokerakka.brokerakka.akka_actor_hierarchy.Main


internal class StartStopActor1 private constructor(context: ActorContext<String>) : AbstractBehavior<String>(context) {
    init {
        println("first started")
        context.spawn(StartStopActor2.create(), "second")
    }

    override fun createReceive(): Receive<String> {
        return newReceiveBuilder()
            .onMessageEquals("stop", Behaviors::stopped)
            .onSignal(PostStop::class.java) { signal -> onPostStop() }
            .build()
    }

    private fun onPostStop(): Behavior<String?> {
        println("first stopped")
        return this
    }

    companion object {
        fun create(): Behavior<String> {
            return Behaviors.setup { context: ActorContext<String> -> StartStopActor1(context) }
        }
    }
}

internal class StartStopActor2 private constructor(context: ActorContext<String>) : AbstractBehavior<String>(context) {
    init {
        println("second started")
    }

    override fun createReceive(): Receive<String> {
        return newReceiveBuilder().onSignal(PostStop::class.java) { signal -> onPostStop() }.build()
    }

    private fun onPostStop(): Behavior<String?> {
        println("second stopped")
        return this
    }

    companion object {
        fun create(): Behavior<String> {
            return Behaviors.setup { context: ActorContext<String> -> StartStopActor2(context) }
        }
    }
}

object StartStop {
    @JvmStatic
    fun main(args: Array<String>) {
        val testSystem: ActorRef<String> = ActorSystem.create(StartStopActor1.create(), "first")
        testSystem.tell("stop")
    }
}