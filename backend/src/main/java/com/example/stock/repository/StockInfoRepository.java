package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock.entity.Stock;

public interface StockInfoRepository extends JpaRepository<StockInfoRepository, Integer>{
    
    //TODO: エラー修正
    //TODO:stock→stock_info変更
    Optional<Stock> findByTickerCode(String tickerCode);

    Optional<StockInfo> findTopByOrderByDisplayOrderDesc();

    List<StockInfo> findByIdIn(List<Long> ids);

    boolean existsByTickerCodeAndIdNot(String tickerCode, Long id);

    Page<StockInfo> findAllByOrderByDisplayOrderAscIdAsc(Pageable pageable);

    Page<StockInfo> findByTickerCodeContainingIgnoreCaseOrStockNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(
            String tickerCodeKeyword,
            String stockNameKeyword,
            Pageable pageable
    );
}
