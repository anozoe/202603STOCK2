package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock.entity.Stock;

public interface StockInfoRepository extends JpaRepository<StockInfoRepository, Integer>{
    
    //TODO: エラー修正
    //TODO:stock→stock_info変更
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
