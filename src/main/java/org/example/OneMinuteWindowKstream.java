package org.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.example.customserdes.CustomSerdes;
import org.example.data.OrderBtcEvent;
import org.example.data.VolumeBtcTimeWindow;
import org.example.data.VolumeHourly;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

public class OneMinuteWindowKstream {
	private Properties props = new Properties();
	private BigQueryWriter bigQueryWriter;

	public OneMinuteWindowKstream(BigQueryWriter bigQueryWriter) {
		this.bigQueryWriter = bigQueryWriter;
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Secrets.SERVER_URL);
		props.put("security.protocol", "SASL_SSL");
		props.put("sasl.jaas.config",
				"org.apache.kafka.common.security.plain.PlainLoginModule required username='"
						+ Secrets.KAFKA_CLUSTER_KEY + "' password='"
						+ Secrets.KAFKA_CLUSTER_SECRET + "';");
		props.put("sasl.mechanism", "PLAIN");
		props.put("client.dns.lookup", "use_all_dns_ips");
		props.put("session.timeout.ms", "45000");
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, Secrets.GROUP_ID);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, Secrets.GROUP_ID);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

	}

	public OneMinuteWindowKstream() {
		// TODO Auto-generated constructor stub
	}

	public Topology createTopology() {
		StreamsBuilder streamsBuilder = new StreamsBuilder();
		var ridesStream = streamsBuilder
				.stream(Topics.ORDERS_BTC,
						Consumed.with(Serdes.String(),
								CustomSerdes.getSerde(OrderBtcEvent.class))
								.withTimestampExtractor(
										(ConsumerRecord<Object, Object> record,
												long partitionTime) -> Instant.parse(
														((OrderBtcEvent) record
																.value()).time)
														.toEpochMilli()))
				.groupByKey()
				.windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
				.aggregate(
						() -> new VolumeBtcTimeWindow(),
						(key, value, aggregate) -> {
							aggregate.side = key;
							aggregate.addVolume(value);
							return aggregate;
						},
						Materialized.<String, VolumeBtcTimeWindow, WindowStore<Bytes, byte[]>>as(
								"one_minute_materialised_window")
								.withKeySerde(Serdes.String())
								.withValueSerde(CustomSerdes
										.getSerde(VolumeBtcTimeWindow.class)))
				.toStream()
				.map(
						(key, value) -> new KeyValue<>(value.side + "T" + key.window().start(),
								new VolumeHourly(
										key.window().start(),
										key.window().end(),
										value.side,
										value.volume)));

		ridesStream.to(Topics.ONE_MIN_WINDOW,
				Produced.with(
						Serdes.String(),
						CustomSerdes.getSerde(VolumeHourly.class)));

		streamsBuilder.stream(Topics.ONE_MIN_WINDOW,
				Consumed.with(Serdes.String(),
						CustomSerdes.getSerde(VolumeHourly.class)))
				.foreach((key, volumeHourly) -> {
					System.out.println("transaction processed , side = " + volumeHourly.side);
					bigQueryWriter.writeStream(volumeHourly);
				});
		return streamsBuilder.build();
	}

	public void calculateVolumeWindowed() {
		var topology = createTopology();
		var kStreams = new KafkaStreams(topology, props);
		kStreams.start();

	}

	public static void main(String[] args) {
		System.out.println("starting stream with " + Secrets.GCP_PROJECT_ID + Secrets.DATASET
				+ Secrets.SERVER_URL);
		new OneMinuteWindowKstream();
		var object = new OneMinuteWindowKstream(
				new BigQueryWriter(Secrets.GCP_PROJECT_ID, Secrets.DATASET, Secrets.TABLE));
		object.calculateVolumeWindowed();
	}
}
