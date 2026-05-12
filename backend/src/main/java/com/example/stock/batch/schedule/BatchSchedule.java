package com.example.stock.batch.schedule;

import com.example.stock.batch.service.ExecutionCheckService;
import com.example.stock.batch.service.MarketsSetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stock.batch.service.PriceUpdateService;

@Component
public class BatchSchedule {
    
    private final MarketsSetupService marketsSetupService;
    private final PriceUpdateService priceUpdateService;
    private final ExecutionCheckService executionCheckService;


    BatchSchedule(MarketsSetupService marketsSetupService, PriceUpdateService priceUpdateService, ExecutionCheckService executionCheckService) {
        this.marketsSetupService = marketsSetupService;
        this.priceUpdateService = priceUpdateService;
        this.executionCheckService = executionCheckService;
    }


    // @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Tokyo")
    public void marketsSetup() {
        marketsSetupService.setOpenPrice();
        executionCheckService.executionCheck();
    }
    
    // @Scheduled(cron = "0 10/10 9 * * MON-FRI", zone = "Asia/Tokyo")
    // @Scheduled(cron = "0 0/10 10-16 * * MON-FRI", zone = "Asia/Tokyo")
    // @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "Asia/Tokyo")
    @Scheduled(cron = "0/25 * * * * MON-FRI", zone = "Asia/Tokyo")
    public void priceUpdate() {
        priceUpdateService.updateCurrentPrice();
        executionCheckService.executionCheck();
    }
}
