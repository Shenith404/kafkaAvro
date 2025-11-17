package com.ruhcom.KafkaAvro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @NotNull(message = "price is mandatory")
    @Positive(message = "price must be positive")
    private float price;
}
