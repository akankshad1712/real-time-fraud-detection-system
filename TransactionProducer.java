package producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;

public class TransactionProducer {
    private static final String TOPIC = "transactions";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 1; i <= 10; i++) {
            String transaction = "txn_id:" + i + ",amount:" + (i * 100) + ",location:IN";
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, transaction);
            producer.send(record);
            System.out.println("Sent: " + transaction);
        }

        producer.close();
    }
}
