package com.example.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SumHoldingAmountResponse {
    private Integer userId;
    private String tickerCode;
    private Integer sumHoldingAmount;
}
