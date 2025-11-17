package com.ruhcom.KafkaAvro.controller;

import com.ruhcom.KafkaAvro.dto.ApiResponseDTO;
import com.ruhcom.KafkaAvro.dto.CreateOrderDTO;
import com.ruhcom.KafkaAvro.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderDTO order) {
        orderService.createOrder(order);
        return ResponseEntity.ok().body(new ApiResponseDTO("200", "Order created successfully",null,true));
    }
}
