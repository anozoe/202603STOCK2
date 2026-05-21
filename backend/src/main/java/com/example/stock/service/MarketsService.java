package com.example.stock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.dto.MarketsDetailResponse;
import com.example.stock.entity.Markets;
import com.example.stock.repository.MarketsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketsService {
    private final MarketsRepository marketsRepository;

    @Transactional
    public MarketsDetailResponse getMarketDetail(String tickerCode) {
        Markets market = marketsRepository.findByTickerCode(tickerCode).orElseThrow();
        MarketsDetailResponse detail = new MarketsDetailResponse(
            market.getId(),
            market.getTickerCode(), 
            market.getStockName(),
            market.getMarket(),
            market.getCurrentPrice(),
            market.getPreviousClose(),
            market.getOpenPrice(),
            market.getHighPrice(),
            market.getLowPrice(),
            market.getFinishPrice(),
            market.getPriceChange(),
            market.getChangeRate(),
            market.getUpdatedAt()    
        );

        return detail;
    }
}
