package it.brokerakka.brokerakka.akka_complex_example

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import java.util.*


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

    /**
     * Map that contains <groupID, DeviceGroupActor>
     */
    private val groupIdToActor: MutableMap<String, ActorRef<DeviceGroup.Command>> = HashMap()

    init {
        context.log.info("DeviceManager started")
    }

    interface Command

    /**
     * Accepted incoming messages (implements [Command] interface)
     * Do not make it private, some of them are used in other class
     */
    data class RequestTrackDevice(val groupId: String, val deviceId: String, val replyTo: ActorRef<DeviceRegistered?>) :
        Command, DeviceGroup.Command
    internal data class RequestDeviceList(val requestId: Long, val groupId: String, val replyTo: ActorRef<ReplyDeviceList>) :
        Command, DeviceGroup.Command
    internal data class DeviceGroupTerminated(val groupId: String) : Command

    // Responses
    class ReplyDeviceList(val requestId: Long, val ids: Set<String>)
    data class DeviceRegistered(val device: ActorRef<Device.Command>)

    fun create(): Behavior<Command> {
        return Behaviors.setup { context: ActorContext<Command> -> DeviceManager(context) }
    }

    /**
     * Function called on RequestTrackDevice's message arrived
     */
    private fun onTrackDevice(trackMsg: RequestTrackDevice): DeviceManager {
        val groupId = trackMsg.groupId
        val groupActor = getOrCreateFromMap(groupId)

        // send the message to the groupActor
        groupActor.tell(trackMsg)
        return this
    }

    private fun getOrCreateFromMap(groupId: String): ActorRef<DeviceGroup.Command> {
        val tmp = groupIdToActor[groupId]
        if (tmp != null){
            return tmp
        }

        getContext().log.info("Creating device group actor for {}", groupId)
        val groupActor = getContext().spawn<DeviceGroup.Command>(
            DeviceGroup.create(groupId),
            "group-$groupId"
        )
        getContext().watchWith(groupActor, DeviceGroupTerminated(groupId))
        groupIdToActor[groupId] = groupActor


        return groupActor
    }

    /**
     * Function called on RequestDeviceList's message arrived
     * If the devicegroup is present pass to it the message
     * Otherwise reply with an empty list
     */
    private fun onRequestDeviceList(request: RequestDeviceList): DeviceManager {
        val ref = groupIdToActor[request.groupId]
        if (ref != null) {
            ref.tell(request)
        } else {
            request.replyTo.tell(ReplyDeviceList(request.requestId, Collections.emptySet()))
        }
        return this
    }

    /**
     * Function called on DeviceGroupTerminated's message arrived
     */
    private fun onTerminated(t: DeviceGroupTerminated): DeviceManager {
        getContext().log.info("Device group actor for {} has been terminated", t.groupId)
        groupIdToActor.remove(t.groupId)
        return this
    }

    /**
     * Function called on onPostStop's signal arrived
     */
    private fun onPostStop(): DeviceManager {
        getContext().log.info("DeviceManager stopped")
        return this
    }

    /**
     * Create the receiver
     */
    override fun createReceive(): Receive<Command?>? {
        return newReceiveBuilder()
            .onMessage(RequestTrackDevice::class.java, ::onTrackDevice)
            .onMessage(RequestDeviceList::class.java, ::onRequestDeviceList)
            .onMessage(DeviceGroupTerminated::class.java, ::onTerminated)
            .onSignal(
                PostStop::class.java
            ){ _ -> onPostStop() }
            .build()
    }
}