package com.example.stock.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock.entity.AssetsTotalHistory;
import com.example.stock.service.AssetsTotalHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/total/history")
@CrossOrigin(origins = "http://localhost:3000")
public class AssetsTotalHistoryController {
    private final AssetsTotalHistoryService assetsTotalHistoryService;

    public AssetsTotalHistoryController (AssetsTotalHistoryService assetsTotalHistoryService){
        this.assetsTotalHistoryService = assetsTotalHistoryService;
    }

    @GetMapping
    public List<AssetsTotalHistory> getHistory (@RequestParam Integer userId) {
        return assetsTotalHistoryService.getHistory(userId);
    }
    
}
