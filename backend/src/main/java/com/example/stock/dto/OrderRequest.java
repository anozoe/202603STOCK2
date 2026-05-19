package com.example.stock.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderRequest {
    private Integer stockId;
    private Integer userId;
    private String tickerCode;
    private Integer orderQuantity;
    private Integer orderSide;
    private Integer orderMethod;
    private BigDecimal limitPrice;
    private BigDecimal buyingPower;
    private Integer holdingAmount;
    private BigDecimal currentPrice;
}
