package org.example;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.example.data.OrderBtcEvent;

import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig;

public class CoinbaseOrderBtcConsumer {

    private Properties props = new Properties();
    private KafkaConsumer<String, OrderBtcEvent> consumer;

    public CoinbaseOrderBtcConsumer() {
        System.out.println(Secrets.SERVER_URL + " is the broker url in use");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Secrets.SERVER_URL);
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username='"
                + Secrets.KAFKA_CLUSTER_KEY + "' password='" + Secrets.KAFKA_CLUSTER_SECRET + "';");
        props.put("sasl.mechanism", "PLAIN");
        props.put("client.dns.lookup", "use_all_dns_ips");
        props.put("session.timeout.ms", "45000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "io.confluent.kafka.serializers.KafkaJsonDeserializer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "orderbtc.consumer.v3");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(KafkaJsonDeserializerConfig.JSON_VALUE_TYPE, OrderBtcEvent.class);
        consumer = new KafkaConsumer<String, OrderBtcEvent>(props);
        consumer.subscribe(List.of(Topics.ORDERS_BTC));

    }

    public void consumeFromKafka() {
        System.out.println("Consuming from kafka started");
        while (true) {
            var results = consumer.poll(Duration.of(1, ChronoUnit.SECONDS));

            for (ConsumerRecord<String, OrderBtcEvent> result : results) {
                System.out.println(result.value().toString());
            }
            System.out.println("RESULTS:::" + results.count());

        }

    }

    public static void main(String[] args) {
        CoinbaseOrderBtcConsumer jsonConsumer = new CoinbaseOrderBtcConsumer();
        jsonConsumer.consumeFromKafka();
    }
}
