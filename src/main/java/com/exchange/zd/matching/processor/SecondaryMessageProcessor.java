package com.exchange.zd.matching.processor;

import com.exchange.zd.matching.waitstrategy.WaitStrategy;

public class SecondaryMessageProcessor implements MessageProcessor{
    private final WaitStrategy waitStrategy;

    public SecondaryMessageProcessor (WaitStrategy waitStrategy){
        this.waitStrategy = waitStrategy;
    }

    @Override
    public void processOrder(String order){
        // start order processing
        System.out.println("Processing order: "+order);

        // imitate hard calculations
        waitStrategy.idle(1000);

        // finish order processing
        System.out.println("Processed order: "+order);
    }
}
