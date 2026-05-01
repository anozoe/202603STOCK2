package com.example.stock.repository;

import com.example.stock.entity.StockInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockInfo, Long> {

    Optional<StockInfo> findByTickerCode(String tickerCode);

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
