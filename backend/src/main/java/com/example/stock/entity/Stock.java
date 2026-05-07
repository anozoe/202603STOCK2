package com.example.stock.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticker_code", nullable = false, unique = true, length = 20)
    private String tickerCode;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    @Column(name = "market", nullable = false)
    private Integer market;

    @Column(name = "market_status", length = 10)
    private String marketStatus;

    @Column(name = "current_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "price_change", precision = 18, scale = 2)
    private BigDecimal priceChange;

    @Column(name = "change_rate", precision = 8, scale = 2)
    private BigDecimal changeRate;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "open_price", precision = 18, scale = 2)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 18, scale = 2)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 18, scale = 2)
    private BigDecimal lowPrice;

    @Column(name = "close_price", precision = 18, scale = 2)
    private BigDecimal closePrice;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "pbr", precision = 12, scale = 4)
    private BigDecimal pbr;

    @Column(name = "per", precision = 12, scale = 4)
    private BigDecimal per;

    @Column(name = "roe", precision = 12, scale = 4)
    private BigDecimal roe;

    @Column(name = "dividend_yield", precision = 12, scale = 4)
    private BigDecimal dividendYield;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;
}