package it.brokerakka.brokerakka.akka_actor_hierarchy

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


internal class PrintMyActorRefActor private constructor(context: ActorContext<String>) :
    AbstractBehavior<String>(context) {

        override fun createReceive(): Receive<String?>? {
            return newReceiveBuilder().onMessageEquals("printit") { printIt() }.build()
        }

        private fun printIt(): Behavior<String?> {
            val secondRef: ActorRef<String> = context.spawn(Behaviors.empty(), "second-actor")
            println("Second: $secondRef")
            return this
        }

        companion object {
            fun create(): Behavior<String> {
                return Behaviors.setup { context: ActorContext<String> -> PrintMyActorRefActor(context) }
            }
        }
}

internal class Main private constructor(context: ActorContext<String>) : AbstractBehavior<String>(context) {

    override fun createReceive(): Receive<String?>? {
        return newReceiveBuilder().onMessageEquals("start") { start() }.build()
    }

    private fun start(): Behavior<String?> {
        val firstRef: ActorRef<String> = context.spawn(PrintMyActorRefActor.create(), "first-actor")
        println("First: $firstRef")
        firstRef.tell("printit")
        return Behaviors.same()
    }

    companion object {
        fun create(): Behavior<String> {
            return Behaviors.setup { context: ActorContext<String> -> Main(context) }
        }
    }
}

object ActorHierarchyExperiments {
    @JvmStatic
    fun main(args: Array<String>) {
        val testSystem: ActorRef<String> = ActorSystem.create(Main.create(), "testSystem")
        testSystem.tell("start")
    }
}