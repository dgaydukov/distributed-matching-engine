package com.exchange.zd.matching.processor;

import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.waitstrategy.WaitStrategy;

public class PrimaryMessageProcessor implements MessageProcessor{
    private final WaitStrategy waitStrategy;
    private final MessageHandler messageHandler;
    private final String outputTopic;

    public PrimaryMessageProcessor (String outputTopic, WaitStrategy waitStrategy,
                                    MessageHandler messageHandler){
        this.outputTopic = outputTopic;
        this.messageHandler = messageHandler;
        this.waitStrategy = waitStrategy;
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
        messageHandler.send(outputTopic,"handled: " + order);
    }
}
