package com.example.stock.constants;

public final class MarketCode {

    private MarketCode() {
    }

    public static final int NASDAQ = 1;
    public static final int NYSE = 2;
    public static final int AMEX = 3;
    
    public static String toName(Integer code) {
    if (code == null) return null;
    return switch (code) {
        case NASDAQ -> "NASDAQ";
        case NYSE -> "NYSE";
        case AMEX -> "AMEX";
        default -> null;
    };
    }
}