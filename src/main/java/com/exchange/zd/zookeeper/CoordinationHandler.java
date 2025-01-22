package com.exchange.zd.zookeeper;

public interface CoordinationHandler {
    boolean detectPrimaryNode();

    boolean promoteToPrimary();
}
