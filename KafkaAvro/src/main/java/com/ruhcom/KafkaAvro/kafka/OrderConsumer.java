package com.ruhcom.KafkaAvro.kafka;

import com.ruhcom.avro.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {
    private static  final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Order order) {
        log.info("Received order: {}", order);
        int dice= (int)(Math.random()*10)+1;
        if(dice%2==0) {
            throw new RuntimeException("Transient error for order " + order.getOrderId());
        }
        log.info("Processed order successfully: {}", order.getOrderId());

    }

  }
