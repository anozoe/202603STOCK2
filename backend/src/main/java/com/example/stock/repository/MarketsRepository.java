package com.example.stock.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.stock.entity.Markets;



public interface MarketsRepository extends JpaRepository<Markets, Integer>{
    @Modifying
    @Query("""
            UPDATE Markets m
            SET m.finishPrice = m.currentPrice,
                m.updatedAt = CURRENT_TIMESTAMP
            """)
    public int updateFinishPriceToCurrentPrice();

}
