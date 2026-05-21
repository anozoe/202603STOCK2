package com.example.stock.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.dto.AssetsStockResponse;
import com.example.stock.dto.HoldingStockProjection;
import com.example.stock.dto.HoldingStockResponse;
import com.example.stock.dto.SumHoldingAmountProjection;
import com.example.stock.dto.SumHoldingAmountResponse;
import com.example.stock.repository.AssetsStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetsStockService {
    private final AssetsStockRepository assetsStockRepository;

    @Transactional
    public List<AssetsStockResponse> getAssetsStock(Integer userId){
        List<AssetsStockResponse> stockList = assetsStockRepository.findAssetsStockInfo(userId)
                                                                   .stream()
                                                                   .map(p -> new AssetsStockResponse(p.getId(), 
                                                                            p.getUserId(), 
                                                                            p.gettickerCode(), 
                                                                            p.getAveragePrice(), 
                                                                            p.getProfitLoss(), 
                                                                            p.getProfitLossRatio(), 
                                                                            p.getMarketValue(), 
                                                                            p.getHoldingAmount()))
                                                                   .toList();
        return stockList;
    }

    @Transactional
    public SumHoldingAmountResponse getSumHoldingAmount(Integer userId, String tickerCode){
        SumHoldingAmountProjection projection = assetsStockRepository.findSumHoldingAmount(userId, tickerCode).orElseThrow();
        SumHoldingAmountResponse response = new SumHoldingAmountResponse();
        response.setUserId(projection.getUserId());
        response.setTickerCode(projection.getTickerCode());
        response.setSumHoldingAmount(projection.getSumHoldingAmount());
        return response; 
    }

    @Transactional(readOnly = true)
    public List<HoldingStockResponse> getHoldingStock(Integer userId) {
        List<HoldingStockProjection> holdingList = assetsStockRepository.findHoldingStock(userId);
        
        return holdingList.stream()    
                .map(p -> new HoldingStockResponse(   
                    p.getTickerCode(),
                    p.getStockName(),
                    p.getMarket(),
                    p.getCurrentPrice(),
                    p.getPriceChange(),
                    p.getChangeRate(),
                    p.getTotalCost(),
                    p.getTotalMarketValue(),
                    p.getTotalHoldingAmount()
                ))
                .toList();
    }                                                     
}
