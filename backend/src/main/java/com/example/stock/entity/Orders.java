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
@Table(name = "orders")
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "stock_id", nullable = false)
    private Integer stockId;

    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;
    
    @Column(name = "order_side", nullable = false)
    private Integer orderSide;
    
    @Column(name = "order_method", nullable = false)
    private Integer orderMethod;
    
    @Column(name = "limit_price", precision = 10, scale = 2)
    private BigDecimal limitPrice;
    
    @Column(name = "order_at", nullable = false)
    private LocalDateTime orderAt;
    
    @Column(name = "executed_price", precision = 10, scale = 2)
    private BigDecimal executedPrice;
    
    @Column(name = "executed_at")
    private LocalDateTime executedAt;
    
    @Column(name = "order_execution_status", nullable = false)
    private Integer orderExecutionStatus;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
