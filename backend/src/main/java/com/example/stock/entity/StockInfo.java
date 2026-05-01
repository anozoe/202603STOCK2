package com.example.stock.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_info")
public class StockInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_id", nullable = false, unique = true)
    private Integer stockId;

    @Column(name = "dividend_yield", precision = 8, scale = 2, nullable = false)
    private BigDecimal dividendYield;
    
    @Column(name = "per", precision = 10, scale = 2, nullable = false)
    private BigDecimal per;
    
    @Column(name = "pbr", precision = 10, scale = 2, nullable = false)
    private BigDecimal pbr;

    @Column(name = "roe", precision = 10, scale = 2, nullable = false)
    private BigDecimal roe;

    @Column(name = "volume", nullable = false)
    private Long volume;

    @Column(name = "market_cap", nullable = false)
    private Long marketCap;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    @Column(name = "market", nullable = false)
    private Integer market;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}