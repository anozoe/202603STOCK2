package com.example.stock.batch.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.stock.batch.dto.BatchResultDto;
import com.example.stock.batch.dto.ExecutionInfoResponse;
import com.example.stock.constants.ORDER_SIDE;
import com.example.stock.constants.ORDER_STATUS;
import com.example.stock.entity.AssetsStock;
import com.example.stock.repository.AssetsStockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecutionCheckService {
    private final ExecutionInfoService executionInfoService;
    private final AssetsStockRepository assetsStockRepository;

    public BatchResultDto executionCheck() {
        List<ExecutionInfoResponse> targetList = executionInfoService.getExecutionInfo();
        int updateCount = 0;
        int failCount = 0;
        for (ExecutionInfoResponse target : targetList ) {
            if (target.getOrderSide() == ORDER_SIDE.BUY.getCode()) {
                buyExecutionCheck(target);  
            } else if (target.getOrderSide() == ORDER_SIDE.SELL.getCode()) {
                sellExecutionCheck(target);
            }
            }
        return new BatchResultDto(updateCount, failCount, "SUCCESS");
    }
    /**
     * 買い処理
     * @param target
     */
    void buyExecutionCheck(ExecutionInfoResponse target) {
        BigDecimal currentPrice = target.getCurrentPrice();
        BigDecimal limitPrice = target.getLimitPrice();
        if (currentPrice.compareTo(limitPrice) <= 0) {
            // assets_stock
            int orderQuantity = target.getOrderQuantity();
            BigDecimal executedPrice = currentPrice;
            BigDecimal averagePrice = executedPrice;
            BigDecimal profitLoss = BigDecimal.ZERO; // 約定したとき損益は0になる
            BigDecimal profitLossRatio = BigDecimal.ZERO; // 約定したとき損益は0になる
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
        }
    }

    private void sellExecutionCheck(ExecutionInfoResponse target) {
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
            for (AssetsStock holding : sellList) {
                if (remainingToSell >= holding.getHoldingAmount()) {    // 完全売却ケース
                    profitLossChange = profitLossChange.add(holding.getProfitLoss());
                    marketValueChange = marketValueChange.add(holding.getMarketValue());
                    remainingToSell -= holding.getHoldingAmount(); 
                    holding.setHoldingAmount(0);
                    holding.setAveragePrice(BigDecimal.ZERO);
                    holding.setProfitLoss(BigDecimal.ZERO);
                    holding.setProfitLossRatio(BigDecimal.ZERO);
                    holding.setMarketValue(BigDecimal.ZERO);
                } else {                                                // 部分売却ケース
                    int newHoldingAmount = holding.getHoldingAmount() - remainingToSell;
                    BigDecimal averagePrice = holding.getAveragePrice();
                    BigDecimal differencePrice = currentPrice.subtract(averagePrice);
                    BigDecimal newProfitLoss = (differencePrice.multiply(BigDecimal.valueOf(newHoldingAmount)));
                    BigDecimal newProfitLossRatio = calcRatio(newProfitLoss, averagePrice);
                    BigDecimal newMarketValue = currentPrice.multiply(BigDecimal.valueOf(newHoldingAmount));
                    holding.setHoldingAmount(newHoldingAmount);
                    holding.setProfitLoss(newProfitLoss);
                    holding.setProfitLossRatio(newProfitLossRatio);
                    holding.setMarketValue(newMarketValue);
                    // 1株あたりの単価は変わらないので、平均取得価格はそのまま
                    profitLossChange = profitLossChange.add(holding.getProfitLoss().subtract(newProfitLoss));
                    marketValueChange = marketValueChange.add(holding.getMarketValue().subtract(newMarketValue));
                    break;
                } 
            }
            BigDecimal executedPrice = currentPrice;
            BigDecimal newBuyingPower = target.getBuyingPower().add(executedPrice.multiply(BigDecimal.valueOf(target.getOrderQuantity())));
            
            target.setExecutedPrice(executedPrice);
            target.setOrderExecutionStatus(ORDER_STATUS.EXECUTED.getCode());
            target.setBuyingPower(newBuyingPower);
        }
    }

    /**
     * 割合計算
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
