package com.example.stock.repository;

import com.example.stock.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByTickerCode(String tickerCode);

    Optional<Stock> findTopByOrderByDisplayOrderDesc();

    List<Stock> findByIdIn(List<Long> ids);

    boolean existsByTickerCodeAndIdNot(String tickerCode, Long id);

    Page<Stock> findAllByOrderByDisplayOrderAscIdAsc(Pageable pageable);

    Page<Stock> findByTickerCodeContainingIgnoreCaseOrStockNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(
            String tickerCodeKeyword,
            String stockNameKeyword,
            Pageable pageable
    );
}