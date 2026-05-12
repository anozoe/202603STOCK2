package com.example.stock.batch.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.batch.dto.BatchResultDto;
import com.example.stock.dto.TwelveDataQuoteResponse;
import com.example.stock.entity.Markets;
import com.example.stock.repository.MarketsRepository;
import com.example.stock.service.TwelveDataClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarketsSetupService {
    private final MarketsRepository marketsRepository;
    private final TwelveDataClient twelveDataClient;
    
    // 始値設定バッチ
    @Transactional
    public BatchResultDto setOpenPrice() {
        // 1. 全銘柄取得
        List<Markets> marketsList = marketsRepository.findAll();
        // 2. 外部APIから終値取得
        int updateCount = 0;
        int failCount = 0;
        for (Markets market : marketsList) {
            String symbol = market.getTickerCode();
            try {
                TwelveDataQuoteResponse response = twelveDataClient.fetchQuote(symbol);
                System.out.println("symbol = " + symbol);
                System.out.println("response = " + response);
                System.out.println("close = " + response.getClose());
                BigDecimal previousClose = response.getClose() == null ? market.getPreviousClose() : new BigDecimal(response.getClose());
                market.setPreviousClose(previousClose);
                market.setCurrentPrice(previousClose);
                market.setOpenPrice(previousClose);
                market.setHighPrice(previousClose);
                market.setLowPrice(previousClose);
                market.setFinishPrice(null);
                market.setUpdatedAt(LocalDateTime.now());
                updateCount++;
            } catch (Exception e) {
                failCount++;
                System.out.println(symbol+"が取得できませんでした。");
            }            
        }
        marketsRepository.saveAll(marketsList);
        return new BatchResultDto(updateCount, failCount, "SUCCESS");
    }
}
