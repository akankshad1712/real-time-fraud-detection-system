package com.akanksha.fraud.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

public final class TransactionConsumer {
    private static final ObjectMapper JSON = new ObjectMapper();
    public static void main(String[] args) {
        String brokers = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String inputTopic = env("KAFKA_TRANSACTIONS_TOPIC", "transactions");
        String alertTopic = env("KAFKA_ALERTS_TOPIC", "fraud-alerts");
        BigDecimal threshold = new BigDecimal(env("FRAUD_AMOUNT_THRESHOLD", "1000.00"));

        Properties cp = new Properties();
        cp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        cp.put(ConsumerConfig.GROUP_ID_CONFIG, "fraud-detector-v1");
        cp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        cp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        cp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        cp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        cp.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        Properties pp = new Properties();
        pp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        pp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        pp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        pp.put(ProducerConfig.ACKS_CONFIG, "all");
        FraudDetector detector = new FraudDetector(threshold);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(cp);
             KafkaProducer<String, String> alerts = new KafkaProducer<>(pp)) {
            consumer.subscribe(Collections.singletonList(inputTopic));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            System.out.printf("Fraud detector listening on %s; threshold=%s%n", inputTopic, threshold);
            try {
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord<String, String> record : records) {
                        try {
                            Map<String, Object> tx = JSON.readValue(record.value(), new TypeReference<Map<String, Object>>() {});
                            List<String> reasons = detector.reasons(tx);
                            if (!reasons.isEmpty()) {
                                Map<String, Object> alert = new LinkedHashMap<>();
                                alert.put("alertId", UUID.randomUUID().toString());
                                alert.put("detectedAt", java.time.Instant.now().toString());
                                alert.put("sourceTopic", record.topic());
                                alert.put("sourcePartition", record.partition());
                                alert.put("sourceOffset", record.offset());
                                alert.put("reasons", reasons);
                                alert.put("transaction", tx);
                                alerts.send(new ProducerRecord<>(alertTopic, String.valueOf(tx.get("accountId")),
                                        JSON.writeValueAsString(alert))).get();
                                System.out.printf("FRAUD ALERT tx=%s reasons=%s%n", tx.get("transactionId"), reasons);
                            }
                        } catch (Exception malformed) {
                            System.err.printf("Skipping malformed event at %s-%d@%d: %s%n",
                                    record.topic(), record.partition(), record.offset(), malformed.getMessage());
                            // Production systems should route malformed records to a dead-letter topic.
                        }
                    }
                    if (!records.isEmpty()) {
                        alerts.flush();
                        consumer.commitSync();
                    }
                }
            } catch (WakeupException shutdown) {
                // Expected when the process is stopped.
            }
        } catch (Exception e) {
            System.err.println("Consumer stopped: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}