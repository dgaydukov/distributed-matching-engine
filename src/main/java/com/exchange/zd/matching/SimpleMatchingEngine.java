package com.exchange.zd.matching;

import com.exchange.zd.enums.InstanceState;
import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.waitstrategy.WaitStrategy;
import com.exchange.zd.zookeeper.CoordinationHandler;

public class SimpleMatchingEngine implements MatchingEngine {
    private final MessageHandler messageHandler;
    private final CoordinationHandler coordinationHandler;
    private final WaitStrategy waitStrategy;
    private InstanceState state = InstanceState.SECONDARY;

    public SimpleMatchingEngine(MessageHandler messageHandler,
        CoordinationHandler coordinationHandler, WaitStrategy waitStrategy){
        this.messageHandler = messageHandler;
        this.coordinationHandler = coordinationHandler;
        this.waitStrategy = waitStrategy;
    }

    @Override
    public void start() {
        new Thread(()->{
            while (true){
                if (isPrimary()){
                    // Run as Primary instance
                    coordinationHandler.ping();
                    messageHandler.consume(this::processOrder);
                } else if (!coordinationHandler.detectPrimaryNode()){
                    // If Secondary instance detected crash
                    System.out.println("Detect Primary death. Promoting to Primary...");
                    if (coordinationHandler.promoteToPrimary()){
                        setAsPrimary();
                    }
                } else {
                    // Run as Secondary instance and update state based on output queue
                    waitStrategy.idle();
                }
            }
        }).start();
    }

    public boolean isPrimary(){
        return state == InstanceState.PRIMARY;
    }
    public void setAsPrimary(){
        state = InstanceState.PRIMARY;
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