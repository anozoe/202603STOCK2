package com.example.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarketsDetailResponse {
    private Integer id;
    private String tickerCode;
    private String stockName;
    private String market;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal finishPrice;
    private BigDecimal priceChange;
    private BigDecimal changeRate;
    private LocalDateTime updatedAt;
}
