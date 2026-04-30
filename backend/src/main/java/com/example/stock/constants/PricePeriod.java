package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PricePeriod {
    DAY("1日", 1),
    WEEK("1週間", 2);

    private final String label;
    private final int code;

    public static PricePeriod getByCode(int code) {
        for( PricePeriod period : PricePeriod.values() ) {
            if ( period.getCode() == code) {
                return period;
            }
        }
        return null;
    }
}
