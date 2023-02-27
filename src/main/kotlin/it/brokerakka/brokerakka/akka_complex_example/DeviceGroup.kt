package it.brokerakka.brokerakka.akka_complex_example

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import it.brokerakka.brokerakka.akka_complex_example.DeviceManager.*


/**
 * When This receives the request to register an actor or the given device:
 * - if the group already has an actor replies with the ActorRef of the existing actor
 * - Otherwise create the device actor and replies with the ActorRef
 *
 * We could have used the Terminated signals to delete an actor from the map but in the map we are saving the actorID.
 * For this reason we are using a message approach to know when an actor is stopping.
 */
class DeviceGroup private constructor(
    private val context: ActorContext<DeviceGroup.Command>,
    private val groupId: String
    ): AbstractBehavior<DeviceGroup.Command>(context) {

    private val deviceIdToActor: MutableMap<String, ActorRef<Device.Command>> = HashMap()

    interface Command

    private data class DeviceTerminated(
        val device: ActorRef<Device.Command>,
        val groupId: String,
        val deviceId: String
    ) : Command

    init {
        context.log.info("DeviceGroup {} started", groupId)
    }

    companion object {
        fun create(groupId: String): Behavior<Command> {
            return Behaviors.setup { context -> DeviceGroup(context, groupId) }
        }
    }

    /**
     * Function called on RequestTrackDevice's message arrived
     * RequestTrackDevice is a message defined in DeviceManager class
     */
    private fun onTrackDevice(trackMsg: RequestTrackDevice): DeviceGroup {
        if (groupId == trackMsg.groupId) {
            var deviceActor: ActorRef<Device.Command>? = deviceIdToActor[trackMsg.deviceId]
            if (deviceActor != null) {
                trackMsg.replyTo.tell(DeviceRegistered(deviceActor))
            } else {
                getContext().log.info("Creating device actor for {}", trackMsg.deviceId)
                deviceActor = context.spawn(Device.create(groupId, trackMsg.deviceId), "device-" + trackMsg.deviceId)
                deviceIdToActor[trackMsg.deviceId] = deviceActor
                trackMsg.replyTo.tell(DeviceRegistered(deviceActor))
            }
        } else {
            getContext()
                .log
                .warn(
                    "Ignoring TrackDevice request for {}. This actor is responsible for {}.",
                    trackMsg.groupId,
                    groupId
                )
        }
        return this
    }

    /**
     * Function called on RequestDeviceList's message arrived
     */
    private fun onDeviceList(r: RequestDeviceList): DeviceGroup {
        r.replyTo.tell(ReplyDeviceList(r.requestId, deviceIdToActor.keys))
        return this
    }

    /**
     * Function called on DeviceTerminated's message arrived
     */
    private fun onTerminated(t: DeviceTerminated): DeviceGroup {
        getContext().log.info("Device actor for {} has been terminated", t.deviceId)
        deviceIdToActor.remove(t.deviceId)
        return this
    }

    /**
     * Function called on onPostStop's signal arrived
     */
    private fun onPostStop(): DeviceGroup {
        getContext().log.info("DeviceGroup {} stopped", groupId)
        return this
    }

    override fun createReceive(): Receive<Command?>? {
        return newReceiveBuilder()
            .onMessage(RequestTrackDevice::class.java, ::onTrackDevice)
            .onMessage(RequestDeviceList::class.java, ::onDeviceList)
            .onMessage(DeviceTerminated::class.java, ::onTerminated)
            .onSignal(PostStop::class.java
            ) { _: PostStop? -> onPostStop() }
            .build()
    }
}