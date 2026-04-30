package com.example.stock.constants;

public enum ChartType {
    LINE("折れ線", 1),
    CANDLE("ローソク", 2);

    private String label;
    private int code;
    
    private ChartType(String label, int code){
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }

    public static ChartType getByCode(int code) {
        for( ChartType type : ChartType.values() ) {
            if ( type.getCode() == code) {
                return type;        
            }
        }
        return null;
    }

}
