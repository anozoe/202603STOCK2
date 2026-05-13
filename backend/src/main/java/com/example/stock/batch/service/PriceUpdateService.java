package com.example.stock.batch.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.batch.dto.BatchResultDto;
import com.example.stock.entity.Markets;
import com.example.stock.repository.MarketsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PriceUpdateService {
    private final MarketsRepository marketsRepository;

    // 株価変動バッチ
    public BatchResultDto updateCurrentPrice() {
        log.info("株価変動 開始");

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
                calcPriceRate(market, newPrice);
                market.setUpdatedAt(LocalDateTime.now());
                updateCount++;
            } catch (Exception e) {
                failCount++;
                System.out.println(market.getTickerCode()+"が更新できませんでした。");
                log.warn("株価更新失敗 tickerCode={]", market.getTickerCode(), e);
            }
        }
        marketsRepository.saveAll(marketsList);

        log.info("株価変動 終了 件数={}", updateCount);

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

    // 騰落率&前日比計算
    private void calcPriceRate(Markets market, BigDecimal newPrice){
        BigDecimal currentPrice = newPrice;
        BigDecimal openPrice = market.getOpenPrice();
        BigDecimal priceChange = currentPrice.subtract(openPrice);
        BigDecimal changeRate = priceChange.divide(openPrice, 4, RoundingMode.HALF_UP)
                                           .multiply(BigDecimal.valueOf(100))
                                           .setScale(2, RoundingMode.HALF_UP);
        market.setPriceChange(priceChange);
        market.setChangeRate(changeRate);
    }

}
