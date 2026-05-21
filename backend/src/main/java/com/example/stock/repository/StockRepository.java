package com.example.stock.repository;

import com.example.stock.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByTickerCode(String tickerCode);

    Optional<Stock> findTopByOrderByDisplayOrderDesc();

    List<Stock> findByIdIn(List<Long> ids);

    boolean existsByTickerCodeAndIdNot(String tickerCode, Long id);

    /*~~(class org.openrewrite.java.tree.J$Erroneous cannot be cast to class org.openrewrite.java.tree.J$Assignment (org.openrewrite.java.tree.J$Erroneous and org.openrewrite.java.tree.J$Assignment are in unnamed module of loader 'app'))~~>*/@Query("SELECT s FROM Stock s ORDER BY s.displayOrder asc, s.id asc")
    Page<Stock> findAllByOrderByDisplayOrderAscIdAsc(Pageable pageable);

    @Query("SELECT s FROM Stock s WHERE UPPER(s.tickerCode) LIKE UPPER(:tickerCodeKeyword) ESCAPE '\\' OR UPPER(s.stockName) LIKE UPPER(:stockNameKeyword) ESCAPE '\\' ORDER BY s.displayOrder asc, s.id asc")
    Page<Stock> findByTickerCodeContainingIgnoreCaseOrStockNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(
            String tickerCodeKeyword,
            String stockNameKeyword,
            Pageable pageable
    );

    @Query("""
        SELECT s FROM Stock s
        WHERE s.id NOT IN (
            SELECT m.stockId FROM Markets m
        )
        """)
    List<Stock> findStocksWithoutMarket();

    List<Stock> findByTickerCodeIn(Collection<String> tickerCodes);
}