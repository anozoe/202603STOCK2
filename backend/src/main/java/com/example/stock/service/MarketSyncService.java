package com.example.stock.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.constants.MarketCode;
import com.example.stock.entity.Markets;
import com.example.stock.entity.Stock;
import com.example.stock.repository.MarketsRepository;
import com.example.stock.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MarketSyncService {

    private final StockRepository stockRepository;
    private final MarketsRepository marketsRepository;

    public int createMissingMarkets() {
        List<Stock> stocksWithoutMarket = stockRepository.findStocksWithoutMarket();

        List<Markets> newMarkets = stocksWithoutMarket.stream()
            .map(stock -> {
                Integer marketCode = stock.getMarket();
                String tradingMarket = toMarketName(marketCode);


                Markets market = new Markets();
                market.setStockId(stock.getId());
                market.setTickerCode(stock.getTickerCode());
                market.setStockName(stock.getStockName());
                market.setMarket(tradingMarket);
                market.setCurrentPrice(stock.getCurrentPrice());
                market.setPreviousClose(stock.getCurrentPrice());
                market.setOpenPrice(stock.getCurrentPrice());
                market.setHighPrice(stock.getCurrentPrice());
                market.setLowPrice(stock.getCurrentPrice());
                market.setFinishPrice(null);
                market.setPriceChange(null);
                market.setChangeRate(null);
                market.setUpdatedAt(null);
                market.setCreatedAt(LocalDateTime.now());

                return market;
            })
            .toList();

        marketsRepository.saveAll(newMarkets);
        return newMarkets.size();
    }

    private String toMarketName(Integer marketCode) {
        if (marketCode == null) {
            throw new IllegalStateException("market code is null");
        }
        return switch (marketCode) {
            case MarketCode.NASDAQ -> "NASDAQ";
            case MarketCode.NYSE   -> "NYSE";
            case MarketCode.AMEX   -> "AMEX";
            default -> throw new IllegalArgumentException("unknown market code: " + marketCode);
        };
    }
  
    @Transactional
    public void deleteByStockId(Long stockId) {
        marketsRepository.deleteByStockId(stockId);
    }
}