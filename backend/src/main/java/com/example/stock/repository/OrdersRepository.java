package com.example.stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.stock.batch.dto.ExecutionInfoProjection;
import com.example.stock.entity.Orders;

public interface OrdersRepository extends JpaRepository<Orders, Integer>{
    // 約定情報取得API
    @Query(value = """
        SELECT 
            o.id AS order_id, 
            o.user_id, 
            o.stock_id, 
            o.order_quantity, 
            o.order_side, 
            o.order_method, 
            o.limit_price, 
            o.order_at, 
            o.executed_price, 
            o.executed_at, 
            o.order_execution_status, 
            a.average_price, 
            a.profit_loss, 
            a.profit_loss_ratio, 
            a.market_value, 
            a.holding_amount, 
            t.buying_power, 
            t.holdings_value, 
            t.total_assets, 
            t.unrealized_pnl, 
            t.unrealized_pnl_ratio, 
            m.current_price
        FROM orders o
        LEFT JOIN assets_stock a ON o.id = a.order_id
        JOIN assets_total t ON o.user_id = t.user_id
        JOIN markets m ON o.stock_id = m.id
        WHERE
        o.order_execution_status = 1
        AND
        o.order_method = 2
    """,
    nativeQuery = true)
    List<ExecutionInfoProjection> findExecutionInfo();

    List<Orders> findByOrderExecutionStatus(Integer orderExecutionStatus);

    
    
}
