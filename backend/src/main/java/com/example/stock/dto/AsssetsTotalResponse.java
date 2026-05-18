package com.example.stock.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AsssetsTotalResponse {
    private BigDecimal buyingPower;
    private BigDecimal holdingValue;
    private BigDecimal totalAssets;
    private BigDecimal unrealizedPnl;
    private BigDecimal unrealizedPnlRatio;
}
