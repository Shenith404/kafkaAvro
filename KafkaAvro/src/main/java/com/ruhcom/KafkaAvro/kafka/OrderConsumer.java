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
        throw new RuntimeException("Transient error for order " + order.getOrderId());

//        try {
//            processOrder(order);
//        } catch (TransientProcessingException t) {
//// throw RuntimeException to trigger retry via DefaultErrorHandler
//            throw new RuntimeException("Transient error for order " + order.getOrderId(), t);
//        } catch (PermanentProcessingException p) {
//// non-retryable -> mark as permanent by throwing IllegalArgumentException
//            throw new IllegalArgumentException("Permanent error for order " + order.getOrderId(), p);
//        }
    }


    private void processOrder(Order order) throws TransientProcessingException, PermanentProcessingException {
        double dice = Math.random();
        if (dice %2==0) {
            throw new PermanentProcessingException("Bad data");
        } else if (dice %3==0) {
            throw new TransientProcessingException("Temporary downstream issue");
        }
// success otherwise
    }


    public static class TransientProcessingException extends Exception { public TransientProcessingException(String m){super(m);} }
    public static class PermanentProcessingException extends Exception { public PermanentProcessingException(String m){super(m);} }
}
