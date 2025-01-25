package com.exchange.zd.matching;

import com.exchange.zd.enums.InstanceState;
import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.processor.MessageProcessor;
import com.exchange.zd.matching.processor.PrimaryMessageProcessor;
import com.exchange.zd.matching.processor.SecondaryMessageProcessor;
import com.exchange.zd.matching.waitstrategy.WaitStrategy;
import com.exchange.zd.zookeeper.CoordinationHandler;

public class SimpleMatchingEngine implements MatchingEngine {
    private final String inputTopic;
    private final String outputTopic;
    private final MessageHandler messageHandler;
    private final CoordinationHandler coordinationHandler;
    private final WaitStrategy waitStrategy;
    private InstanceState state = InstanceState.SECONDARY;
    private final MessageProcessor primaryMessageProcessor;
    private final MessageProcessor secondaryMessageProcessor;

    public SimpleMatchingEngine(String inputTopic, String outputTopic, MessageHandler messageHandler,
                                CoordinationHandler coordinationHandler, WaitStrategy waitStrategy){
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.messageHandler = messageHandler;
        this.coordinationHandler = coordinationHandler;
        this.waitStrategy = waitStrategy;
        primaryMessageProcessor = new PrimaryMessageProcessor(waitStrategy);
        secondaryMessageProcessor = new SecondaryMessageProcessor();
    }

    @Override
    public void start() {
        new Thread(()->{
            while (true){
                if (isPrimary()){
                    // Run as Primary instance
                    System.out.println("Fetch messages from queue...");
                    coordinationHandler.ping();
                    messageHandler.consume(inputTopic, primaryMessageProcessor::processOrder);
                } else if (!coordinationHandler.detectPrimaryNode()){
                    // If Secondary instance detected crash
                    System.out.println("Promoting instance to Primary...");
                    if (coordinationHandler.promoteToPrimary()){
                        setAsPrimary();
                    }
                } else {
                    // Run as Secondary instance and update state based on output queue
                    System.out.println("Run Secondary Instance...");
                    messageHandler.consume(outputTopic, secondaryMessageProcessor::processOrder);
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
}