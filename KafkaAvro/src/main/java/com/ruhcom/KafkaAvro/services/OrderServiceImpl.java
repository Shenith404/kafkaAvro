package com.ruhcom.KafkaAvro.services;

import com.ruhcom.KafkaAvro.dto.CreateOrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {


    @Override
    public void createOrder(CreateOrderDTO order) {
        // Implementation logic to create an order
    }
}
