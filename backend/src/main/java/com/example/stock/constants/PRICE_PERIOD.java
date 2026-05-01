package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PRICE_PERIOD {
    DAY("1日", 1),
    WEEK("1週間", 2);

    private final String label;
    private final int code;

    public static PRICE_PERIOD getByCode(int code) {
        for( PRICE_PERIOD period : PRICE_PERIOD.values() ) {
            if ( period.getCode() == code) {
                return period;
            }
        }
        return null;
    }
}
