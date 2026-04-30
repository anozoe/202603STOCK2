package com.example.stock.constants;

public enum OrderStatus {
    UNEXECUTED("未約定", 1),
    EXECUTED("約定済", 2),
    CANCEL("キャンセル", 3);

    private String label;
    private int code;
    
    private OrderStatus(String label, int code){
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }

    public static OrderStatus getByCode(int code) {
        for( OrderStatus status : OrderStatus.values() ) {
            if ( status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
