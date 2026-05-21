package com.example.stock.batch.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.batch.dto.BatchResultDto;
import com.example.stock.batch.dto.ExecutionInfoResponse;
import com.example.stock.constants.ORDER_SIDE;
import com.example.stock.constants.ORDER_STATUS;
import com.example.stock.entity.AssetsStock;
import com.example.stock.entity.AssetsTotal;
import com.example.stock.entity.Orders;
import com.example.stock.repository.AssetsStockRepository;
import com.example.stock.repository.AssetsTotalRepository;
import com.example.stock.repository.OrdersRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ExecutionCheckService {
    private final ExecutionInfoService executionInfoService;
    private final AssetsStockRepository assetsStockRepository;
    private final OrdersRepository ordersRepository;
    private final AssetsTotalRepository assetsTotalRepository;
    private static final Logger log = LoggerFactory.getLogger(ExecutionCheckService.class);
    public BatchResultDto executionCheck() {
        List<ExecutionInfoResponse> targetList = executionInfoService.getExecutionInfo();
        int updateCount = 0;
        int failCount = 0;
        for (ExecutionInfoResponse target : targetList ) {
            try {
                if (target.getOrderSide() == ORDER_SIDE.BUY.getCode()) {
                    updateCount += buyExecutionCheck(target)  ? 1 : 0;  
                } else if (target.getOrderSide() == ORDER_SIDE.SELL.getCode()) {
                    updateCount += sellExecutionCheck(target) ? 1 : 0;
                }
            } catch (Exception e) {
                failCount++;
                log.error("注文ID={} の処理で失敗", target.getOrderId(), e);
            }
        }
        log.info("{}件の注文が成立しました。", updateCount);
        
        return new BatchResultDto(updateCount, failCount, "SUCCESS");
    }

    /**
     * 注文情報をもとに買い判定を行い、約定結果を返す。
     * @param ExecutionInfoResponse　注文情報
     * @return 約定結果
     */
    private boolean buyExecutionCheck(ExecutionInfoResponse target) {
        BigDecimal currentPrice = target.getCurrentPrice();
        BigDecimal limitPrice = target.getLimitPrice();
        if (currentPrice.compareTo(limitPrice) <= 0) {
            // assets_stock
            int orderQuantity = target.getOrderQuantity();
            BigDecimal executedPrice = currentPrice;
            BigDecimal averagePrice = executedPrice;
            BigDecimal profitLoss = BigDecimal.ZERO;        // 約定したとき損益は0になる
            BigDecimal profitLossRatio = BigDecimal.ZERO;   // 約定したとき損益は0になる
            BigDecimal marketValue = executedPrice.multiply(BigDecimal.valueOf(orderQuantity));
            BigDecimal limitValue = limitPrice.multiply(BigDecimal.valueOf(orderQuantity));
            BigDecimal differenceValue = limitValue.subtract(marketValue);

            // assets_total
            BigDecimal newBuyingPower = target.getBuyingPower().add(differenceValue);
            BigDecimal newHoldingValue = target.getHoldingsValue().add(marketValue);
            BigDecimal newTotalAssets = newBuyingPower.add(newHoldingValue);
            BigDecimal newUnrealizedPnl = target.getUnrealizedPnl().add(profitLoss);
            BigDecimal newUnrealizedPnlRatio = calcRatio(newUnrealizedPnl, newHoldingValue);

            
            target.setExecutedPrice(executedPrice);
            target.setOrderExecutionStatus(ORDER_STATUS.EXECUTED.getCode());
            target.setAveragePrice(averagePrice);
            target.setProfitLoss(profitLoss);
            target.setProfitLossRatio(profitLossRatio);
            target.setMarketValue(marketValue);
            target.setHoldingAmount(orderQuantity);
            target.setBuyingPower(newBuyingPower);
            target.setHoldingsValue(newHoldingValue);
            target.setTotalAssets(newTotalAssets);
            target.setUnrealizedPnl(newUnrealizedPnl);
            target.setUnrealizedPnlRatio(newUnrealizedPnlRatio);
            target.setExecutedAt(LocalDateTime.now());

            saveOrders(target);
            addAssetsStock(target);
            saveAssetsTotal(target);

            return true;
        } 
        return false;
    }

    /**
     * 注文情報をもとに売り判定を行い、約定結果を返す。
     * @param target　注文情報
     * @return 約定結果
     */
    private boolean sellExecutionCheck(ExecutionInfoResponse target) {
        BigDecimal currentPrice = target.getCurrentPrice();
        BigDecimal limitPrice = target.getLimitPrice();
        if (currentPrice.compareTo(limitPrice) >= 0) {
            
            List<AssetsStock> sellList =  assetsStockRepository.findHoldingsForSell(
                target.getUserId(),
                target.getStockId()
            );
            int remainingToSell = target.getOrderQuantity();    // 残り売却数量
            BigDecimal profitLossChange = BigDecimal.ZERO;         // 売却したことによる損益減少額（評価損益計算に使用）
            BigDecimal marketValueChange = BigDecimal.ZERO;        // 売却したことによる評価額減少額（保有資産評価額計算に使用）
            int totalSoldAmount = 0;
            for (AssetsStock holding : sellList) {
                if (remainingToSell >= holding.getHoldingAmount()) {    // 完全売却ケース
                    profitLossChange = profitLossChange.add(holding.getProfitLoss());
                    marketValueChange = marketValueChange.add(holding.getMarketValue());
                    remainingToSell -= holding.getHoldingAmount();
                    totalSoldAmount += holding.getHoldingAmount(); 
                    holding.setHoldingAmount(0);
                    holding.setAveragePrice(BigDecimal.ZERO);
                    holding.setProfitLoss(BigDecimal.ZERO);
                    holding.setProfitLossRatio(BigDecimal.ZERO);
                    holding.setMarketValue(BigDecimal.ZERO);
                } else {                                                // 部分売却ケース
                    BigDecimal oldProfitLoss = holding.getProfitLoss();
                    BigDecimal oldMarketValue = holding.getMarketValue();
                    int newHoldingAmount = holding.getHoldingAmount() - remainingToSell;
                    BigDecimal averagePrice = holding.getAveragePrice();
                    BigDecimal differencePrice = currentPrice.subtract(averagePrice);
                    BigDecimal newProfitLoss = (differencePrice.multiply(BigDecimal.valueOf(newHoldingAmount)));
                    BigDecimal newProfitLossRatio = calcRatio(differencePrice, averagePrice);
                    BigDecimal newMarketValue = currentPrice.multiply(BigDecimal.valueOf(newHoldingAmount));
                    holding.setHoldingAmount(newHoldingAmount);
                    holding.setProfitLoss(newProfitLoss);
                    holding.setProfitLossRatio(newProfitLossRatio);
                    holding.setMarketValue(newMarketValue);
                    // 1株あたりの単価は変わらないので、平均取得価格はそのまま
                    profitLossChange = profitLossChange.add(oldProfitLoss.subtract(newProfitLoss));
                    marketValueChange = marketValueChange.add(oldMarketValue.subtract(newMarketValue));
                    totalSoldAmount += remainingToSell;
                    remainingToSell = 0;
                    break;
                } 
            }
            assetsStockRepository.saveAll(sellList);

            BigDecimal executedPrice = currentPrice;
            BigDecimal newBuyingPower = target.getBuyingPower().add(executedPrice.multiply(BigDecimal.valueOf(totalSoldAmount)));
            BigDecimal newHoldingsValue = target.getHoldingsValue().subtract(marketValueChange);
            BigDecimal newTotalAssets = newBuyingPower.add(newHoldingsValue);
            BigDecimal newUnrealizedPnl = target.getUnrealizedPnl().subtract(profitLossChange);
            BigDecimal newUnrealizedPnlRatio = calcRatio(newUnrealizedPnl, newHoldingsValue);
            
            target.setExecutedPrice(executedPrice);
            target.setOrderExecutionStatus(ORDER_STATUS.EXECUTED.getCode());
            target.setBuyingPower(newBuyingPower);
            target.setHoldingsValue(newHoldingsValue);
            target.setTotalAssets(newTotalAssets);
            target.setUnrealizedPnl(newUnrealizedPnl);
            target.setUnrealizedPnlRatio(newUnrealizedPnlRatio);
            target.setExecutedAt(LocalDateTime.now());

            saveOrders(target);
            saveAssetsTotal(target);

            return true;
        } 
        return false;
    }

    /**
     * 約定情報を注文に反映してDBに保存する。
     * @param ExecutionInfoResponse 約定情報レスポンス
     */
    // orderテーブル保存
    private void saveOrders(ExecutionInfoResponse target){
        Orders order = ordersRepository.findById(target.getOrderId()).orElseThrow();
        order.setExecutedPrice(target.getExecutedPrice());
        order.setOrderExecutionStatus(target.getOrderExecutionStatus());
        order.setExecutedAt(target.getExecutedAt());
        order.setUpdatedAt(LocalDateTime.now());
        ordersRepository.save(order);
    }

    // assets_stockテーブル保存
    private void addAssetsStock(ExecutionInfoResponse target){
        AssetsStock assetsStock = new AssetsStock();
        assetsStock.setUserId(target.getUserId());
        assetsStock.setStockId(target.getStockId());
        assetsStock.setOrderId(target.getOrderId());
        assetsStock.setAveragePrice(target.getExecutedPrice());
        assetsStock.setProfitLoss(target.getProfitLoss());
        assetsStock.setProfitLossRatio(target.getProfitLossRatio());
        assetsStock.setMarketValue(target.getMarketValue());
        assetsStock.setHoldingAmount(target.getHoldingAmount());
        assetsStock.setCreatedAt(LocalDateTime.now());
        assetsStockRepository.save(assetsStock);
    }

    // assets_totalテーブル保存
    private void saveAssetsTotal(ExecutionInfoResponse target){
        AssetsTotal assetsTotal = assetsTotalRepository.findByUserId(target.getUserId()).orElseThrow();
        assetsTotal.setBuyingPower(target.getBuyingPower());
        assetsTotal.setHoldingsValue(target.getHoldingsValue());
        assetsTotal.setTotalAssets(target.getTotalAssets());
        assetsTotal.setUnrealizedPnl(target.getUnrealizedPnl());
        assetsTotal.setUnrealizedPnlRatio(target.getUnrealizedPnlRatio());
        assetsTotal.setUpdatedAt(LocalDateTime.now());
        assetsTotalRepository.save(assetsTotal);
    }

    /**
     * ゼロ除算防止しつつ、小数第二位までの割合計算を行う。
     * @param numerator     分子
     * @param denominator   分母
     * @return  割合（%）、分母が0のときは0
     */
    private BigDecimal calcRatio(BigDecimal numerator, BigDecimal denominator) {
        // ゼロ除算防止
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP)   // 小数第4位まで、小数第5位以降は四捨五入
                        .multiply(BigDecimal.valueOf(100))              // 百分率
                        .setScale(2, RoundingMode.HALF_UP);             // 小数第二位まで表記
    }

}
