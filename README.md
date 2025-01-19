# Zero-downtime matching engine

### Content
* [Description](#description)
* [Why Zookeeper](#why-zookeeper)
* [How it works](#how-it-works)
* [Test coverage](#test-coverage)

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
Test coverage is the most important thing when we are writing software, so we have created following test cases: