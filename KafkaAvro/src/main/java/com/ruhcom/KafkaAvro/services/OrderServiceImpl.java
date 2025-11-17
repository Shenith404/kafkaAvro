package com.ruhcom.KafkaAvro.services;

import com.ruhcom.KafkaAvro.dto.CreateOrderDTO;
import com.ruhcom.KafkaAvro.kafka.OrderProducer;
import com.ruhcom.avro.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderProducer orderProducer;

    @Override
    public void createOrder(CreateOrderDTO order) {
        // Convert CreateOrderDTO to Avro Order object
        com.ruhcom.avro.Order avroOrder = Order.newBuilder()
                .setOrderId(order.getOrderId())
                .setProduct(order.getProduct())
                .setPrice(order.getPrice())
                .build();

        // Send the Avro Order object to Kafka
        orderProducer.sendOrder(avroOrder);
    }
}
