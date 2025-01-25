package com.exchange.zd.matching.processor;

public interface MessageProcessor {
    void processOrder(String msg);
}
