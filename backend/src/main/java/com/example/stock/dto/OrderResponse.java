package com.example.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer orderId;
    private Integer orderExecutedStatus;
    private BigDecimal executedPrice;
    private LocalDateTime executedAt;
}
