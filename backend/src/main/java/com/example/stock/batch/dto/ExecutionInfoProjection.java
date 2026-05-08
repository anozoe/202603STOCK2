package com.example.stock.batch.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;




public interface ExecutionInfoProjection {
    Integer getOrderId();
    Integer getUserId();
    Integer getStockId();
    Integer getOrderQuantity();
    Integer getOrderSide();
    Integer getOrderMethod();
    BigDecimal getLimitPrice();
    LocalDateTime getOrderAt();
    BigDecimal getExecutedPrice();
    LocalDateTime getExecutedAt();
    Integer getOrderExecutionStatus();
    BigDecimal getAveragePrice();
    BigDecimal getProfitLoss();
    BigDecimal getProfitLossRatio();
    BigDecimal getMarketValue();
    Integer getHoldingAmount();
    BigDecimal getBuyingPower();
    BigDecimal getHoldingsValue();
    BigDecimal getTotalAssets();
    BigDecimal getUnrealizedPnl();
    BigDecimal getUnrealizedPnlRatio();
    BigDecimal getCurrentPrice();
}

    
