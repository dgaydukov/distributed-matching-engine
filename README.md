# Zero-downtime matching engine

### Content
* [Description](#description)
* [Zookeeper](#zookeeper)
* [How it works](#how-it-works)
* [Test coverage](#test-coverage)

### Description
This is the project to show how to implement zero-downtime deployment for 24/7 running instance of matching-engine.
If we are talking about crypto exchange which supposed to work 24/7 or any high-load application that is supposed to never be shutdown, we need an algo to release new code.
Because how we can release. Normally we shutdown the app. Upload new version of code (in case of java it's build jar files) and then start the app.
But if we are talking about crypto exchange, we are not able to shut it down. Trading is not supposed to be stopping. It should always work.
So the main purpose of this project is to show you, how you can implement this feature.

### Zookeeper

### How it works

### Test coverage