package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FAVORITE_FLAG {
    UNREGISTERED("未お気に入り", 1),
    REGISTERED("お気に入り", 2);

    private final String label;
    private final int code;

    public static FAVORITE_FLAG getByCode(int code) {
        for( FAVORITE_FLAG flag : FAVORITE_FLAG.values() ) {
            if (flag.getCode() == code) {
                return flag;
            }
        }
        return null;
    }
}
