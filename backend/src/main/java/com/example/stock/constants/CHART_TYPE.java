package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CHART_TYPE {
    LINE("折れ線", 1),
    CANDLE("ローソク", 2);

    private final String label;
    private final int code;
    
    public static CHART_TYPE getByCode(int code) {
        for( CHART_TYPE type : CHART_TYPE.values() ) {
            if ( type.getCode() == code) {
                return type;        
            }
        }
        return null;
    }

}
