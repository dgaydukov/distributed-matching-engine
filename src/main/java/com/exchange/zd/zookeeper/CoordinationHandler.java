package com.exchange.zd.zookeeper;

public interface CoordinationHandler {
    void detectPrimaryCrash();

    void promoteToPrimary();
}
