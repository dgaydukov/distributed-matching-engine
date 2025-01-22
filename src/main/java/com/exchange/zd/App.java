package com.exchange.zd;

import com.exchange.zd.kafka.KafkaMessageHandler;
import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.MatchingEngine;
import com.exchange.zd.matching.SimpleMatchingEngine;
import com.exchange.zd.zookeeper.CoordinationHandler;
import com.exchange.zd.zookeeper.ZookeeperCoordinationHandler;

public class App {
    public static void main(String[] args) {
        MessageHandler messageHandler = new KafkaMessageHandler("localhost:9092", "me-input", "me-output");
        CoordinationHandler coordinationHandler = new ZookeeperCoordinationHandler();
        MatchingEngine me = new SimpleMatchingEngine(messageHandler, coordinationHandler);
        me.start();
    }
}