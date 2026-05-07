package com.example.stock.service;

import com.example.stock.constants.MarketCode;
import com.example.stock.dto.*;
import com.example.stock.entity.Stock;
import com.example.stock.entity.StockPriceHistory;
import com.example.stock.exception.BusinessException;
import com.example.stock.repository.StockPriceHistoryRepository;
import com.example.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMarketSyncService {

    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final TwelveDataClient twelveDataClient;
    private final FmpClient fmpClient;
    private final StockMetricCalculator stockMetricCalculator;

    @Transactional
    public Stock syncByTicker(String tickerCode, Integer displayOrder) {
        String normalizedTicker = tickerCode == null ? "" : tickerCode.trim().toUpperCase();

        TwelveDataQuoteResponse quote;
        try {
            quote = twelveDataClient.fetchQuote(normalizedTicker);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("E002", "銘柄コード");
        }

        if (quote == null || quote.getSymbol() == null || quote.getSymbol().isBlank()) {
            throw new BusinessException("E002", "銘柄コード");
        }

        Stock stock = stockRepository.findByTickerCode(normalizedTicker)
                .orElseGet(Stock::new);

        FmpMarketCapResponse marketCap = safeFetchMarketCap(normalizedTicker);
        FmpIncomeStatementTtmResponse incomeTtm = safeFetchIncomeStatementTtm(normalizedTicker);
        FmpBalanceSheetTtmResponse balanceTtm = safeFetchBalanceSheetTtm(normalizedTicker);
        FmpKeyMetricsTtmResponse keyMetricsTtm = safeFetchKeyMetricsTtm(normalizedTicker);
        FmpRatiosTtmResponse ratiosTtm = safeFetchRatiosTtm(normalizedTicker);

        stock.setTickerCode(normalizedTicker);
        stock.setStockName(
                quote.getName() != null && !quote.getName().isBlank()
                        ? quote.getName()
                        : normalizedTicker
        );
        stock.setMarket(mapMarketCode(quote.getExchange()));
        stock.setMarketStatus(Boolean.TRUE.equals(quote.getIsMarketOpen()) ? "OPEN" : "CLOSE");
        stock.setCurrentPrice(nonNullBigDecimal(toBigDecimal(quote.getClose())));
        stock.setOpenPrice(toBigDecimal(quote.getOpen()));
        stock.setHighPrice(toBigDecimal(quote.getHigh()));
        stock.setLowPrice(toBigDecimal(quote.getLow()));
        stock.setClosePrice(toBigDecimal(quote.getClose()));
        stock.setPriceChange(toBigDecimal(quote.getChange()));
        stock.setChangeRate(toBigDecimal(quote.getPercentChange()));
        stock.setVolume(toLong(quote.getVolume()));
        stock.setFetchedAt(LocalDateTime.now());

        if (marketCap != null && marketCap.getMarketCap() != null) {
            stock.setMarketCap(marketCap.getMarketCap().longValue());
        }

        BigDecimal pbr = null;
        if (ratiosTtm != null) {
            pbr = ratiosTtm.getPriceToBookRatioTTM() != null
                    ? ratiosTtm.getPriceToBookRatioTTM()
                    : ratiosTtm.getPriceToBookRatio();
        }
        stock.setPbr(pbr);

        BigDecimal per = stockMetricCalculator.calculatePer(
                marketCap == null ? null : marketCap.getMarketCap(),
                incomeTtm == null ? null : incomeTtm.getNetIncome()
        );
        stock.setPer(per);

        BigDecimal roe = stockMetricCalculator.calculateRoe(
                incomeTtm == null ? null : incomeTtm.getNetIncome(),
                balanceTtm == null ? null : balanceTtm.getTotalStockholdersEquity()
        );
        stock.setRoe(roe);

        BigDecimal dividendYield = stockMetricCalculator.calculateDividendYield(
                keyMetricsTtm == null ? null : keyMetricsTtm.getDividendPerShareTTM(),
                stock.getCurrentPrice()
        );
        stock.setDividendYield(dividendYield);

        if (displayOrder != null && displayOrder > 0) {
            stock.setDisplayOrder(displayOrder);
        } else if (stock.getDisplayOrder() == null || stock.getDisplayOrder() <= 0) {
            int nextOrder = stockRepository.findTopByOrderByDisplayOrderDesc()
                    .map(s -> (s.getDisplayOrder() == null ? 0 : s.getDisplayOrder()) + 1)
                    .orElse(1);
            stock.setDisplayOrder(nextOrder);
        }

        Stock saved = stockRepository.save(stock);

        try {
            TwelveDataTimeSeriesResponse timeSeries = twelveDataClient.fetchDailyTimeSeries(normalizedTicker, 30);
            replacePriceHistories(saved, timeSeries);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return saved;
    }

    private FmpMarketCapResponse safeFetchMarketCap(String ticker) {
        try {
            return fmpClient.fetchMarketCap(ticker);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private FmpIncomeStatementTtmResponse safeFetchIncomeStatementTtm(String ticker) {
        try {
            return fmpClient.fetchIncomeStatementTtm(ticker);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private FmpBalanceSheetTtmResponse safeFetchBalanceSheetTtm(String ticker) {
        try {
            return fmpClient.fetchBalanceSheetTtm(ticker);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private FmpKeyMetricsTtmResponse safeFetchKeyMetricsTtm(String ticker) {
        try {
            return fmpClient.fetchKeyMetricsTtm(ticker);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private FmpRatiosTtmResponse safeFetchRatiosTtm(String ticker) {
        try {
            return fmpClient.fetchRatiosTtm(ticker);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void replacePriceHistories(Stock stock, TwelveDataTimeSeriesResponse response) {
        if (response == null || response.getValues() == null || response.getValues().isEmpty()) {
            return;
        }

        stockPriceHistoryRepository.deleteByStockId(stock.getId());

        List<StockPriceHistory> histories = response.getValues().stream()
                .map(value -> {
                    StockPriceHistory history = new StockPriceHistory();
                    history.setStock(stock);
                    history.setPriceDate(parseDate(value.getDatetime()));
                    history.setOpenPrice(toBigDecimal(value.getOpen()));
                    history.setHighPrice(toBigDecimal(value.getHigh()));
                    history.setLowPrice(toBigDecimal(value.getLow()));
                    history.setClosePrice(toBigDecimal(value.getClose()));
                    history.setVolume(toLong(value.getVolume()));
                    history.setCreatedAt(LocalDateTime.now());
                    history.setCreatedBy("system");
                    history.setUpdatedAt(LocalDateTime.now());
                    history.setUpdatedBy("system");
                    return history;
                })
                .filter(h -> h.getPriceDate() != null)
                .sorted(Comparator.comparing(StockPriceHistory::getPriceDate))
                .toList();

        stockPriceHistoryRepository.saveAll(histories);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.substring(0, 10));
    }

    private BigDecimal toBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value);
    }

    private Long toLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value).longValue();
    }

    private BigDecimal nonNullBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Integer mapMarketCode(String exchange) {
        if (exchange == null) {
            return MarketCode.NASDAQ;
        }

        return switch (exchange.toUpperCase()) {
            case "NASDAQ" -> MarketCode.NASDAQ;
            case "NYSE" -> MarketCode.NYSE;
            case "AMEX", "NYSE ARCA" -> MarketCode.AMEX;
            default -> MarketCode.NASDAQ;
        };
    }
}