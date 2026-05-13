package com.example.stock.batch.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.batch.dto.BatchResultDto;
import com.example.stock.entity.AssetsStock;
import com.example.stock.entity.AssetsTotal;
import com.example.stock.repository.AssetsStockRepository;
import com.example.stock.repository.AssetsTotalRepository;
import com.example.stock.repository.MarketsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AssetsUpdateService {
    private final MarketsRepository marketsRepository;
    private final AssetsStockRepository assetsStockRepository;
    private final AssetsTotalRepository assetsTotalRepository;


    public BatchResultDto updateAssets(){
        log.info("資産更新バッチ 開始");

        int updateCount = 0;
        int failCount = 0;
        try {
            Map<Integer, BigDecimal> marketMap = marketsRepository.findAll().stream().collect(Collectors.toMap(s->s.getId(), s->s.getCurrentPrice()));
            List<AssetsStock> stockList = assetsStockRepository.findAll();
            List<AssetsTotal> totalList = assetsTotalRepository.findAll();
            LocalDateTime batchTime = LocalDateTime.now();
            // assets_stock更新
            for (AssetsStock stock : stockList){
                BigDecimal currentPrice = marketMap.get(stock.getStockId());
                BigDecimal averagePrice = stock.getAveragePrice();
                int holdingAmount = stock.getHoldingAmount();
    
                BigDecimal differencePrice = currentPrice.subtract(averagePrice);
                BigDecimal newProfitLoss = differencePrice.multiply(BigDecimal.valueOf(holdingAmount));
                BigDecimal newProfitLossRatio;
                if (averagePrice.compareTo(BigDecimal.ZERO) == 0) {
                    newProfitLossRatio = BigDecimal.ZERO;
                } else {
                    newProfitLossRatio = differencePrice    // ← 前回のレビューで指摘した修正もここで
                        .divide(averagePrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                }
                BigDecimal newMarketValue = currentPrice.multiply(BigDecimal.valueOf(holdingAmount));
    
                stock.setProfitLoss(newProfitLoss);
                stock.setProfitLossRatio(newProfitLossRatio);
                stock.setMarketValue(newMarketValue);
                stock.setUpdatedAt(batchTime);
            }
            assetsStockRepository.saveAll(stockList);
    
            // assets_total更新
            Map<Integer, List<AssetsStock>> stockByUser = stockList.stream().collect(Collectors.groupingBy(AssetsStock::getUserId));
            for (AssetsTotal total : totalList) {
                int userId = total.getUserId();
                BigDecimal totalProfitLoss = BigDecimal.ZERO;
                BigDecimal totalMarketValue = BigDecimal.ZERO;
                List<AssetsStock> userStocks = stockByUser.getOrDefault(userId, List.of());

                for (AssetsStock userStock : userStocks){
                    totalProfitLoss = totalProfitLoss.add(userStock.getProfitLoss());
                    totalMarketValue = totalMarketValue.add(userStock.getMarketValue());
                }

                BigDecimal newBuyingPower = total.getBuyingPower();
                BigDecimal newHoldingsValue = totalMarketValue;
                BigDecimal newTotalAssets = newBuyingPower.add(newHoldingsValue);
                BigDecimal newUnrealizedPnl = totalProfitLoss;
                BigDecimal acquisitionCost = newHoldingsValue.subtract(newUnrealizedPnl);
                BigDecimal newUnrealizedPnlRatio;
                if (acquisitionCost.compareTo(BigDecimal.ZERO) == 0) {
                    newUnrealizedPnlRatio = BigDecimal.ZERO;
                } else {
                    newUnrealizedPnlRatio = newUnrealizedPnl
                        .divide(acquisitionCost, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                }

                total.setBuyingPower(newBuyingPower);
                total.setHoldingsValue(newHoldingsValue);
                total.setTotalAssets(newTotalAssets);
                total.setUnrealizedPnl(newUnrealizedPnl);
                total.setUnrealizedPnlRatio(newUnrealizedPnlRatio);
                total.setUpdatedAt(batchTime);
            }
            assetsTotalRepository.saveAll(totalList);
            updateCount++;
            log.info("資産更新に成功しました。 件数={}", updateCount);
            
        } catch (Exception e) {
            failCount++;
            log.error("資産更新に失敗しました", e);
        }
        return new BatchResultDto(updateCount, failCount, "SUCCESS");

    }
}
