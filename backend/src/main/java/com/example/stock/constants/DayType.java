package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DayType {
    FRIDAY("金曜", 1),
    OTHER("それ以外", 2);

    private final String label;
    private final int code;

    public static DayType getByCode(int code) {
        for( DayType type : DayType.values() ) {
            if ( type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

}
