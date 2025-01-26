package com.exchange.zd.matching.processor;

import com.exchange.zd.matching.waitstrategy.WaitStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecondaryMessageProcessor implements MessageProcessor{
    private final WaitStrategy waitStrategy;

    public SecondaryMessageProcessor (WaitStrategy waitStrategy){
        this.waitStrategy = waitStrategy;
    }

    @Override
    public void processOrder(String order){
        // start order processing
        log.info("Processing order={}", order);

        // imitate hard calculations
        waitStrategy.idle(1000);

        // finish order processing
        log.info("Processed order:={}", order);
    }
}
