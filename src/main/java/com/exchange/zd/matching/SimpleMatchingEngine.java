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
        new Thread(()->{
            while (true){
                messageHandler.consume(this::processOrder);
            }
        }).start();
    }

    public void processOrder(String order){
        // start order processing
        System.out.println("Processing order: "+order);

        // imitate hard calculations
        sleep(1);

        // finish order processing
        System.out.println("Processed order: "+order);

        // send response
        messageHandler.send("handled: " + order);
    }

    private void sleep(int sec){
        try{
            Thread.sleep(sec * 1000L);
        } catch (InterruptedException ex){
            throw new RuntimeException(ex);
        }
    }
}