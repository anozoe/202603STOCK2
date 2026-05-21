package com.example.stock.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.entity.AssetsTotalHistory;
import com.example.stock.repository.AssetsTotalHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetsTotalHistoryService {
    private final AssetsTotalHistoryRepository assetsTotalHistoryRepository;

    @Transactional
    public List<AssetsTotalHistory> getHistory(Integer userId){
        List<AssetsTotalHistory> histories = assetsTotalHistoryRepository.findByUserId(userId);
        return histories;
    }
}
