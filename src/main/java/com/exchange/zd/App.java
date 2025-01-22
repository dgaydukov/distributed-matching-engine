package com.exchange.zd;

import com.exchange.zd.kafka.KafkaMessageHandler;
import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.MatchingEngine;
import com.exchange.zd.matching.SimpleMatchingEngine;
import com.exchange.zd.zookeeper.CoordinationHandler;
import com.exchange.zd.zookeeper.ZookeeperCoordinationHandler;

public class App {
    public static void main(String[] args) throws InterruptedException {
        CoordinationHandler coordinationHandler = new ZookeeperCoordinationHandler("localhost:2181");
        System.out.println(coordinationHandler.promoteToPrimary());
        for(int i = 1; i <= 10; i++){
            System.out.println("Step => "+i);
            if(!coordinationHandler.detectPrimaryNode()){
                break;
            }
            Thread.sleep(100_000);
        }


//        MessageHandler messageHandler = new KafkaMessageHandler("localhost:9092", "me-input", "me-output");
//        CoordinationHandler coordinationHandler = new ZookeeperCoordinationHandler();
//        MatchingEngine me = new SimpleMatchingEngine(messageHandler, coordinationHandler);
//        me.start();
    }
}