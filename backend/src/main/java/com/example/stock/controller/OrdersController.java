package com.example.stock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock.dto.OrderRequest;
import com.example.stock.dto.OrderResponse;
import com.example.stock.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrdersController {
    private final OrderService orderService;

    public OrdersController (OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> orderRegister(@RequestBody OrderRequest request) {
        OrderResponse response = orderService.orderRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    

}
