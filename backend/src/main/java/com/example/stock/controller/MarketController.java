package com.example.stock.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock.dto.MarketsDetailResponse;
import com.example.stock.service.MarketsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/markets")
@CrossOrigin(origins = "http://localhost:3000")
public class MarketController {
    private final MarketsService marketsService;

    public MarketController (MarketsService marketsService){
        this.marketsService = marketsService;
    }
    @GetMapping("/{tickerCode}")
    public MarketsDetailResponse getMarketDetail (@PathVariable String tickerCode) {
        return marketsService.getMarketDetail(tickerCode);
    }
    
}
