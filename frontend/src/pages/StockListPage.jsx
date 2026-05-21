import { useEffect, useState } from "react";
import Header from "../components/Header";
import StockListTable from "../components/StockListTable";
import Pagination from "../components/Pagination";
import { fetchStocks, addFavorite, removeFavorite } from "../api/stockApi";
import "../styles/StockListPage.css";

const PAGE_SIZE = 20;

function StockListPage() {
  const [keyword, setKeyword] = useState("");
  const [message, setMessage] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [filterdStockData, setFilterdStockData] = useState([]);
  const [stockData, setStockData] = useState({
    totalCount: 0,
    page: 0,
    size: PAGE_SIZE,
    totalPages: 0,
    currentFavoriteCount: 0,
    maxFavoriteCount: 20,
    items: [],
  });


  useEffect(() => {
    loadStocks(0, "");
  }, []);

  useEffect(() => {
    loadStocks(currentPage);
  }, [currentPage]);

  async function loadStocks(page) {
    try {
      const res = await fetchStocks(page, PAGE_SIZE);
      setStockData(res.data);
      setFilterdStockData(res.data.items);
      setMessage("");
    } catch (error) {
      console.error("fetchStocks error:", error);
      setMessage(error.message || "処理に失敗しました。");
      setStockData({
        totalCount: 0,
        page: 0,
        size: PAGE_SIZE,
        totalPages: 0,
        currentFavoriteCount: 0,
        maxFavoriteCount: 20,
        items: [],
      });
    }
  }

  function filterList(e) {
    let keywordValue = e.target.value;
    setKeyword(keywordValue);
    keywordValue = keywordValue.toUpperCase();
    setFilterdStockData(
      stockData.items.filter(
        (item) =>
          (item.tickerCode ?? "").includes(keywordValue) ||
          (item.stockName.toUpperCase() ?? "").includes(keywordValue)
      ) ?? []
    );
  }

  async function handleToggleFavorite(tickerCode, isFavorite) {
    try {
      if (isFavorite) {
        await removeFavorite(tickerCode);
      } else {
        await addFavorite(tickerCode);
      }

      await loadStocks(currentPage);
    } catch (error) {
      console.error("favorite error:", error);
      setMessage(error.message || "処理に失敗しました。");
    }
  }

  return (
    <div className="stock-list-screen">
      <Header />

      <div className="stock-list-page">
        {message && <div className="page-message">{message}</div>}

        <div className="stock-search-area">
          <input
            type="text"
            className="stock-search-input"
            placeholder="銘柄コードまたは銘柄名"
            value={keyword}
            onChange={filterList}
          />
        </div>

        <StockListTable
          title="銘柄一覧"
          currentCount={stockData.currentFavoriteCount}
          maxCount={stockData.maxFavoriteCount}
          items={filterdStockData}
          onToggleFavorite={handleToggleFavorite}
          fromPath="/mypage?tab=${activeTab}"
        />

        <Pagination
          currentPage={currentPage}
          totalCount={stockData.totalCount}
          pageSize={PAGE_SIZE}
          onPageChange={setCurrentPage}
        />
      </div>
    </div>
  );
}

export default StockListPage;