package com.example.stock.dto;

import java.math.BigDecimal;

public interface AssetsStockProjection {
    Integer getId();
    Integer getUserId();
    String gettickerCode();
    BigDecimal getAveragePrice();
    BigDecimal getProfitLoss();
    BigDecimal getProfitLossRatio();
    BigDecimal getMarketValue();
    Integer getHoldingAmount();
} 