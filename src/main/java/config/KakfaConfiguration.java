package config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configurable
public class KakfaConfiguration {

//consumer de kafka
    public Map<String, Object> config(){
        Map<String, Object>props=new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "batch");

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true);

//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
//                "10");
//        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
//                "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        return props;
    }
    @Bean
    public ConsumerFactory<String,String> consumerFactory(){

       return new DefaultKafkaConsumerFactory<>(config());
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
//        tres hilos
        factory.setConcurrency(3);

        return factory;
    }

//    producer kafka
private Map<String, Object> producerProps() { Map<String, Object> props=new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9092");
//    props.put(ProducerConfig.RETRIES_CONFIG, 0);
//    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
//    props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
//    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 16384);
    props.put(ConsumerConfig.GROUP_ID_CONFIG,
            "batch");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
    return props;
}

    @Bean
    public KafkaTemplate<String, String> createTemplate() {
        Map<String, Object>senderProps= producerProps();
        ProducerFactory<String, String> pf= new DefaultKafkaProducerFactory<String, String>(senderProps);

//        agregando metricas

        pf.addListener(new MicrometerProducerListener<String,String>(meterRegistry()));
        KafkaTemplate<String, String> template=new KafkaTemplate<>(pf);
        return template;
    }

//    metricas

    @Bean
    public MeterRegistry meterRegistry() {
        PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        return meterRegistry;
    }
}
