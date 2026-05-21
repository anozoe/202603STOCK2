package com.example.stock.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stock.dto.AssetsStockProjection;
import com.example.stock.dto.HoldingStockProjection;
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
            as1.user_id,
            as1.stock_id,
            m.ticker_code,
            as1.holding_amount,
            as1.average_price,
            m.current_price,
            as1.market_value,
            as1.holding_amount * m.current_price AS expected_mv,
            as1.profit_loss,
            as1.updated_at AS asset_updated,
            m.updated_at  AS market_updated
            FROM newstock.assets_stock as1
            JOIN newstock.markets m ON m.stock_id = as1.stock_id
            WHERE as1.holding_amount > 0
            AND as1.user_id = :userId;
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

    @Query(value = """
                SELECT
                    m.ticker_code,
                    m.stock_name,
                    m.market,
                    m.current_price,
                    m.price_change,
                    m.change_rate,
                    SUM(a.average_price * a.holding_amount) AS total_cost,
                    SUM(a.market_value)                     AS total_market_value,
                    SUM(a.holding_amount)  AS total_holding_amount
                FROM newstock.markets m
                INNER JOIN newstock.assets_stock a ON a.stock_id = m.id
                WHERE a.user_id = :userId
                GROUP BY
                    m.ticker_code,
                    m.stock_name,
                    m.market,
                    m.current_price,
                    m.price_change,
                    m.change_rate
                HAVING SUM(a.holding_amount)
            """,
            nativeQuery = true
        )
    List<HoldingStockProjection> findHoldingStock(@Param("userId") Integer userId);
}
