package com.example.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarketsDetailResponse {
    private String tickerCode;
    private String stockName;
    private String market;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal changeRate;
    private LocalDateTime updatedAt;
}
