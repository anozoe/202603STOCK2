package com.example.stock.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.example.stock.batch.dto.ExecutionInfoResponse;
import com.example.stock.batch.service.ExecutionInfoService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/batch")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class BatchController {

    private final ExecutionInfoService executionInfoService;
    @GetMapping("/execution-info")
    public List<ExecutionInfoResponse> getExecutionInfo(){
        return executionInfoService.getExecutionInfo();
    }
    
}
