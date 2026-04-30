package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FavoriteFlag {
    UNREGISTERED("未お気に入り", 1),
    REGISTERED("お気に入り", 2);

    private final String label;
    private final int code;

    public static FavoriteFlag getByCode(int code) {
        for( FavoriteFlag flag : FavoriteFlag.values() ) {
            if (flag.getCode() == code) {
                return flag;
            }
        }
        return null;
    }
}
