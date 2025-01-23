package com.exchange.zd.matching;

import com.exchange.zd.enums.InstanceState;
import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.zookeeper.CoordinationHandler;

public class SimpleMatchingEngine implements MatchingEngine {
    private final MessageHandler messageHandler;
    private final CoordinationHandler coordinationHandler;
    private InstanceState state = InstanceState.SECONDARY;

    public SimpleMatchingEngine(MessageHandler messageHandler,
        CoordinationHandler coordinationHandler){
        this.messageHandler = messageHandler;
        this.coordinationHandler = coordinationHandler;
    }

    @Override
    public void start() {
        new Thread(()->{
            while (true){
                if (isPrimary()){
                    coordinationHandler.ping();
                    messageHandler.consume(this::processOrder);
                } else if (!coordinationHandler.detectPrimaryNode()){
                    // after each consume we can check if Primary is dead and if we are Secondary to switch to Primary
                    System.out.println("Detect Primary death. Promoting to Primary...");
                    if (coordinationHandler.promoteToPrimary()){
                        setAsPrimary();
                    }
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