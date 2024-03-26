package org.example.customserdes;

import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class CustomSerdes {

    public static <T> Serde<T> getSerde(Class<T> classOf) {
        Map<String, Object> serdeProps = new HashMap<>();
        serdeProps.put("json.value.type", classOf);
        final Serializer<T> mySerializer = new KafkaJsonSerializer<>();
        mySerializer.configure(serdeProps, false);

        final Deserializer<T> myDeserializer = new KafkaJsonDeserializer<>();
        myDeserializer.configure(serdeProps, false);
        return Serdes.serdeFrom(mySerializer, myDeserializer);
    }

}
