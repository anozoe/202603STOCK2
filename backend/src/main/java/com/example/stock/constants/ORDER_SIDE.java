package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ORDER_SIDE {
    BUY("買い", 1),
    SELL("売り", 2);

    private final String label;
    private final int code;

    public static ORDER_SIDE getByCode(int code) {
        for( ORDER_SIDE side : ORDER_SIDE.values() ) {
            if ( side.getCode() == code) {
                return side;
            }
        }
        return null;
    }
}
