package it.brokerakka.brokerakka.akka_supervision_strategy

import akka.actor.typed.*
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import it.brokerakka.brokerakka.akka_postStop.StartStopActor1


internal class SupervisingActor private constructor(context: ActorContext<String>) :
    AbstractBehavior<String>(context) {
    private val child: ActorRef<String>

    init {
        child = context.spawn(
            Behaviors.supervise(SupervisedActor.create()).onFailure(SupervisorStrategy.restart()),
            "supervised-actor"
        )
    }

    override fun createReceive(): Receive<String> {
        return newReceiveBuilder().onMessageEquals("failChild") { onFailChild() }.build()
    }

    private fun onFailChild(): Behavior<String?> {
        child.tell("fail")
        return this
    }

    companion object {
        fun create(): Behavior<String> {
            return Behaviors.setup { context: ActorContext<String> -> SupervisingActor(context) }
        }
    }
}

internal class SupervisedActor private constructor(context: ActorContext<String>) : AbstractBehavior<String>(context) {
    init {
        println("supervised actor started")
    }

    override fun createReceive(): Receive<String> {
        return newReceiveBuilder()
            .onMessageEquals("fail") { fail() }
            .onSignal(PreRestart::class.java) { signal -> preRestart() }
            .onSignal(PostStop::class.java) { signal -> postStop() }
            .build()
    }

    private fun fail(): Behavior<String?> {
        println("supervised actor fails now")
        throw RuntimeException("I failed!")
    }

    private fun preRestart(): Behavior<String?> {
        println("supervised will be restarted")
        return this
    }

    private fun postStop(): Behavior<String?> {
        println("supervised stopped")
        return this
    }

    companion object {
        fun create(): Behavior<String> {
            return Behaviors.setup { context: ActorContext<String> -> SupervisedActor(context) }
        }
    }
}

object Supervising {
    @JvmStatic
    fun main(args: Array<String>) {
        val supervisingActor: ActorRef<String> = ActorSystem.create(SupervisingActor.create(), "supervising-actor")
        supervisingActor.tell("failChild")
    }
}