package com.example.stock.batch.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


import org.springframework.stereotype.Service;

import com.example.stock.batch.dto.BatchResultDto;
import com.example.stock.constants.ORDER_SIDE;
import com.example.stock.constants.ORDER_STATUS;
import com.example.stock.entity.AssetsTotal;
import com.example.stock.entity.Orders;
import com.example.stock.repository.AssetsTotalRepository;
import com.example.stock.repository.MarketsRepository;
import com.example.stock.repository.OrdersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MarketCloseService {
    private final OrdersRepository ordersRepository;
    private final AssetsTotalRepository assetsTotalRepository;
    private final MarketsRepository marketsRepository;
    
    
    /**
     * 未約定の銘柄リストを取得し、キャンセル処理を行う。<br>
     * その後、終値を更新する。
     * @return
     */
    public BatchResultDto setmarketClose(){
        log.info("買い注文キャンセル処理＆終値更新 開始");
        // 未約定の銘柄リストを取得
        refundProcessing();
        int updateCount = 0;
        int failCount = 0;
        
        try {
                updateCount = marketsRepository.updateFinishPriceToCurrentPrice();
                log.info("finish_price 更新成功");
            } catch (Exception e) {
                failCount = 1; // 失敗時は1（バッチ全体が失敗）
                log.warn("finish_price 更新失敗");
        }
        log.info("買い注文キャンセル処理＆終値更新 終了");
        return new BatchResultDto(updateCount, failCount, "SUCCESS");
    } 

    /**
     * キャンセル処理<br>
     * 買い注文キャンセルに伴う買付可能額を対象ユーザに返金し、買付可能額を増やす。
     */
    private void refundProcessing() {
        List<Orders> orderList = ordersRepository.findByOrderExecutionStatus(ORDER_STATUS.UNEXECUTED.getCode());
        for (Orders order : orderList) {
            order.setOrderExecutionStatus(ORDER_STATUS.CANCEL.getCode());
            if (order.getOrderSide() == ORDER_SIDE.BUY.getCode()) {         
                BigDecimal limitPrice = order.getLimitPrice();
                int orderQuantity = order.getOrderQuantity();
                BigDecimal cancelPrice = limitPrice.multiply(BigDecimal.valueOf(orderQuantity)); 
                int cancelUserId = order.getUserId();               
                Optional<AssetsTotal> assetOpt = assetsTotalRepository.findByUserId(cancelUserId);
                assetOpt.ifPresent(asset -> {
                BigDecimal newBuyingPower = asset.getBuyingPower().add(cancelPrice);
                asset.setBuyingPower(newBuyingPower);
                assetsTotalRepository.save(asset);
                });
            } 
        }
        ordersRepository.saveAll(orderList);
    }
}
