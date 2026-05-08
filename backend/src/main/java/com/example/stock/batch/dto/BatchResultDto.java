package com.example.stock.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResultDto {
    private Integer updateCount;
    private Integer failCount;
    private String resultCode;
}
