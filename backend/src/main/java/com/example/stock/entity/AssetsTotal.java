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
@Table(name = "assets_total")
public class AssetsTotal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "buying_power", nullable = false, precision = 10, scale = 2)
    private BigDecimal buyingPower;
    
    @Column(name = "holdings_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal holdingsValue;
    
    @Column(name = "total_assets", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAssets;
    
    @Column(name = "unrealized_pnl", nullable = false, precision = 10, scale = 2)
    private BigDecimal unrealizedPnl;
    
    @Column(name = "unrealized_pnl_ratio", nullable = false, precision = 5, scale = 2)
    private BigDecimal unrealizedPnlRatio;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
