package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChartType {
    LINE("折れ線", 1),
    CANDLE("ローソク", 2);

    private final String label;
    private final int code;
    
    public static ChartType getByCode(int code) {
        for( ChartType type : ChartType.values() ) {
            if ( type.getCode() == code) {
                return type;        
            }
        }
        return null;
    }

}
