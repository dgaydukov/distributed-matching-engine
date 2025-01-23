package com.exchange.zd;

import com.exchange.zd.kafka.KafkaMessageHandler;
import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.MatchingEngine;
import com.exchange.zd.matching.SimpleMatchingEngine;
import com.exchange.zd.zookeeper.CoordinationHandler;
import com.exchange.zd.zookeeper.ZookeeperCoordinationHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main(String[] args) {
        System.out.println("start");
        log.info("info");
        log.debug("debug");
        emulateLeaderSelection();

//        MessageHandler messageHandler = new KafkaMessageHandler("localhost:9092", "me-input", "me-output");
//        CoordinationHandler coordinationHandler = new ZookeeperCoordinationHandler("localhost:2181");
//        MatchingEngine me = new SimpleMatchingEngine(messageHandler, coordinationHandler);
//        me.start();
    }

    @SneakyThrows
    public static void emulateLeaderSelection()  {
        CoordinationHandler coordinationHandler = new ZookeeperCoordinationHandler("localhost:2181");
        System.out.println(coordinationHandler.promoteToPrimary());

        System.out.println("detectPrimaryNode => "+coordinationHandler.detectPrimaryNode());
        Thread.sleep(10*60*1000);
        System.out.println("detectPrimaryNode => "+coordinationHandler.detectPrimaryNode());
    }
}