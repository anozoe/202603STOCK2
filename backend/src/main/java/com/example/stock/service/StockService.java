package com.example.stock.service;

import com.example.stock.constants.BusinessConstants;
import com.example.stock.dto.FavoriteStockListResponse;
import com.example.stock.dto.FavoriteToggleResponse;
import com.example.stock.dto.StockChartPointResponse;
import com.example.stock.dto.StockDetailResponse;
import com.example.stock.dto.StockListItemResponse;
import com.example.stock.dto.StockListResponse;
import com.example.stock.dto.StockOverviewResponse;
import com.example.stock.entity.Stock;
import com.example.stock.entity.StockPriceHistory;
import com.example.stock.entity.User;
import com.example.stock.entity.UserFavorite;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository stockPriceHistoryRepository;
    private final UserFavoriteRepository userFavoriteRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public StockListResponse getStocks(String keyword, int page, int size) {
        Long currentUserId = currentUserService.getCurrentUserId();

        int pageIndex = Math.max(page, 0);
        String trimmedKeyword = keyword == null ? "" : keyword.trim();

        Page<Stock> result;
        if (trimmedKeyword.isEmpty()) {
            result = stockRepository.findAllByOrderByDisplayOrderAscIdAsc(
                    PageRequest.of(pageIndex, size)
            );
        } else {
            result = stockRepository
                    .findByTickerCodeContainingIgnoreCaseOrStockNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(
                            trimmedKeyword,
                            trimmedKeyword,
                            PageRequest.of(pageIndex, size)
                    );
        }

        int currentFavoriteCount = userFavoriteRepository.countByUserId(currentUserId);

        List<StockListItemResponse> items = result.getContent().stream()
                .map(stock -> new StockListItemResponse(
                        stock.getTickerCode(),
                        stock.getStockName(),
                        stock.getMarket(),
                        stock.getCurrentPrice(),
                        stock.getPriceChange(),
                        stock.getChangeRate(),
                        stock.getMarketCap(),
                        userFavoriteRepository.existsByUserIdAndStockId(
                                currentUserId,
                                stock.getId()
                        )
                ))
                .toList();

        return new StockListResponse(
                Math.toIntExact(result.getTotalElements()),
                pageIndex,
                size,
                result.getTotalPages(),
                currentFavoriteCount,
                BusinessConstants.MAX_FAVORITE_COUNT,
                items
        );
    }

    @Transactional(readOnly = true)
    public FavoriteStockListResponse getFavoriteStocks(int page, int size) {
        Long currentUserId = currentUserService.getCurrentUserId();

        int pageIndex = Math.max(page, 0);

        Page<UserFavorite> result = userFavoriteRepository.findByUserIdOrderByStockIdAsc(
                currentUserId,
                PageRequest.of(pageIndex, size)
        );

        int currentFavoriteCount = userFavoriteRepository.countByUserId(currentUserId);

        List<StockListItemResponse> items = result.getContent().stream()
                .map(UserFavorite::getStock)
                .sorted(Comparator
                        .comparing((Stock s) -> s.getDisplayOrder() == null ? Integer.MAX_VALUE : s.getDisplayOrder())
                        .thenComparing(Stock::getId))
                .map(stock -> new StockListItemResponse(
                        stock.getTickerCode(),
                        stock.getStockName(),
                        stock.getMarket(),
                        stock.getCurrentPrice(),
                        stock.getPriceChange(),
                        stock.getChangeRate(),
                        stock.getMarketCap(),
                        true
                ))
                .toList();

        return new FavoriteStockListResponse(
                Math.toIntExact(result.getTotalElements()),
                pageIndex,
                size,
                result.getTotalPages(),
                currentFavoriteCount,
                BusinessConstants.MAX_FAVORITE_COUNT,
                items
        );
    }

    @Transactional(readOnly = true)
    public StockDetailResponse getStockDetail(String tickerCode) {
        Stock stock = stockRepository.findByTickerCode(tickerCode)
                .orElseThrow(() -> new BusinessException("E010", "銘柄"));

        StockOverviewResponse overview = new StockOverviewResponse(
                stock.getOpenPrice(),
                stock.getHighPrice(),
                stock.getLowPrice(),
                stock.getClosePrice(),
                stock.getPer(),
                stock.getPbr(),
                stock.getRoe(),
                stock.getDividendYield(),
                stock.getVolume(),
                stock.getMarketCap()
        );

        List<StockChartPointResponse> weekChart = toChartResponse(
                stockPriceHistoryRepository.findTop7ByStockIdOrderByPriceDateDesc(stock.getId())
        );

        List<StockChartPointResponse> monthChart = toChartResponse(
                stockPriceHistoryRepository.findTop30ByStockIdOrderByPriceDateDesc(stock.getId())
        );

        return new StockDetailResponse(
                stock.getTickerCode(),
                stock.getStockName(),
                stock.getMarket(),
                stock.getMarketStatus(),
                stock.getCurrentPrice(),
                stock.getPriceChange(),
                stock.getChangeRate(),
                stock.getFetchedAt(),
                overview,
                weekChart,
                monthChart
        );
    }

    @Transactional
    public FavoriteToggleResponse addFavorite(String tickerCode) {
        Long currentUserId = currentUserService.getCurrentUserId();

        Stock stock = stockRepository.findByTickerCode(tickerCode)
                .orElseThrow(() -> new BusinessException("E010", "銘柄"));

        int currentFavoriteCount = userFavoriteRepository.countByUserId(currentUserId);
        if (currentFavoriteCount >= BusinessConstants.MAX_FAVORITE_COUNT) {
            throw new BusinessException("E012", "お気に入り銘柄", "登録");
        }

        boolean exists = userFavoriteRepository.existsByUserIdAndStockId(
                currentUserId,
                stock.getId()
        );

        if (!exists) {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException("E010", "ユーザ"));

            UserFavorite favorite = new UserFavorite();
            favorite.setUser(user);
            favorite.setStock(stock);
            favorite.setCreatedAt(LocalDateTime.now());
            favorite.setCreatedBy("system");
            userFavoriteRepository.save(favorite);
        }

        return new FavoriteToggleResponse(true);
    }

    @Transactional
    public FavoriteToggleResponse removeFavorite(String tickerCode) {
        Long currentUserId = currentUserService.getCurrentUserId();

        Stock stock = stockRepository.findByTickerCode(tickerCode)
                .orElseThrow(() -> new BusinessException("E010", "銘柄"));

        boolean exists = userFavoriteRepository.existsByUserIdAndStockId(
                currentUserId,
                stock.getId()
        );

        if (exists) {
            userFavoriteRepository.deleteByUserIdAndStockId(
                    currentUserId,
                    stock.getId()
            );
        }

        return new FavoriteToggleResponse(false);
    }

    private List<StockChartPointResponse> toChartResponse(List<StockPriceHistory> histories) {
        List<StockPriceHistory> sorted = histories.stream()
                .sorted(Comparator.comparing(StockPriceHistory::getPriceDate))
                .toList();

        List<StockChartPointResponse> result = new ArrayList<>();

        for (int i = 0; i < sorted.size(); i++) {
            StockPriceHistory history = sorted.get(i);

            BigDecimal movingAverage5 = calculateMovingAverage5(sorted, i);

            result.add(new StockChartPointResponse(
                    history.getPriceDate(),
                    history.getOpenPrice(),
                    history.getHighPrice(),
                    history.getLowPrice(),
                    history.getClosePrice(),
                    movingAverage5
            ));
        }

        return result;
    }

    private BigDecimal calculateMovingAverage5(List<StockPriceHistory> histories, int currentIndex) {
        int start = Math.max(0, currentIndex - 4);

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (int i = start; i <= currentIndex; i++) {
            if (histories.get(i).getClosePrice() != null) {
                total = total.add(histories.get(i).getClosePrice());
                count++;
            }
        }

        if (count == 0) {
            return null;
        }

        return total.divide(BigDecimal.valueOf(count), 4, java.math.RoundingMode.HALF_UP);
    }
}