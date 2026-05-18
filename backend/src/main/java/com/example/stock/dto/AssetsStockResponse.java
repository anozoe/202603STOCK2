package com.example.stock.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetsStockResponse {
    private Integer id;
    private Integer userId;
    private String tickerCode;
    private BigDecimal averagePrice;
    private BigDecimal profitLoss;
    private BigDecimal profitLossRatio;
    private BigDecimal marketValue;
    private Integer holdingAmount;
}
