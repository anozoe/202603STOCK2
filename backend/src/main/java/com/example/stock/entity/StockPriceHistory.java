package com.example.stock.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "stock_price_histories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stock_price_histories_stock_id_price_date", columnNames = {"stock_id", "price_date"})
        }
)
public class StockPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "stock_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Stock stock;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(name = "open_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal openPrice;

    @Column(name = "high_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal highPrice;

    @Column(name = "low_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal closePrice;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}