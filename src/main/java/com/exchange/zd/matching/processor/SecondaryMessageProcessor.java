package com.exchange.zd.matching.processor;

public class SecondaryMessageProcessor implements MessageProcessor{

    @Override
    public void processOrder(String order){
        // start order processing
        System.out.println("Processing order: "+order);

        // finish order processing
        System.out.println("Processed order: "+order);
    }
}
