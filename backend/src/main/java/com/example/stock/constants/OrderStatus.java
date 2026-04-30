package com.example.stock.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    UNEXECUTED("未約定", 1),
    EXECUTED("約定済", 2),
    CANCEL("キャンセル", 3);

    private final String label;
    private final int code;

    public static OrderStatus getByCode(int code) {
        for( OrderStatus status : OrderStatus.values() ) {
            if ( status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
