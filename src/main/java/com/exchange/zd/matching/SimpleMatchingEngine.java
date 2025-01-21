package com.exchange.zd.matching;

import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.zookeeper.CoordinationHandler;

public class SimpleMatchingEngine implements MatchingEngine {
    private final MessageHandler messageHandler;
    private final CoordinationHandler coordinationHandler;

    public SimpleMatchingEngine(MessageHandler messageHandler,
        CoordinationHandler coordinationHandler){
        this.messageHandler = messageHandler;
        this.coordinationHandler = coordinationHandler;
    }

    @Override
    public void start() {

    }
}