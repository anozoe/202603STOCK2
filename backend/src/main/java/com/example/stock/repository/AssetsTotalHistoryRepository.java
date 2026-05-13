package com.example.stock.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.stock.entity.AssetsTotalHistory;

public interface AssetsTotalHistoryRepository extends JpaRepository<AssetsTotalHistory, Integer> {
    /**
     * fridayJudgeを渡して、assets_total_historyに資産履歴を追加していく。
     * @param fridayJudge　金曜なら1、それ以外なら2
     * @return
     */
    @Modifying
    @Query(value = """
        INSERT INTO assets_total_history(
            user_id, 
            buying_power, 
            holdings_value, 
            total_assets, 
            unrealized_pnl, 
            unrealized_pnl_ratio,
            friday_judge,
            created_at)
        SELECT  user_id, 
                buying_power, 
                holdings_value, 
                total_assets, 
                unrealized_pnl, 
                unrealized_pnl_ratio,
                :fridayJudge,
                CURRENT_TIMESTAMP
        FROM assets_total
        """, nativeQuery = true
    )
    int insertHistoryFromAssetsTotal(Integer fridayJudge);

}
