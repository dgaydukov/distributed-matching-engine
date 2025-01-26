package com.exchange.zd.matching;

import com.exchange.zd.enums.InstanceState;
import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.processor.MessageProcessor;
import com.exchange.zd.matching.processor.PrimaryMessageProcessor;
import com.exchange.zd.matching.processor.SecondaryMessageProcessor;
import com.exchange.zd.matching.waitstrategy.WaitStrategy;
import com.exchange.zd.zookeeper.CoordinationHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleMatchingEngine implements MatchingEngine {
    private final String inputTopic;
    private final String outputTopic;
    private final MessageHandler messageHandler;
    private final CoordinationHandler coordinationHandler;
    private InstanceState state = InstanceState.SECONDARY;
    private final MessageProcessor primaryMessageProcessor;
    private final MessageProcessor secondaryMessageProcessor;

    public SimpleMatchingEngine(String inputTopic, String outputTopic, MessageHandler messageHandler,
                                CoordinationHandler coordinationHandler, WaitStrategy waitStrategy){
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.messageHandler = messageHandler;
        this.coordinationHandler = coordinationHandler;
        primaryMessageProcessor = new PrimaryMessageProcessor(outputTopic, waitStrategy, messageHandler);
        secondaryMessageProcessor = new SecondaryMessageProcessor(waitStrategy);
    }

    @Override
    public void start() {
        new Thread(()->{
            while (true){
                process();
            }
        }).start();
    }

    public void process(){
        if (isPrimary()){
            // Run as Primary instance
            log.info("Fetch messages from queue...");
            coordinationHandler.ping();
            messageHandler.consume(inputTopic, primaryMessageProcessor::processOrder);
        } else if (!coordinationHandler.detectPrimaryNode()){
            // If Secondary instance detected crash
            log.info("Promoting instance to Primary...");
            if (coordinationHandler.promoteToPrimary()){
                setAsPrimary();
            }
        } else {
            // Run as Secondary instance and update state based on output queue
            log.info("Run Secondary Instance...");
            messageHandler.consume(outputTopic, secondaryMessageProcessor::processOrder);
        }
    }

    public boolean isPrimary(){
        return state == InstanceState.PRIMARY;
    }
    public void setAsPrimary(){
        state = InstanceState.PRIMARY;
    }
}