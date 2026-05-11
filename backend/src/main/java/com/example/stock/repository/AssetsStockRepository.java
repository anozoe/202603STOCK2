package com.example.stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
