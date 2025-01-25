package com.exchange.zd.kafka;

import java.util.function.Consumer;

public interface MessageHandler {
    void send(String topic, String msg);

    void consume(String topic, Consumer<String> consumer);
}
