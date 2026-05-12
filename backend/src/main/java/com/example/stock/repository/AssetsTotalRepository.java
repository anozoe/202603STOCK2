package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock.entity.AssetsTotal;
import java.util.Optional;


public interface AssetsTotalRepository extends JpaRepository<AssetsTotal, Integer> {
    Optional<AssetsTotal> findByUserId(Integer userId);
}
