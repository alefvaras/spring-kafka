package com.example.springkafka;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class SpringKafkaApplication  {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private MeterRegistry meterRegistry;

    private static final Logger logger = LoggerFactory.getLogger(SpringKafkaApplication.class);
    public static void main(String[] args) {

        SpringApplication.run(SpringKafkaApplication.class, args);


    }

    @KafkaListener(id="ale-id",autoStartup = "true",topics  = "ale-topic3",containerFactory = "kafkaListenerContainerFactory" ,groupId = "batch",properties =
            {"max.poll.interval.ms:4000",
                    "max.poll.records:50"})
    public void listen(List<ConsumerRecord<String,String>> messages) {
        logger.info("reading message");
       for (ConsumerRecord<String,String> message : messages) {
//           logger.info("message received partition={}, key={}, value={}",message.partition(),message.key(),message.value());
       }

        logger.info("complete batch");
    }

//    @Override
//    public void run(String... args) throws Exception {
//
//        for (int i=0; i<100;i++){
//
//            ListenableFuture<SendResult<String,String>>future= kafkaTemplate.send("ale-topic3",String.valueOf(i),String.format("sample message %d",i));
//            future.addCallback(new KafkaSendCallback<String,String>(){
//
//                @Override
//                public void onSuccess(SendResult<String, String> stringStringSendResult) {
//                    logger.info("message send result: " + stringStringSendResult.getRecordMetadata().offset());
//                }
//
//                @Override
//                public void onFailure(KafkaProducerException e) {
//                    logger.error("message send exception: " + e.getMessage());
//                }
//            });
//        }
//
////        logger.info("wait thread");
////        Thread.sleep(5000);
////        registry.getListenerContainer("ale-id").start();
////        logger.info("stop thread");
////        registry.getListenerContainer("ale-id").stop();
//    }

    @Scheduled(fixedDelay = 2000, initialDelay = 100)
    public void sendKafkaMesseger() {
                for (int i=0; i<100;i++){

            ListenableFuture<SendResult<String,String>>future= kafkaTemplate.send("ale-topic3",String.valueOf(i),String.format("sample message %d",i));
            future.addCallback(new KafkaSendCallback<String,String>(){

                @Override
                public void onSuccess(SendResult<String, String> stringStringSendResult) {
                    logger.info("message send result: " + stringStringSendResult.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(KafkaProducerException e) {
                    logger.error("message send exception: " + e.getMessage());
                }
            });
        }

    }

    @Scheduled(fixedDelay = 2000, initialDelay = 500)
    public void messageCountMetric() {

       List<Meter> metric= meterRegistry.getMeters();
       for (Meter meter : metric) {
           logger.info(" meter {}", meter.getId().getName());
       }
        double count = meterRegistry.get("kafka.producer.record.send.total")
                .functionCounter().count();
        logger.info("Count {} ",count);
    }
}
