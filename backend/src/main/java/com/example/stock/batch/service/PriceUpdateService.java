package com.example.stock.batch.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stock.batch.dto.BatchResultDto;
import com.example.stock.entity.Markets;
import com.example.stock.repository.MarketsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceUpdateService {
    private final MarketsRepository marketsRepository;

    // 株価変動バッチ
    public BatchResultDto updateCurrentPrice() {
        // 1. 全銘柄取得
        List<Markets> marketsList = marketsRepository.findAll();
        int updateCount = 0;
        int failCount = 0;
        // 2. 現在値安値高値更新
        for (Markets market : marketsList) {
            try {
                BigDecimal currentPrice = market.getCurrentPrice();
                BigDecimal newPrice = calcCurrentPrice(currentPrice);
                market.setCurrentPrice(newPrice);
                updateHighPrice(market, newPrice);
                updateLowPrice(market, newPrice);
                updateCount++;
            } catch (Exception e) {
                failCount++;
                System.out.println(market.getTickerCode()+"が更新できませんでした。");
            }
        }
        marketsRepository.saveAll(marketsList);
        return new BatchResultDto(updateCount, failCount, "SUCCESS");
    }
    
    // 現在値計算
    private BigDecimal calcCurrentPrice(BigDecimal currentPrice) {
        double randomValue = 1 + (Math.random() * 0.06 - 0.03);
        BigDecimal calcPrice = currentPrice.multiply(BigDecimal.valueOf(randomValue));
        return calcPrice.setScale(2, RoundingMode.HALF_UP);
    }

    //　高値更新
    private void updateHighPrice(Markets market, BigDecimal newPrice) {
        BigDecimal currentHighPrice = market.getHighPrice();
        if (newPrice.compareTo(currentHighPrice) == 1) {
            market.setHighPrice(newPrice);
        } 
    }
    
    // 安値更新
    private void updateLowPrice(Markets market, BigDecimal newPrice) {
        BigDecimal currentLowPrice = market.getLowPrice();
        if (newPrice.compareTo(currentLowPrice) == -1) {
            market.setLowPrice(newPrice);
        } 
    }

}
