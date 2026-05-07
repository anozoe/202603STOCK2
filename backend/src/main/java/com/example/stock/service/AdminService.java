package com.example.stock.service;

import com.example.stock.constants.BusinessConstants;
import com.example.stock.constants.RoleCode;
import com.example.stock.dto.AdminStockItemResponse;
import com.example.stock.dto.AdminStockListResponse;
import com.example.stock.dto.AdminStockReorderRequest;
import com.example.stock.dto.AdminStockUpsertRequest;
import com.example.stock.dto.AdminUserListResponse;
import com.example.stock.dto.UserInfoResponse;
import com.example.stock.entity.Stock;
import com.example.stock.entity.User;
import com.example.stock.exception.BusinessException;
import com.example.stock.repository.StockPriceHistoryRepository;
import com.example.stock.repository.StockRepository;
import com.example.stock.repository.UserFavoriteRepository;
import com.example.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final StockMarketSyncService stockMarketSyncService;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;

    @Transactional(readOnly = true)
    public AdminStockListResponse getStocks(int page, int size) {
        int pageIndex = Math.max(page, 0);

        Page<Stock> result = stockRepository.findAllByOrderByDisplayOrderAscIdAsc(
                PageRequest.of(pageIndex, size)
        );

        return new AdminStockListResponse(
                (int) result.getTotalElements(),
                pageIndex,
                size,
                result.getTotalPages(),
                result.getContent().stream()
                        .map(s -> new AdminStockItemResponse(
                                s.getId(),
                                s.getTickerCode(),
                                s.getStockName(),
                                s.getMarket(),
                                s.getCurrentPrice(),
                                s.getDisplayOrder()
                        ))
                        .toList()
        );
    }

    @Transactional
    public void createStock(AdminStockUpsertRequest req) {
        if (stockRepository.count() >= BusinessConstants.MAX_ADMIN_STOCK_COUNT) {
            throw new BusinessException("E012", "銘柄", "登録");
        }

        String tickerCode = req.getTickerCode().trim().toUpperCase();

        if (stockRepository.findByTickerCode(tickerCode).isPresent()) {
            throw new BusinessException("E005", "銘柄コード");
        }

        stockMarketSyncService.syncByTicker(tickerCode, null);
    }

    @Transactional
    public void updateStock(AdminStockUpsertRequest req) {
        if (req.getId() == null) {
            throw new BusinessException("E001", "ID");
        }

        Stock existing = stockRepository.findById(req.getId())
                .orElseThrow(() -> new BusinessException("E010", "銘柄"));

        String tickerCode = req.getTickerCode().trim().toUpperCase();

        if (stockRepository.existsByTickerCodeAndIdNot(tickerCode, req.getId())) {
            throw new BusinessException("E005", "銘柄コード");
        }

        stockMarketSyncService.syncByTicker(tickerCode, existing.getDisplayOrder());
    }

    @Transactional
    public void deleteStock(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new BusinessException("E010", "銘柄"));

        userFavoriteRepository.deleteByStockId(stock.getId());
        stockPriceHistoryRepository.deleteByStockId(stock.getId());
        stockRepository.delete(stock);

     normalizeDisplayOrder();
    }

    @Transactional
public void reorder(AdminStockReorderRequest req) {
    if (req == null || req.getStockIds() == null || req.getStockIds().isEmpty()) {
        throw new BusinessException("E001", "並び順");
    }

    List<Long> stockIds = req.getStockIds();
    System.out.println("reorder start");
    System.out.println("stockIds = " + stockIds);

    List<Stock> stocks = stockRepository.findAllById(stockIds);
    System.out.println("stocks.size = " + stocks.size());
    System.out.println("stocks ids = " + stocks.stream().map(Stock::getId).toList());

    if (stocks.size() != stockIds.size()) {
        throw new IllegalStateException(
                "取得した銘柄件数が一致しません。 request=" + stockIds
                        + ", actual=" + stocks.stream().map(Stock::getId).toList()
        );
    }

    Map<Long, Integer> orderMap = new HashMap<>();
    for (int i = 0; i < stockIds.size(); i++) {
        orderMap.put(stockIds.get(i), i + 1);
    }
    System.out.println("orderMap = " + orderMap);

    for (Stock stock : stocks) {
        Integer displayOrder = orderMap.get(stock.getId());
        if (displayOrder == null) {
            throw new IllegalStateException("displayOrder が見つかりません。 stockId=" + stock.getId());
        }
        stock.setDisplayOrder(displayOrder);
    }

    stockRepository.saveAll(stocks);
    stockRepository.flush();

    System.out.println("reorder success");
}

    @Transactional(readOnly = true)
    public AdminUserListResponse getUsers(int page, int size) {
        var result = userRepository.findByDeletedAtIsNullOrderByIdAsc(PageRequest.of(page, size));

        return new AdminUserListResponse(
                userRepository.countByDeletedAtIsNull(),
                BusinessConstants.MAX_ADMIN_USER_DISPLAY_COUNT,
                result.getContent().stream()
                        .map(this::toUserInfoResponse)
                        .toList()
        );
    }

    @Transactional
    public void logicalDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("E010", "ユーザ"));

        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy("admin");
        userRepository.save(user);
    }

    private void normalizeDisplayOrder() {
        List<Stock> allStocks = stockRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((Stock s) -> s.getDisplayOrder() == null ? Integer.MAX_VALUE : s.getDisplayOrder())
                        .thenComparing(Stock::getId))
                .toList();

        int order = 1;
        for (Stock stock : allStocks) {
            stock.setDisplayOrder(order++);
        }
        stockRepository.saveAll(allStocks);
    }

    private UserInfoResponse toUserInfoResponse(User user) {
        String roleName = user.getRole() != null && user.getRole() == RoleCode.ADMIN
                ? "管理者"
                : "一般ユーザ";

        return new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roleName,
                user.getUpdatedAt() == null ? null : user.getUpdatedAt().toString()
        );
    }
}