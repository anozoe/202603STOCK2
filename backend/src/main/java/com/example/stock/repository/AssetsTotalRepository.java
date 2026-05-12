package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock.entity.AssetsTotal;
import java.util.List;


public interface AssetsTotalRepository extends JpaRepository<AssetsTotal, Integer> {
    List<AssetsTotal> findByUserId(Integer userId);
}
