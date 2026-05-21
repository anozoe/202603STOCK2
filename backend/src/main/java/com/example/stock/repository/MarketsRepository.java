package com.example.stock.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stock.entity.Markets;
import com.example.stock.entity.Stock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;





public interface MarketsRepository extends JpaRepository<Markets, Integer>{
    @Modifying
    @Query("""
            UPDATE Markets m
            SET m.finishPrice = m.currentPrice,
                m.updatedAt = CURRENT_TIMESTAMP
            """)
    public int updateFinishPriceToCurrentPrice();

    Optional<Markets> findByTickerCode(String tickerCode);

    @Modifying
    void deleteByStockId(Long stocksId);

    @Query("""
        SELECT m FROM Markets m
        JOIN Stock s ON s.id = m.stockId
        ORDER BY s.displayOrder ASC, m.id ASC
        """)
    Page<Markets> findAllByOrderByDisplayOrderAscIdAsc(Pageable pageable);

    @Query("""
        SELECT m FROM Markets m
        JOIN Stock s ON s.id = m.stockId
        WHERE LOWER(m.tickerCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(m.stockName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY s.displayOrder ASC, m.id ASC
        """)
    Page<Markets> searchByKeywordOrderByDisplayOrderAscIdAsc(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Markets> findByTickerCodeIn(Collection<String> tickerCodes);
}
