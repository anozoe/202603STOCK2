package com.example.stock.constants;

public enum PricePeriod {
    DAY("1日", 1),
    WEEK("1週間", 2);

    private String label;
    private int code;
    
    private PricePeriod(String label, int code){
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }

    public static PricePeriod getByCode(int code) {
        for( PricePeriod period : PricePeriod.values() ) {
            if ( period.getCode() == code) {
                return period;
            }
        }
        return null;
    }
}
