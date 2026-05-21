package com.example.stock.controller;

import com.example.stock.service.MarketSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/sync")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class MarketSyncController {

    private final MarketSyncService marketSyncService;

    @PostMapping("/markets")
    public ResponseEntity<Map<String, Integer>> syncMarkets() {
        int created = marketSyncService.createMissingMarkets();
        return ResponseEntity.ok(Map.of("created", created));
    }
}