package com.example.stock.constants;

public enum FavoriteFlag {
    UNREGISTERED("未お気に入り", 1),
    REGISTERED("お気に入り", 2);

    private String label;
    private int code;

    private FavoriteFlag(String label, int code) {
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }

    public static FavoriteFlag getByCode(int code) {
        for( FavoriteFlag flag : FavoriteFlag.values() ) {
            if (flag.getCode() == code) {
                return flag;
            }
        }
        return null;
    }
}
