package com.ruhcom.KafkaAvro.services;


import com.ruhcom.KafkaAvro.dto.CreateOrderDTO;

public interface OrderService {
    public void createOrder(CreateOrderDTO order);
}
