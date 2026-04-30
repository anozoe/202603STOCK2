package com.example.stock.constants;

public enum DayType {
    FRIDAY("金曜", 1),
    OTHER("それ以外", 2);

    private String label;
    private int code;

    private DayType(String label, int code){
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }

    public static DayType getByCode(int code) {
        for( DayType type : DayType.values() ) {
            if ( type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

}
