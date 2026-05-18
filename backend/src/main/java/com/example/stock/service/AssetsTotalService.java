package com.example.stock.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.dto.AsssetsTotalResponse;
import com.example.stock.entity.AssetsTotal;
import com.example.stock.repository.AssetsTotalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetsTotalService {
    private final AssetsTotalRepository assetsTotalRepository;

    @Transactional
    public AsssetsTotalResponse getAssetsTotal(Integer userId) {
        AssetsTotal total = assetsTotalRepository.findByUserId(userId).orElseThrow();
        AsssetsTotalResponse totalByUser = new AsssetsTotalResponse(
            total.getBuyingPower(), 
            total.getHoldingsValue(),
            total.getTotalAssets(), 
            total.getUnrealizedPnl(),
            total.getUnrealizedPnlRatio()
        );
        return totalByUser;
    }
}
