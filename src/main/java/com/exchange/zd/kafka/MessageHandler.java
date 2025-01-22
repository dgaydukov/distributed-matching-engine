package com.exchange.zd.kafka;

import java.util.function.Consumer;

public interface MessageHandler {
    void send(String msg);

    void consume(Consumer<String> consumer);
}
