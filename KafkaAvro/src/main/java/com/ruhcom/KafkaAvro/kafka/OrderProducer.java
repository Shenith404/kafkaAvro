package com.ruhcom.KafkaAvro.kafka;

import com.ruhcom.avro.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String ordersTopic;


    public OrderProducer(KafkaTemplate<String, Object> kafkaTemplate,
                         @Value("${app.kafka.topic}") String ordersTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.ordersTopic = ordersTopic;
    }


    public void sendOrder(Order order) {
        kafkaTemplate.send(ordersTopic, order.getOrderId().toString(), order)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Order sent successfully: {}", order);
                    } else {
                        log.error("Error while sending order: {}", order, ex);
                    }
                });
    }
}
