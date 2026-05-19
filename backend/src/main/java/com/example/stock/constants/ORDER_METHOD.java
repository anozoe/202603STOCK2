package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ORDER_METHOD {
    MarketOrder("成行", 1),
    LimitOrder("指値", 2);

    private final String label;
    private final int code;

    public static ORDER_METHOD getByCode(int code) {
        for( ORDER_METHOD side : ORDER_METHOD.values() ) {
            if ( side.getCode() == code) {
                return side;
            }
        }
        return null;
    }
}
