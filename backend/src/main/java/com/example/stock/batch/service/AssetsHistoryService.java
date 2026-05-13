package com.example.stock.batch.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stock.batch.dto.BatchResultDto;
import com.example.stock.constants.DAY_TYPE;
import com.example.stock.repository.AssetsTotalHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AssetsHistoryService {
    private final AssetsTotalHistoryRepository assetsTotalHistoryRepository;

    public BatchResultDto addAssetsTotal(){
        log.info("資産履歴登録 開始");

        int fridayJudge;
        if (LocalDate.now().getDayOfWeek().getValue() == DayOfWeek.FRIDAY.getValue()) {
            fridayJudge = DAY_TYPE.FRIDAY.getCode();
        } else {
            fridayJudge = DAY_TYPE.OTHER.getCode();
        }

        int insertCount = assetsTotalHistoryRepository.insertHistoryFromAssetsTotal(fridayJudge);
        
        log.info("資産履歴登録 終了 件数={} fridayJudge={}", insertCount, fridayJudge);
        return new BatchResultDto(insertCount, 0, "SUCCESS");
    }
}
