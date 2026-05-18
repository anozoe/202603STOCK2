package com.example.stock.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock.dto.AssetsStockResponse;
import com.example.stock.dto.SumHoldingAmountResponse;
import com.example.stock.service.AssetsStockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/users/me")
@CrossOrigin(origins = "http://localhost:3000")
public class AssetsStockController {
    private final AssetsStockService assetsStockService;

    public AssetsStockController (AssetsStockService assetsStockService){
        this.assetsStockService = assetsStockService;
    }

    @GetMapping("/stock")
    public List<AssetsStockResponse> getAssetsStock(@RequestParam Integer userId) {
        return assetsStockService.getAssetsStock(userId);
    }

    @GetMapping("/amount")
    public SumHoldingAmountResponse getSumHoldingAmount(@RequestParam Integer userId, @RequestParam String tickerCode) {
        return assetsStockService.getSumHoldingAmount(userId, tickerCode);
    }
    
    
}
