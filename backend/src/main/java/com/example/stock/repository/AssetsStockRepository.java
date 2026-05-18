package com.example.stock.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stock.dto.AssetsStockProjection;
import com.example.stock.dto.SumHoldingAmountProjection;
import com.example.stock.entity.AssetsStock;

public interface AssetsStockRepository extends JpaRepository<AssetsStock, Integer> {
    /**
     * 売却約定判定補助クエリ
     * 保有銘柄を古い順で取得
     * @param userId　  ユーザID
     * @param stockId   銘柄ID
     * @return          古い順の保有リスト
     */
    @Query(value = """
            SELECT *
            FROM newstock.assets_stock
            WHERE user_id = :userId
            AND stock_id = :stockId
            AND holding_amount > 0
            ORDER BY created_at ASC, id ASC
            """, nativeQuery = true
        )
    List<AssetsStock>findHoldingsForSell(
        @Param("userId") Integer userId,
        @Param("stockId") Integer stockId
    );
    Optional<AssetsStock> findByOrderId(Integer orderId);

    @Query(value = """
            SELECT 
                s.id,
                s.user_id,
                m.ticker_code,
                s.average_price,
                s.profit_loss,
                s.profit_loss_ratio,
                s.market_value,
                s.holding_amount
            FROM newstock.assets_stock s
            JOIN newstock.markets m ON s.stock_id = m.id
            WHERE s.user_id = :userId
            AND s.holding_amount > 0
            """, 
            nativeQuery = true
        )
    List<AssetsStockProjection> findAssetsStockInfo(Integer userId);

    @Query(value = """
                SELECT 
                    s.user_id,
                    m.ticker_code,
                    SUM(s.holding_amount) AS sum_holding_amount
                FROM newstock.assets_stock s
                LEFT JOIN newstock.markets m ON s.stock_id = m.id
                WHERE m.ticker_code = :tickerCode
                AND user_id = :userId
            """, 
            nativeQuery = true
        )
    Optional<SumHoldingAmountProjection> findSumHoldingAmount(Integer userId, String tickerCode);

}
