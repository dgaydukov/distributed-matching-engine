# Zero-downtime matching engine

### Content
* [Description](#description)
* [Why Zookeeper](#why-zookeeper)
* [How it works](#how-it-works)
* [Test coverage](#test-coverage)
* [Run instruction](#run-instruction)

### Description
This is the project to show how to implement zero-downtime deployment for 24/7 running instance of matching-engine.
If we are talking about crypto exchange which supposed to work 24/7 or any high-load application that is supposed to never be shutdown, we need an algo to release new code.
Because how we can release. Normally we shutdown the app. Upload new version of code (in case of java it's build jar files) and then start the app.
But if we are talking about crypto exchange, we are not able to shut it down. Trading is not supposed to be stopping. It should always work.
So the main purpose of this project is to show you, how you can implement this feature.

### Why Zookeeper
To implement the Primary/Secondary architecture, where you can release on-the-fly, you have to use some third-party distributed manager.
There are many out there. You can even try to create your own. You can use other systems like Redis or Aeron cluster.
But in my experience, most of matching-engine are written with kafka as main message bus. And as you know, Kafka always come bundled with Zookeeper.
So I assume it's the most known tool and if such, it can be clear to most of the ppl how we use it.
Other than that, there is no advantage over other instruments, the main purpose to use Zookeeper is just because of assumption, that majority of my audience already have experience with this tool.

### How it works
The logic is very simple.
We have 2 instances running at the same time and listening the same sequence of events.
Both should be updated to the same state.
Primary is always running as main instance. Secondary is not active, it just maintain it state.
When we want to update:
* take Secondary and stop it
* Update code for Secondary
* Start Secondary
* Make sure Secondary is up-to-date - since we have non-stopping sequence of events, it may take some time for Secondary to keep-up and achieve same state as Primary
* Send message to switch the instances
* Primary would stop, Secondary would pick-up at exactly the same moment-in-time or messageId
* Now Secondary is running as Primary that handles the app but with updated code
* It's always better to update the code of former Primary and start it as Secondary

### Test coverage
Across all my projects I always write about importance of test coverage.
Why it's so important. The answer is pretty simple, if you write test cases first and then write code, you will write better code.
But if you do the other way around, I ensure you with 99% you will have to refactor your code in some way.
In all my projects I always write high-quality code, but here I'll demonstrate what is bad code is.
Take a look at these 2 classes
* [KafkaMessageHandler](src/main/java/com/exchange/zd/kafka/KafkaMessageHandler.java) - class to work with kafka to send/receive messages
* [ZookeeperCoordinationHandler](src/main/java/com/exchange/zd/zookeeper/ZookeeperCoordinationHandler.java) - class to work with zookeeper to promote instance from Secondary state to Primary

What is the problem with those 2? Right Dependency Injection. Because we added class init as `new` inside those 2, we can't properly test them. To fix it, you need to use DI across your app. The best way is to use some kind of framework like Spring, because if you try to manually manage all dependencies - first you will have a lot of clutter code and second at some point you may mess up and cause troubles.
But here we don't have any type, and by writing code this way you create untestable code. 
If you try to test those 2 classes they would fail on init, because Kafka/Zookeeper is not running when you run tests. The best and easiest way is to move code creation beans of Kafka/Zookeeper into separate config file, and then inject those beans into above 2 classes.
I won't change the code here, so keep it as bad example.

On the other hand the class [SimpleMatchingEngine](src/main/java/com/exchange/zd/matching/SimpleMatchingEngine.java) is written a little better. In order to instantiate you have to pass other classes as params. By this you can mock them and test logic end-to-end.
Take a look at the [SimpleMatchingEngineTest](src/test/java/com/exchange/zd/matching/SimpleMatchingEngineTest.java) that test all 3 scenarios when you run matching-engine in primary mode, secondary mode or promoting Secondary to Primary.

### Run instruction
How to run this app. The installment can be run from Docker and consist of 4 parts:
* Zookeeper - to switch between Primary and Secondary instance
* Kafka - used as message queue (anything can be used, but since Kafka is bundled with Zookeeper I've decided to use it)
* Primary - java app that acts as primary matching-engine instance
* Secondary - same java app that acts as secondary matching-engine instance

Keep in mind:
* Both Primary and Secondary use the same java app
* We use Kafka as external queue, but any queue that can run as standalone app can be used. Keep in mind that internal queue inside java can't be used, because it pertains to java process, but we need something running as separate process.
* Matching-engine is quite simple, it's just print orders, no logic inside. This is done on purpose to keep system as simple as possible. If you want to see how to write ME, take a look [exchange-core](https://github.com/dgaydukov/exchange-core).
* The only thing that is implemented is transactionality:
  * The main risk during switching between Primary and Secondary is that message is half-executed. Imagine order is being matched, but not settled. And if crash happened, in such case order should be discarded in executed from scratch in newly promoted instance.
  * To catch such things we use concept called transactionality - only fully processed messages are marked as processed, otherwise it would be handled from scratch.

There are 2 main scenarios:
* Manual switching (zero-downtime deployment) - when we deliberately send message to switch from Primary to Secondary
  * we can emulate this scenario by sending `switch` message
* DR (disaster recovery) - in case our Primary died, we need to detect it and promote Secondary to be new Primary
  * we can emulate this by manually killing Primary java app

You can check out [docker-compose](docker-compose.yml), and see that we have 3 items there:
* Zookeeper - coordination service, used to run Kafka and also used to coordinate switching between Primary and Secondary
* Kafka - used as external queue
* kafka-ui - nice UI tool to send/receive kafka messages. It's useful in out case cause you can create topic, send message, receive message using this UI tool, and you don't need to run any console commands for this. You can check it out by going to `http://localhost:8082`

To start the app:
```shell
# start kafka, zookeeper and kafka-ui
docker-compose -f docker-compose.yml up -d
# create topic for testing (you can also use kafka-ui to manually add topic)
docker-compose exec kafka kafka-topics --create --topic matching-engine --partitions 1 --replication-factor 1 --bootstrap-server kafka:9092
# checkout ui, you should see topic
http://localhost:8082
```

We use Zookeeper concept of `CreateMode.EPHEMERAL` to create ephemeral node. When our app is dead or if we don't send ping once in a while, zookeeper would detect it, and mark the node as non-existing. We have implemented special method to ping.
So once our Primary would be declared dead, our Secondary instance would detect it, and promote itself as Primary.

Logger - we are using [logback.xml](src/main/resources/logback.xml) to configure our logger to show only `INFO` level, otherwise Zookeeper would print a lot of ping logs to check if instance is connected. Since we don't want to clutter console with such logs, we've added Logback configuration to manage logging level.
```shell
09:24:49.218 [main-SendThread(localhost:2181)] DEBUG org.apache.zookeeper.ClientCnxn - Got ping response for session id: 0x1000014c6700046 after 3ms.
09:24:50.884 [main-SendThread(localhost:2181)] DEBUG org.apache.zookeeper.ClientCnxn - Got ping response for session id: 0x1000014c6700046 after 2ms.
09:24:52.552 [main-SendThread(localhost:2181)] DEBUG org.apache.zookeeper.ClientCnxn - Got ping response for session id: 0x1000014c6700046 after 3ms.
```

Build and run the app:
* we use 2 plugins in `pom.xml` file `maven-shade-plugin` to build fat jar, where all files are bundled inside so we can run the app with simply put `java -jar target/zd-1.0.jar`
* Build docker with `docker build -t zookeeper-me .`
* Run docker `docker run --network=host zookeeper-me` - we need to pass param `--network=host` so docker call our machine localhost, not container internal localhost
* Run above command from 2 tabs in cmd
* You will see that one is running as Primary and second as Secondary
* Kill Primary instance, and you will notice how Secondary would be promoted to Primary
* If you re-run killed instance you will notice how it run as Secondary now