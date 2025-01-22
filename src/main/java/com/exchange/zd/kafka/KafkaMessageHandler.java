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
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;

public class KafkaMessageHandler implements MessageHandler {
    private final KafkaProducer<String, String> producer;
    private final KafkaConsumer<String, String> consumer;
    private final String inputTopic;
    private final String outputTopic;

    public KafkaMessageHandler(String kafkaHost, String inputTopic, String outputTopic){
        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(producerProperties);

        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(List.of(outputTopic));

        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
    }

    @Override
    public void send(String msg) {
        ProducerRecord<String, String> record = new ProducerRecord<>(inputTopic, msg);
        producer.send(record);
    }

    @Override
    public void consume(Consumer<String> msgConsumer) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        for (ConsumerRecord<String, String> record : records) {
            msgConsumer.accept(record.value());
        }
    }
}
