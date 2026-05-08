package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock.entity.AssetsStock;

public interface AssetsStockRepository extends JpaRepository<AssetsStock, Integer> {
    
}
