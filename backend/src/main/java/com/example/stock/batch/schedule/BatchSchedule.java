package com.example.stock.batch.schedule;

import com.example.stock.batch.service.AssetsHistoryService;
import com.example.stock.batch.service.ExecutionCheckService;
import com.example.stock.batch.service.MarketCloseService;
import com.example.stock.batch.service.MarketsSetupService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stock.batch.service.PriceUpdateService;

@Component
public class BatchSchedule {
    
    private final MarketsSetupService marketsSetupService;
    private final PriceUpdateService priceUpdateService;
    private final ExecutionCheckService executionCheckService;
    private final MarketCloseService marketCloseService;
    private final AssetsHistoryService assetsHistoryService;


    BatchSchedule(
        MarketsSetupService marketsSetupService, 
        PriceUpdateService priceUpdateService, 
        ExecutionCheckService executionCheckService,
        MarketCloseService marketCloseService,
        AssetsHistoryService assetsHistoryService
    ){
            this.marketsSetupService = marketsSetupService;
            this.priceUpdateService = priceUpdateService;
            this.executionCheckService = executionCheckService;
            this.marketCloseService = marketCloseService;
            this.assetsHistoryService = assetsHistoryService;
    }


    // @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Tokyo")
    public void marketsSetup() {
        marketsSetupService.setOpenPrice();
        executionCheckService.executionCheck();
    }
    
    // @Scheduled(cron = "0 10/10 9 * * MON-FRI", zone = "Asia/Tokyo")
    // @Scheduled(cron = "0 0/10 10-16 * * MON-FRI", zone = "Asia/Tokyo")
    // @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "Asia/Tokyo")
    @Scheduled(cron = "0 * * * * MON-FRI", zone = "Asia/Tokyo")
    public void priceUpdate() {
        priceUpdateService.updateCurrentPrice();
        //executionCheckService.executionCheck();
    }
    
    public void marketClose(){
        marketCloseService.setmarketClose();
        assetsHistoryService.addAssetsTotal();
    }
}
