package com.exchange.zd.kafka;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

/**
 * Compare to producer to whom we pass topic every time, consumers should be created once
 * To avoid constant object creation for each call of `consume` method, we use nice trick called object pooling
 * We have a map of consumer objects and each time we want to consume new topic we create consumer and put it into map
 * So next time we want to get consumer, we don't create it, but reuse already created consumer
 */
public class KafkaMessageHandler implements MessageHandler {
    private final KafkaProducer<String, String> producer;
    private final Map<String, KafkaConsumer<String, String>> consumers = new HashMap<>();

    public KafkaMessageHandler(String kafkaHost){
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(producerProperties);
    }

    private void addConsumer(String topic){
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties);
        consumers.put(topic, consumer);
    }

    private KafkaConsumer<String, String> getConsumer(String topic){
        if (!consumers.containsKey(topic)){
            addConsumer(topic);
        }
        return consumers.get(topic);
    }

    @Override
    public void send(String topic, String msg) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, msg);
        producer.send(record);
    }

    @Override
    public void consume(String topic, Consumer<String> msgConsumer) {
        KafkaConsumer<String, String> consumer = getConsumer(topic);
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
        for (ConsumerRecord<String, String> record : records) {
            msgConsumer.accept(record.value());
        }
    }
}
