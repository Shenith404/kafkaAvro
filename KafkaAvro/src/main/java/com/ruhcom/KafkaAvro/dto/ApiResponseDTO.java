package com.ruhcom.KafkaAvro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO {
    private  String code;
    private String message;
    private Object data;
    private boolean success;
}
