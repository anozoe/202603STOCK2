package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetsStockRepository extends JpaRepository<AssetsStockRepository, Integer> {
    //TODO:保有銘柄一覧取得API
}
