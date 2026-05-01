package com.example.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetsTotalRepository extends JpaRepository<AssetsTotalRepository, Integer>{
    //TODO:資産サマリー取得API_ユーザごと取得
    //TODO:買付可能額更新API_追加した買付可能額分、buyingPowerとtotalAssetsを増やす
    //TODO: 市場クローズAPI_注文キャンセルになった分、buyingPowerやtotalAssetsを増やす
    //TODO: 約定情報取得API_各ユーザの資産情報を取得
}
