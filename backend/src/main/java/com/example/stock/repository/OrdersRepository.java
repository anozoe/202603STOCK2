package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<OrdersRepository, Integer>{
    //TODO: 注文約定履歴取得API_ユーザごと
    //TODO: 板情報取得API_銘柄ごとの注文データ取得
    //TODO: 注文登録API
    //TODO: 管理者注文約定一覧取得API
    //TODO: 約定通知API_ステータス、約定価格登録
}
