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
    public void processTest(){
        MessageHandler messageHandler = Mockito.mock(MessageHandler.class);
        CoordinationHandler coordinationHandler = Mockito.mock(CoordinationHandler.class);
        WaitStrategy waitStrategy = Mockito.mock(WaitStrategy.class);
        SimpleMatchingEngine me = new SimpleMatchingEngine(INPUT_TOPIC, OUTPUT_TOPIC,
                messageHandler, coordinationHandler, waitStrategy);

        // test primary/secondary
        Assertions.assertFalse(me.isPrimary(), "Matching-engine should be in Secondary state");
        me.setAsPrimary();
        Assertions.assertTrue(me.isPrimary(), "Matching-engine should be in Primary state");

        // run process method single time for Primary instance
        me.process();
        Mockito.verify(coordinationHandler, Mockito.times(1)).ping();
        Mockito.verify(messageHandler, Mockito.times(1)).consume(Mockito.eq(INPUT_TOPIC), Mockito.any());

        // run process method single time for Secondary instance
        // since our state is Primary we re-create instance to be in Secondary mode by default
        me = new SimpleMatchingEngine(INPUT_TOPIC, OUTPUT_TOPIC,
                messageHandler, coordinationHandler, waitStrategy);
        Mockito.when(coordinationHandler.detectPrimaryNode()).thenReturn(true);
        me.process();
        Mockito.verify(messageHandler, Mockito.times(1)).consume(Mockito.eq(OUTPUT_TOPIC), Mockito.any());

        // test promotion from Secondary to Primary
        Mockito.when(coordinationHandler.detectPrimaryNode()).thenReturn(false);
        Mockito.when(coordinationHandler.promoteToPrimary()).thenReturn(true);
        Assertions.assertFalse(me.isPrimary(), "Matching-engine should be in Secondary state");
        me.process();
        Mockito.verify(coordinationHandler, Mockito.times(1)).promoteToPrimary();
        Assertions.assertTrue(me.isPrimary(), "Matching-engine should be in Primary state");
    }
}
