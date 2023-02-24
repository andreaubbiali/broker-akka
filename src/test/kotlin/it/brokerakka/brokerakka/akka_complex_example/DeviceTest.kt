package it.brokerakka.brokerakka.akka_complex_example

import akka.actor.testkit.typed.javadsl.TestKitJunitResource
import akka.actor.testkit.typed.javadsl.TestProbe
import akka.actor.typed.ActorRef
import it.brokerakka.brokerakka.akka_complex_example.Device.*
import org.junit.ClassRule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*


class DeviceTest {

    companion object {
        @JvmField
        @ClassRule
        val testKit: TestKitJunitResource = TestKitJunitResource()
    }

    @Test
    fun testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
        val probe: TestProbe<RespondTemperature> = Companion.testKit.createTestProbe(RespondTemperature::class.java)
        val deviceActor: ActorRef<Device.Command> = Companion.testKit.spawn(Device.create("group", "device"))
        deviceActor.tell(ReadTemperature(42L, probe.ref))
        val response: RespondTemperature = probe.receiveMessage()
        assertEquals(42L, response.requestID)
        assertEquals(Optional.empty<Double>(), response.value)
    }




//    @Test
//    fun testReplyWithLatestTemperatureReading() {
//        val recordProbe: TestProbe<TemperatureRecorded> = testKit.createTestProbe(TemperatureRecorded::class.java)
//        val readProbe: TestProbe<RespondTemperature> = testKit.createTestProbe(RespondTemperature::class.java)
//        val deviceActor: ActorRef<Command> = testKit.spawn(Device.create("group", "device"))
//        deviceActor.tell(RecordTemperature(1L, 24.0, recordProbe.getRef()))
//        assertEquals(1L, recordProbe.receiveMessage().requestId)
//        deviceActor.tell(ReadTemperature(2L, readProbe.getRef()))
//        val response1: RespondTemperature = readProbe.receiveMessage()
//        assertEquals(2L, response1.requestId)
//        assertEquals(Optional.of(24.0), response1.value)
//        deviceActor.tell(RecordTemperature(3L, 55.0, recordProbe.getRef()))
//        assertEquals(3L, recordProbe.receiveMessage().requestId)
//        deviceActor.tell(ReadTemperature(4L, readProbe.getRef()))
//        val response2: RespondTemperature = readProbe.receiveMessage()
//        assertEquals(4L, response2.requestId)
//        assertEquals(Optional.of(55.0), response2.value)
//    }

//    @Test
//    fun testReplyToRegistrationRequests() {
//        val probe: TestProbe<DeviceRegistered> = testKit.createTestProbe(DeviceRegistered::class.java)
//        val groupActor: ActorRef<DeviceGroup.Command> = testKit.spawn(DeviceGroup.create("group"))
//        groupActor.tell(RequestTrackDevice("group", "device", probe.getRef()))
//        val (device) = probe.receiveMessage()
//
//        // another deviceId
//        groupActor.tell(RequestTrackDevice("group", "device3", probe.getRef()))
//        val (device1) = probe.receiveMessage()
//        assertNotEquals(device, device1)
//
//        // Check that the device actors are working
//        val recordProbe: TestProbe<TemperatureRecorded> = testKit.createTestProbe(TemperatureRecorded::class.java)
//        device.tell(RecordTemperature(0L, 1.0, recordProbe.getRef()))
//        assertEquals(0L, recordProbe.receiveMessage().requestId)
//        device1.tell(RecordTemperature(1L, 2.0, recordProbe.getRef()))
//        assertEquals(1L, recordProbe.receiveMessage().requestId)
//    }
//
//    @Test
//    fun testIgnoreWrongRegistrationRequests() {
//        val probe: TestProbe<DeviceRegistered> = testKit.createTestProbe(DeviceRegistered::class.java)
//        val groupActor: ActorRef<DeviceGroup.Command> = testKit.spawn(DeviceGroup.create("group"))
//        groupActor.tell(RequestTrackDevice("wrongGroup", "device1", probe.getRef()))
//        probe.expectNoMessage()
//    }

//    @Test
//    fun testReturnSameActorForSameDeviceId() {
//        val probe: TestProbe<DeviceRegistered> = testKit.createTestProbe(DeviceRegistered::class.java)
//        val groupActor: ActorRef<DeviceGroup.Command> = testKit.spawn(DeviceGroup.create("group"))
//        groupActor.tell(RequestTrackDevice("group", "device", probe.getRef()))
//        val (device) = probe.receiveMessage()
//
//        // registering same again should be idempotent
//        groupActor.tell(RequestTrackDevice("group", "device", probe.getRef()))
//        val (device1) = probe.receiveMessage()
//        assertEquals(device, device1)
//    }

//    @Test
//    fun testListActiveDevices() {
//        val registeredProbe: TestProbe<DeviceRegistered> = testKit.createTestProbe(DeviceRegistered::class.java)
//        val groupActor: ActorRef<DeviceGroup.Command> = testKit.spawn(DeviceGroup.create("group"))
//        groupActor.tell(RequestTrackDevice("group", "device1", registeredProbe.getRef()))
//        registeredProbe.receiveMessage()
//        groupActor.tell(RequestTrackDevice("group", "device2", registeredProbe.getRef()))
//        registeredProbe.receiveMessage()
//        val deviceListProbe: TestProbe<ReplyDeviceList> = testKit.createTestProbe(ReplyDeviceList::class.java)
//        groupActor.tell(RequestDeviceList(0L, "group", deviceListProbe.getRef()))
//        val reply: ReplyDeviceList = deviceListProbe.receiveMessage()
//        assertEquals(0L, reply.requestId)
//        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.ids)
//    }
//
//    @Test
//    fun testListActiveDevicesAfterOneShutsDown() {
//        val registeredProbe: TestProbe<DeviceRegistered> = testKit.createTestProbe(DeviceRegistered::class.java)
//        val groupActor: ActorRef<DeviceGroup.Command> = testKit.spawn(DeviceGroup.create("group"))
//        groupActor.tell(RequestTrackDevice("group", "device1", registeredProbe.getRef()))
//        val (toShutDown) = registeredProbe.receiveMessage()
//        groupActor.tell(RequestTrackDevice("group", "device2", registeredProbe.getRef()))
//        val (device) = registeredProbe.receiveMessage()
//        val deviceListProbe: TestProbe<ReplyDeviceList> = testKit.createTestProbe(ReplyDeviceList::class.java)
//        groupActor.tell(RequestDeviceList(0L, "group", deviceListProbe.getRef()))
//        val reply: ReplyDeviceList = deviceListProbe.receiveMessage()
//        assertEquals(0L, reply.requestId)
//        assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.ids)
//        toShutDown.tell(Passivate.INSTANCE)
//        registeredProbe.expectTerminated(toShutDown, registeredProbe.getRemainingOrDefault())
//
//        // using awaitAssert to retry because it might take longer for the groupActor
//        // to see the Terminated, that order is undefined
//        registeredProbe.awaitAssert<Any>(
//            Function0<Any?> {
//                groupActor.tell(RequestDeviceList(1L, "group", deviceListProbe.getRef()))
//                val r: ReplyDeviceList = deviceListProbe.receiveMessage()
//                assertEquals(1L, r.requestId)
//                assertEquals(Stream.of("device2").collect(Collectors.toSet()), r.ids)
//                null
//            })
//    }

    // TODO do tests of deviceManager

}