package com.exchange.zd.matching;

import com.exchange.zd.kafka.MessageHandler;
import com.exchange.zd.matching.waitstrategy.WaitStrategy;
import com.exchange.zd.zookeeper.CoordinationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SimpleMatchingEngineTest {
    private final static String INPUT_TOPIC = "input";
    private final static String OUTPUT_TOPIC = "output";

    @Test
    public void initTest(){
        MessageHandler messageHandler = Mockito.mock(MessageHandler.class);
        CoordinationHandler coordinationHandler = Mockito.mock(CoordinationHandler.class);
        WaitStrategy waitStrategy = Mockito.mock(WaitStrategy.class);
        SimpleMatchingEngine me = new SimpleMatchingEngine(INPUT_TOPIC, OUTPUT_TOPIC,
                messageHandler, coordinationHandler, waitStrategy);

        Assertions.assertFalse(me.isPrimary(), "Matching-engine should be in Secondary state");
        me.setAsPrimary();
        Assertions.assertTrue(me.isPrimary(), "Matching-engine should be in Primary state");
    }
}
