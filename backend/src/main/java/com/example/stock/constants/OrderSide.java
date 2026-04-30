package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderSide {
    BUY("買い", 1),
    SELL("売り", 2);

    private final String label;
    private final int code;

    public static OrderSide getByCode(int code) {
        for( OrderSide side : OrderSide.values() ) {
            if ( side.getCode() == code) {
                return side;
            }
        }
        return null;
    }
}
