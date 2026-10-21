package com.akanksha.fraud.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Generates JSON transaction events for local development and demos. */
public final class TransactionProducer {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String[] ACCOUNTS = {"acct-1001", "acct-1002", "acct-1003", "acct-1004"};
    private static final String[] MERCHANTS = {"grocery", "fuel", "travel", "electronics"};

    public static void main(String[] args) throws Exception {
        String brokers = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String topic = env("KAFKA_TRANSACTIONS_TOPIC", "transactions");
        int count = Integer.parseInt(env("TRANSACTION_COUNT", "100"));
        long delayMs = Long.parseLong(env("PRODUCER_DELAY_MS", "250"));

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < count; i++) {
                String account = ACCOUNTS[ThreadLocalRandom.current().nextInt(ACCOUNTS.length)];
                // Every tenth event is deliberately suspicious for demonstration.
                BigDecimal amount = (i % 10 == 0)
                        ? new BigDecimal("1000.00")
                        : BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(5, 45000), 2);
                Transaction tx = new Transaction(UUID.randomUUID().toString(), account,
                        amount, MERCHANTS[ThreadLocalRandom.current().nextInt(MERCHANTS.length)],
                        Instant.now().toString(), "INR");
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(topic, tx.accountId, JSON.writeValueAsString(tx));
                producer.send(record, (metadata, error) -> {
                    if (error != null) System.err.println("Publish failed: " + error.getMessage());
                    else System.out.printf("published topic=%s partition=%d offset=%d tx=%s amount=%s%n",
                            metadata.topic(), metadata.partition(), metadata.offset(), tx.transactionId, tx.amount);
                });
                if (delayMs > 0) Thread.sleep(delayMs);
            }
            producer.flush();
            System.out.printf("Finished publishing %d demo transactions to %s%n", count, topic);
        }
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    public static final class Transaction {
        public String transactionId;
        public String accountId;
        public BigDecimal amount;
        public String merchant;
        public String timestamp;
        public String currency;
        public Transaction() {}
        Transaction(String id, String account, BigDecimal amount, String merchant, String timestamp, String currency) {
            this.transactionId=id; this.accountId=account; this.amount=amount; this.merchant=merchant;
            this.timestamp=timestamp; this.currency=currency;
        }
    }
}