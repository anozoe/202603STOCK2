package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock.entity.AssetsTotal;

public interface AssetsTotalRepository extends JpaRepository<AssetsTotal, Integer> {
    
}
