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
@Table(name = "assets_stock")
public class AssetsStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "stock_id", nullable = false)
    private Integer stockId;
    
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "average_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal averagePrice;
    
    @Column(name = "profit_loss", nullable = false, precision = 10, scale = 2)
    private BigDecimal profitLoss;
    
    @Column(name = "profit_loss_ratio", nullable = false, precision = 5, scale = 2)
    private BigDecimal profitLossRatio;

    @Column(name = "market_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal marketValue;
    
    @Column(name = "holding_amount", nullable = false)
    private Integer holdingAmount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
