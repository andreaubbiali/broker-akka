package it.brokerakka.brokerakka.akka_complex_example

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import akka.japi.function.Function
import java.util.*
import kotlin.collections.HashMap


/**
 * Responsible for register a device and create the group and devices actors.
 * This is the only available component up front while groups and device actors are created on-demand.
 *
 * When the deviceManager receive a request with a group and deviceID:
 * - If the manager has an actor for the device group, it forwards the request to it.
 * - Otherwise it creates a new device group actor and then forwards the request.
 *
 */
class DeviceManager private constructor(private val context: ActorContext<DeviceManager.Command>): AbstractBehavior<DeviceManager.Command>(context) {

    private val groupIdToActor: MutableMap<String, ActorRef<DeviceGroup.Command>> = HashMap()

    init {
        context.log.info("DeviceManager started")
    }

    interface Command

    data class RequestTrackDevice(val groupId: String, val deviceId: String, val replyTo: ActorRef<DeviceRegistered?>) :
        Command, DeviceGroup.Command

    data class DeviceRegistered(val device: ActorRef<Device.Command>)

    internal data class RequestDeviceList(val requestId: Long, val groupId: String, val replyTo: ActorRef<ReplyDeviceList>) :
        Command, DeviceGroup.Command

    class ReplyDeviceList(val requestId: Long, val ids: Set<String>)

    internal data class DeviceGroupTerminated(val groupId: String) : Command

    fun create(): Behavior<Command> {
        return Behaviors.setup { context: ActorContext<Command> -> DeviceManager(context) }
    }

    private fun onTrackDevice(trackMsg: RequestTrackDevice): DeviceManager {
        val groupId = trackMsg.groupId
        val ref = groupIdToActor[groupId]
        if (ref != null) {
            ref.tell(trackMsg)
        } else {
            getContext().log.info("Creating device group actor for {}", groupId)
            val groupActor = getContext().spawn<DeviceGroup.Command>(
                DeviceGroup.create(groupId),
                "group-$groupId"
            )
            getContext().watchWith(groupActor, DeviceGroupTerminated(groupId))
            groupActor.tell(trackMsg)
            groupIdToActor[groupId] = groupActor
        }
        return this
    }

    private fun onRequestDeviceList(request: RequestDeviceList): DeviceManager {
        val ref = groupIdToActor[request.groupId]
        if (ref != null) {
            ref.tell(request)
        } else {
            request.replyTo.tell(ReplyDeviceList(request.requestId, Collections.emptySet()))
        }
        return this
    }

    private fun onTerminated(t: DeviceGroupTerminated): DeviceManager {
        getContext().log.info("Device group actor for {} has been terminated", t.groupId)
        groupIdToActor.remove(t.groupId)
        return this
    }

    override fun createReceive(): Receive<Command?>? {
        return newReceiveBuilder()
            .onMessage(
                RequestTrackDevice::class.java
            ) { trackMsg: RequestTrackDevice ->
                onTrackDevice(
                    trackMsg
                )
            }
            .onMessage(
                RequestDeviceList::class.java
            ) { request: RequestDeviceList -> onRequestDeviceList(request) }
            .onMessage(
                DeviceGroupTerminated::class.java,
                Function { t -> onTerminated(t) })
            .onSignal(
                PostStop::class.java,
                Function { _ -> onPostStop() })
            .build()
    }

    private fun onPostStop(): DeviceManager {
        getContext().log.info("DeviceManager stopped")
        return this
    }
}