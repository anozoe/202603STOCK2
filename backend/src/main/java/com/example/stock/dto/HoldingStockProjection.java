package com.example.stock.dto;

import java.math.BigDecimal;

public interface HoldingStockProjection {
    String getTickerCode();
    String getStockName();
    String getMarket();
    BigDecimal getCurrentPrice();
    BigDecimal getPriceChange();
    BigDecimal getChangeRate();
    BigDecimal getTotalCost();
    BigDecimal getTotalMarketValue();
    Long getTotalHoldingAmount(); 
    
} 