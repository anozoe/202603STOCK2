package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DAY_TYPE {
    FRIDAY("金曜", 1),
    OTHER("それ以外", 2);

    private final String label;
    private final int code;

    public static DAY_TYPE getByCode(int code) {
        for( DAY_TYPE type : DAY_TYPE.values() ) {
            if ( type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

}
