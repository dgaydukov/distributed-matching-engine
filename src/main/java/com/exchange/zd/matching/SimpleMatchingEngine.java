package com.exchange.zd.matching;

import com.exchange.zd.enums.InstanceState;
import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.waitstrategy.WaitStrategy;
import com.exchange.zd.zookeeper.CoordinationHandler;

public class SimpleMatchingEngine implements MatchingEngine {
    private final MessageHandler inputMessageHandler;
    private final MessageHandler outputMessageHandler;
    private final CoordinationHandler coordinationHandler;
    private final WaitStrategy waitStrategy;
    private InstanceState state = InstanceState.SECONDARY;

    public SimpleMatchingEngine(MessageHandler inputMessageHandler,
                                MessageHandler outputMessageHandler,
                                CoordinationHandler coordinationHandler,
                                WaitStrategy waitStrategy){
        this.inputMessageHandler = inputMessageHandler;
        this.outputMessageHandler = outputMessageHandler;
        this.coordinationHandler = coordinationHandler;
        this.waitStrategy = waitStrategy;
    }

    @Override
    public void start() {
        new Thread(()->{
            while (true){
                if (isPrimary()){
                    // Run as Primary instance
                    System.out.println("Fetch messages from queue...");
                    coordinationHandler.ping();
                    messageHandler.consume(this::processOrder);
                } else if (!coordinationHandler.detectPrimaryNode()){
                    // If Secondary instance detected crash
                    System.out.println("Promoting instance to Primary...");
                    if (coordinationHandler.promoteToPrimary()){
                        setAsPrimary();
                    }
                } else {
                    // Run as Secondary instance and update state based on output queue
                    System.out.println("Run Secondary Instance...");
                    waitStrategy.idle(1000);
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
        // If switch signal detected, exit Primary app
        if("SWITCH".equalsIgnoreCase(order)){
            System.out.println("Exiting app...");
            System.exit(0);
        }

        // start order processing
        System.out.println("Processing order: "+order);

        // imitate hard calculations
        waitStrategy.idle(1000);

        // finish order processing
        System.out.println("Processed order: "+order);

        // send response
        messageHandler.send("handled: " + order);
    }
}