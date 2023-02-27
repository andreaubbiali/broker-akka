# Guide

Exemple from akka documentation:
https://doc.akka.io/docs/akka/current/typed/guide/tutorial.html

# Architecture

![img.png](/home/aubbiali/project/broker-akka/src/main/resources/arch.png)

Actor hierarchy:

                    IoTSupervisor 
          DeviceManager         DashboardManager  
      DevGroup ...  DevGroup      
    device1    ...       deviceN   

# What does an actor do?

An actor:
1. Waits for a request for the current temperature.
2. Responds to the request with a reply that either:
     - contains the current temperature or,
     - indicates that a temperature is not yet available.

# What we are finally implementing

Usual flow is:

1. A sensor device in the home connects through some protocol.
2. The component managing network connections accepts the connection.
3. The sensor provides its group and device ID to register with the device manager component of our system.
4. The device manager component handles registration by looking up or creating the actor responsible for keeping sensor state.
5. The actor responds with an acknowledgement, exposing its ActorRef.
6. The networking component now uses the ActorRef for communication between the sensor and device actor without going through the device manager.

We will work for point 3-6

