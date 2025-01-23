package com.exchange.zd;

import com.exchange.zd.kafka.KafkaMessageHandler;
import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.MatchingEngine;
import com.exchange.zd.matching.SimpleMatchingEngine;
import com.exchange.zd.matching.waitstrategy.SleepWaitStrategy;
import com.exchange.zd.matching.waitstrategy.WaitStrategy;
import com.exchange.zd.zookeeper.CoordinationHandler;
import com.exchange.zd.zookeeper.ZookeeperCoordinationHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main(String[] args) {
        emulateLeaderSelection();
    }

    public static void startMatchingEngine(){
        WaitStrategy waitStrategy = new SleepWaitStrategy();
        MessageHandler messageHandler = new KafkaMessageHandler("localhost:9092", "me-input", "me-output");
        CoordinationHandler coordinationHandler = new ZookeeperCoordinationHandler("localhost:2181");
        MatchingEngine me = new SimpleMatchingEngine(messageHandler, coordinationHandler, waitStrategy);
        me.start();
    }

    @SneakyThrows
    public static void emulateLeaderSelection()  {
        CoordinationHandler coordinationHandler = new ZookeeperCoordinationHandler("localhost:2181");
        System.out.println("promoteToPrimary => "+coordinationHandler.promoteToPrimary());
        for(int i = 1; i <= 10; i++){
            System.out.println("i = "+i+", detectPrimaryNode => "+coordinationHandler.detectPrimaryNode());
            Thread.sleep(10*60*1000);
        }
    }
}