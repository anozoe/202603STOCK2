package com.example.stock.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.stock.entity.Markets;

public interface MarketsRepository extends JpaRepository<Markets, Integer>{

}
