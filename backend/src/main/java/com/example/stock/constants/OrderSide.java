package com.example.stock.constants;


public enum OrderSide {
    BUY("買い", 1),
    SELL("売り", 2);

    private String label;
    private int code;
    
    private OrderSide(String label, int code){
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }

    public static OrderSide getByCode(int code) {
        for( OrderSide side : OrderSide.values() ) {
            if ( side.getCode() == code) {
                return side;
            }
        }
        return null;
    }
}
