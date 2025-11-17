package com.ruhcom.KafkaAvro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderDTO {
    @NotBlank(message = "orderId is mandatory")
    private String orderId;
    @NotBlank(message = "product is mandatory")
    private String product;
    @NotBlank(message = "price is mandatory")
    private float price;
}
