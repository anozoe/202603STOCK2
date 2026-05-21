package com.example.stock.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.constants.ORDER_METHOD;
import com.example.stock.constants.ORDER_SIDE;
import com.example.stock.constants.ORDER_STATUS;
import com.example.stock.dto.OrderRequest;
import com.example.stock.dto.OrderResponse;
import com.example.stock.entity.AssetsStock;
import com.example.stock.entity.AssetsTotal;
import com.example.stock.entity.Orders;
import com.example.stock.repository.AssetsStockRepository;
import com.example.stock.repository.AssetsTotalRepository;
import com.example.stock.repository.OrdersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrdersRepository ordersRepository;
    private final AssetsStockRepository assetsStockRepository;
    private final AssetsTotalRepository assetsTotalRepository;

    @Transactional
    public OrderResponse orderRegister(OrderRequest request) {
        LocalDateTime executedTime = LocalDateTime.now();
        boolean isMarketOrder = ORDER_METHOD.MarketOrder.getCode() == request.getOrderMethod();

        Orders newOrder = addOrderRecord(request, isMarketOrder, executedTime);
        
        boolean isBuy = newOrder.getOrderSide().equals(ORDER_SIDE.BUY.getCode());
        boolean isSell = newOrder.getOrderSide().equals(ORDER_SIDE.SELL.getCode());

        // 成行・買い
        if (isMarketOrder && isBuy) {
            addStockRecord(request, newOrder, executedTime);  
            updateTotalRecord(request, newOrder, executedTime, null, null);      
        } else if (isMarketOrder && isSell) {
            List<AssetsStock> sellList =  assetsStockRepository.findHoldingsForSell(
                request.getUserId(),
                request.getStockId()
            );
            int remainingToSell = request.getOrderQuantity();      // 残り売却数量
            BigDecimal profitLossChange = BigDecimal.ZERO;          // 売却したことによる損益減少額（評価損益計算に使用）
            BigDecimal marketValueChange = BigDecimal.ZERO;         // 売却したことによる評価額減少額（保有資産評価額計算に使用）
            BigDecimal currentPrice = request.getCurrentPrice();
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
                    remainingToSell = 0;
                    break;
                } 
            }
            assetsStockRepository.saveAll(sellList);
            
            updateTotalRecord(request, newOrder, executedTime, profitLossChange, marketValueChange);
        }
        
        return new OrderResponse(
            newOrder.getId(),
            newOrder.getOrderExecutionStatus(),
            newOrder.getExecutedPrice(),
            newOrder.getExecutedAt()
        );
    }
    
    /**
     * frontendから受け取ったリクエストをもとに、orderレコードを新しく作る。
     * @param request           frontendから受け取ったリクエスト
     * @param isMarketOrder     成行注文判定
     * @param executedTime      約定時間
     * @return                  新しく作ったorderレコード
     */
    private Orders addOrderRecord(OrderRequest request, boolean isMarketOrder, LocalDateTime executedTime){
        Orders order = new Orders();
        order.setUserId(request.getUserId());
        order.setStockId(request.getStockId());
        order.setOrderQuantity(request.getOrderQuantity());
        order.setOrderSide(request.getOrderSide());
        order.setOrderMethod(request.getOrderMethod());
        order.setLimitPrice(request.getLimitPrice());
        order.setOrderAt(executedTime);
        order.setExecutedPrice(isMarketOrder ? request.getLimitPrice() : null);
        order.setExecutedAt(isMarketOrder ? executedTime : null);
        order.setOrderExecutionStatus(isMarketOrder ? ORDER_STATUS.EXECUTED.getCode() : ORDER_STATUS.UNEXECUTED.getCode());
        order.setCreatedAt(executedTime);
        order.setUpdatedAt(null);
        Orders newOrder = ordersRepository.save(order);
        return newOrder;        
    }

    /**
     * 成行・買い注文処理<br>
     * 注文内容と約定時間を渡して、その情報をもとにassets_stockのレコードを新たに作成し保存する。
     * @param newOrder      注文内容（orders）
     * @param executedTime  約定時間
     */
    private void addStockRecord(OrderRequest request, Orders newOrder, LocalDateTime executedTime){
        // assets_stock更新
        BigDecimal currentPrice = newOrder.getExecutedPrice();
        Integer holdingAmount = newOrder.getOrderQuantity();
        BigDecimal marketValue = currentPrice.multiply(BigDecimal.valueOf(holdingAmount));
        AssetsStock newStock = new AssetsStock();
        newStock.setUserId(newOrder.getUserId());
        newStock.setStockId(newOrder.getStockId());
        newStock.setOrderId(newOrder.getId());
        newStock.setAveragePrice(newOrder.getExecutedPrice());
        newStock.setProfitLoss(BigDecimal.ZERO);
        newStock.setProfitLossRatio(BigDecimal.ZERO);
        newStock.setMarketValue(marketValue);
        newStock.setHoldingAmount(holdingAmount);
        newStock.setCreatedAt(executedTime);
        assetsStockRepository.save(newStock);
    }

    /**
     * 受け取ったパラメータをもとに、assets_totalの内容を計算し、更新する。
     * @param request               frontendからの注文リクエスト
     * @param newOrder              新しく追加買い注文データ
     * @param executedTime          約定時間
     * @param profitLossChange      売った銘柄の損益変化
     * @param marketValueChange     売った銘柄の評価額変化
     */
    private void updateTotalRecord(OrderRequest request, Orders newOrder, LocalDateTime executedTime,  BigDecimal profitLossChange, BigDecimal marketValueChange){
        BigDecimal currentPrice = newOrder.getExecutedPrice();
        BigDecimal quantity = BigDecimal.valueOf(newOrder.getOrderQuantity());
        BigDecimal tradeAmount = currentPrice.multiply(quantity);

        BigDecimal buyingPowerDelta;
        BigDecimal holdingValueDelta;
        BigDecimal pnlDelta;

        ORDER_SIDE side = ORDER_SIDE.getByCode(newOrder.getOrderSide());
        
        switch (side) {
            case BUY -> {
                buyingPowerDelta = tradeAmount.negate();
                holdingValueDelta = tradeAmount;
                pnlDelta = BigDecimal.ZERO;
            }
            case SELL -> {
                buyingPowerDelta = tradeAmount;
                holdingValueDelta = marketValueChange.negate();
                pnlDelta = profitLossChange.negate();
            }
            default -> throw new IllegalStateException("Unexpected order side: " + side);
        }
        
        AssetsTotal assets = assetsTotalRepository.findByUserId(request.getUserId()).orElseThrow(() -> new IllegalStateException("AssetsTotal not found: userId=" + request.getUserId()));

        BigDecimal currentBuyingPower = assets.getBuyingPower();
        BigDecimal currentHoldingsValue = assets.getHoldingsValue();
        BigDecimal currentUnrealizedPnl = assets.getUnrealizedPnl();

        BigDecimal newBuyingPower = currentBuyingPower.add(buyingPowerDelta);
        BigDecimal newHoldingsValue =  currentHoldingsValue.add(holdingValueDelta);
        BigDecimal newTotalAssets = newBuyingPower.add(newHoldingsValue);
        BigDecimal newUnrealizedPnl = currentUnrealizedPnl.add(pnlDelta);
        BigDecimal principal = newHoldingsValue.subtract(newUnrealizedPnl);
        BigDecimal newUnrealizedPnlRatio = calcRatio(newUnrealizedPnl, principal);

        assets.setBuyingPower(newBuyingPower);
        assets.setHoldingsValue(newHoldingsValue);
        assets.setTotalAssets(newTotalAssets);
        assets.setUnrealizedPnl(newUnrealizedPnl);
        assets.setUnrealizedPnlRatio(newUnrealizedPnlRatio);
        assets.setUpdatedAt(executedTime);
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
