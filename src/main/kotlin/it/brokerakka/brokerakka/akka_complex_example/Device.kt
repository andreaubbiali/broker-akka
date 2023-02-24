package it.brokerakka.brokerakka.akka_complex_example

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.japi.function.Function
import it.brokerakka.brokerakka.akka_complex_example.Device.RecordTemperature
import java.util.*


/**
 * Accepted messages are ones that implements Command
 * Akka doesn't provide any mechanism to acknowledge the sender that the message is arrived and has been processed.
 * You need to implement it. Here we implemented when arrive the [RecordTemperature] message we will send the ack to
 * the sender after the temperature has been saved.
 */

class Device private constructor(
    private val context: ActorContext<Device.Command>,
    private val groupId: String,
    private val deviceId: String): AbstractBehavior<Device.Command>(context){

    private var lastTemperatureReading: Optional<Double> = Optional.empty()

    init {
        context.log.info("Device actor {}-{} started", groupId, deviceId)
    }

    interface Command

    // Received messages
    data class ReadTemperature(val requestId: Long, val replyTo: ActorRef<RespondTemperature>) : Command
    data class RecordTemperature(val requestId: Long, val replyTo: ActorRef<TemperatureRecorded>, val value: Double) : Command
    // Sent messages (in fact doesn't implement Command)
    data class RespondTemperature(val requestID: Long, val value: Optional<Double>)
    data class TemperatureRecorded(val requestID: Long)

    // Used for tests purposes
    internal enum class Passivate : Command {
        INSTANCE
    }

    /**
     * Defines how to construct the Behavior for the Device actor
     */
    companion object {
        fun create(groupId: String, deviceId: String): Behavior<Command> {
            return Behaviors.setup { context -> Device(context, groupId, deviceId) }
        }
    }

    override fun createReceive(): Receive<Command> {
        return newReceiveBuilder()
            .onMessage(
                ReadTemperature::class.java,
                Function { r: ReadTemperature ->
                    onReadTemperature(
                        r
                    )
                })
            .onMessage(
                RecordTemperature::class.java,
                Function { r: RecordTemperature ->
                    onRecordTemperature(
                        r
                    )
                })
            .onSignal(
                PostStop::class.java,
                Function { _ -> onPostStop() })
            .build()
    }

    private fun onRecordTemperature(r: RecordTemperature): Behavior<Command> {
        getContext().log.info("Recorded temperature reading {} with {}", r.value, r.requestId)
        lastTemperatureReading = Optional.of(r.value)
        r.replyTo.tell(TemperatureRecorded(r.requestId))
        return this
    }

    private fun onReadTemperature(r: ReadTemperature): Behavior<Command> {
        r.replyTo.tell(RespondTemperature(r.requestId, lastTemperatureReading))
        return this
    }

    private fun onPostStop(): Device {
        context.log.info("Device actor {}-{} stopped", groupId, deviceId)
        return this
    }
}