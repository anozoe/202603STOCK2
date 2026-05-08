package com.example.stock.batch.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionInfoResponse {
    private Integer orderId;
    private Integer userId;
    private Integer stockId;
    private Integer orderQuantity;
    private Integer orderSide;
    private Integer orderMethod;
    private BigDecimal limitPrice;
    private LocalDateTime orderAt;
    private BigDecimal executedPrice;
    private LocalDateTime executedAt;
    private Integer orderExecutionStatus;
    private BigDecimal averagePrice;
    private BigDecimal profitLoss;
    private BigDecimal profitLossRatio;
    private BigDecimal marketValue;
    private Integer holdingAmount;
    private BigDecimal buyingPower;
    private BigDecimal holdingsValue;
    private BigDecimal totalAssets;
    private BigDecimal unrealizedPnl;
    private BigDecimal unrealizedPnlRatio;
    private BigDecimal currentPrice;

}
