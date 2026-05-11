package com.example.stock.batch.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stock.batch.dto.ExecutionInfoResponse;
import com.example.stock.repository.OrdersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecutionInfoService {
    private final OrdersRepository ordersRepository;

    public List<ExecutionInfoResponse> getExecutionInfo() {
        return ordersRepository.findExecutionInfo().stream()
            .map(p -> new ExecutionInfoResponse(
                p.getOrderId(),
                p.getUserId(),
                p.getStockId(),
                p.getOrderQuantity(),
                p.getOrderSide(),
                p.getOrderMethod(),
                p.getLimitPrice(),
                p.getOrderAt(),
                p.getExecutedPrice(),
                p.getExecutedAt(),
                p.getOrderExecutionStatus(),
                p.getAveragePrice(),
                p.getProfitLoss(),
                p.getProfitLossRatio(),
                p.getMarketValue(),
                p.getHoldingAmount(),
                p.getBuyingPower(),
                p.getHoldingsValue(),
                p.getTotalAssets(),
                p.getUnrealizedPnl(),
                p.getUnrealizedPnlRatio(),
                p.getCurrentPrice()
            ))
            .toList();
    }
}
