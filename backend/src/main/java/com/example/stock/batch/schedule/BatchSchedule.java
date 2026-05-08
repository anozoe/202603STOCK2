package com.example.stock.batch.schedule;

import com.example.stock.batch.service.MarketsSetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stock.batch.service.PriceUpdateService;

@Component
public class BatchSchedule {
    @Autowired
    MarketsSetupService marketsSetupService;
    PriceUpdateService priceUpdateService;


    BatchSchedule(MarketsSetupService marketsSetupService, PriceUpdateService priceUpdateService) {
        this.marketsSetupService = marketsSetupService;
        this.priceUpdateService = priceUpdateService;
    }


    // @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Tokyo")
    public void marketsSetup() {
        marketsSetupService.setOpenPrice();
    }
    
    // @Scheduled(cron = "0 10/10 9 * * MON-FRI", zone = "Asia/Tokyo")
    // @Scheduled(cron = "0 0/10 10-16 * * MON-FRI", zone = "Asia/Tokyo")
    // @Scheduled(cron = "0 0 17 * * MON-FRI", zone = "Asia/Tokyo")
    @Scheduled(cron = "10 * * * * MON-FRI", zone = "Asia/Tokyo")
    public void priceUpdate() {
        priceUpdateService.updateCurrentPrice();
    }
}
