package com.example.stock.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "markets")
public class markets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ticker_code", nullable = false, unique = true, length = 20)
    private String tickerCode;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    @Column(name = "market", nullable = false, length = 100)
    private String market;

    @Column(name = "current_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "previous_close", nullable = false, precision = 10, scale = 2)
    private BigDecimal previousClose;

    
    private BigDecimal open_price;
    private BigDecimal high_price;
    private BigDecimal low_price;
    private BigDecimal finish_price;
    private BigDecimal price_change;
    private BigDecimal change_rate;
    private LocalDateTime updated_at;
    private LocalDateTime created_at;
}
