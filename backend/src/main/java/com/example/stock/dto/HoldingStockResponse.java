package com.example.stock.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingStockResponse {
    private String tickerCode;
    private String stockName;
    private String market;
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal changeRate;
    private BigDecimal totalCost;
    private BigDecimal totalMarketValue;
    private Long totalHoldingAmount;
}
