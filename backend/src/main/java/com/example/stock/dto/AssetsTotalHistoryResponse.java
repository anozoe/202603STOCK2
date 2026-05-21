package com.example.stock.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetsTotalHistoryResponse {
    private Integer userId;
    private BigDecimal totalAssets;
    private LocalDateTime createdAt;
}
