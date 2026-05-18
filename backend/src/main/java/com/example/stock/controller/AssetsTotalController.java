package com.example.stock.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock.dto.AsssetsTotalResponse;
import com.example.stock.service.AssetsTotalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/users/me")
@CrossOrigin(origins = "http://localhost:3000")
public class AssetsTotalController {
    private final AssetsTotalService assetsTotalService2;

    public AssetsTotalController (AssetsTotalService assetsTotalService){
        assetsTotalService2 = assetsTotalService;
    }

    /**
     * 資産サマリーAPI
     * @param userId
     * @return
     */
    @GetMapping("/assets")
    public AsssetsTotalResponse getAssetsTotal (@RequestParam Integer userId) {
        return assetsTotalService2.getAssetsTotal(userId);
    }
    
}
