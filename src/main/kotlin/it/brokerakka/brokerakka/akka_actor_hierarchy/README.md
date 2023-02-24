# What is this?

This is an example detached from others, used to see the actor hierarchy in Akka.

The response are:
```shell
    First: Actor[akka://testSystem/user/first-actor#397142239]
    Second: Actor[akka://testSystem/user/first-actor/second-actor#-72907485]
```

So we can see that every actor I created is under the user actor.
Another thing we can notice is that the second actor has "first-actor" in its path, it is because first actor is its parent.

Each reference start with `akka://` this is because each reference is a valid URL (this is protocol specific).

The `testSystem` stands for the hostname in the URL.  If remote communication between multiple systems is enabled, this part of the URL includes the hostname so other systems can find it on the network

The number in the last part is a unique identifier that you can ignore in most cases.