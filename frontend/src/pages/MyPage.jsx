import { useEffect, useRef, useState } from "react";
import StockListTable from "../components/StockListTable";
import Pagination from "../components/Pagination";
import {
  fetchMyInfo,
  updateMyInfo,
  fetchMyFavorites,
  removeFavorite,
} from "../api/userApi";
import "../styles/MyPage.css";
import UserNameField from "../components/UserNameField";
import EmailField from "../components/EmailField";
import Header from "../components/Header";
import StockListPage from "./StockListPage";
import StockList from "../components/StockList";
import { getLoginUserId } from "../utils/authHeader";
import { fetchAssetTotal, fetchHoldingStock } from "../api/AssetsApi";
import '../styles/StockPrice.css'
import { useSearchParams } from "react-router-dom";
import HoldingStockTable from "../components/HoldingStockTable";
import TotalAssetsChart from "../components/TotalChart";
import { PieChart } from "recharts";
import { SimplePieChart } from "../components/PieChart";

const PAGE_SIZE = 20;

function getDiffClass(value) {
  const num = Number(value);
  if (Number.isNaN(num)) return "";
  if (num > 0) return "stock-detail-plus";
  if (num < 0) return "stock-detail-minus";
  return "";
}

function normalizeUserName(value) {
  return value.replace(/[\s　]+/g, "");
}

function validateUserName(value) {
  if (!value) return "ユーザ名は必須です。";
  if (value.length > 30) return "ユーザ名は30文字までです。";
  if (/[ -~]/.test(value)) return "正しいユーザ名を入力してください。";
  return "";
}

function validateEmail(value) {
  if (!value) return "メールアドレスは必須です。";
  if (value.length > 50) return "メールアドレスは50文字までです。";
  const regex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
  if (!regex.test(value)) return "正しいメールアドレスを入力してください。";
  return "";
}


function MyPage() {
  const [mode, setMode] = useState("display");
  const [message, setMessage] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [assetsTotalData, setAssetsTotalData] = useState(null);
  const userId = getLoginUserId();

  const [searchParams, setSearchParams] = useSearchParams();
  const [activeTab, setActiveTab] = useState(() => {
    return sessionStorage.getItem("mypage_tab") || "home";
  });

  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setSearchParams({ tab });
    sessionStorage.setItem("mypage_tab", tab);
  };

  async function handleHomeTab() {
    handleTabChange("home");
    await initialize();
  }

  const [userInfo, setUserInfo] = useState({ userName: "", email: "" });
  const [form, setForm] = useState({ userName: "", email: "" });
  const [errors, setErrors] = useState({ userName: "", email: "" });
  const [favoriteData, setFavoriteData] = useState({
    totalFavorites: 0,
    page: 0,
    size: 20,
    totalPages: 0,
    currentFavoriteCount: 0,
    maxFavoriteCount: 20,
    items: [],
  });

  useEffect(() => {
    initialize();
  }, []);

  useEffect(() => {
    loadFavorites(currentPage);
  }, [currentPage]);

  async function initialize() {
    try {
      const userRes = await fetchMyInfo();
      const user = userRes.data;

      setUserInfo({
        userName: user.userName || "",
        email: user.email || "",
      });
      setForm({
        userName: user.userName || "",
        email: user.email || "",
      });

      const favoriteRes = await fetchMyFavorites(0, PAGE_SIZE);
      setFavoriteData(favoriteRes.data);
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function loadFavorites(page) {
    try {
      const res = await fetchMyFavorites(page, PAGE_SIZE);
      setFavoriteData(res.data);
    } catch (error) {
      setMessage(error.message);
    }
  }

  const userNameRef = useRef();
  const emailRef = useRef();

  async function handleUpdate() {
    const isUserNameValid = userNameRef.current.validate();
    const isEmailValid = emailRef.current.validate();
    if (!isUserNameValid || !isEmailValid) return;

    const normalizedUserName = normalizeUserName(form.userName);
    const trimmedEmail = form.email.trim();

    const nextErrors = {
      userName: validateUserName(normalizedUserName),
      email: validateEmail(trimmedEmail),
    };
    setErrors(nextErrors);

    if (nextErrors.userName || nextErrors.email) return;

    try {
      const res = await updateMyInfo({
        userName: normalizedUserName,
        email: trimmedEmail,
      });

      setUserInfo({
        userName: res.data.userName,
        email: res.data.email,
      });
      setForm({
        userName: res.data.userName,
        email: res.data.email,
      });
      setMode("display");
      setMessage(res.message || "更新しました。");
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function handleRemoveFavorite(tickerCode) {
    try {
      const res = await removeFavorite(tickerCode);
      setMessage(res.message || "お気に入り解除しました。");
      await loadFavorites(currentPage);
    } catch (error) {
      setMessage(error.message);
    }
  }

  useEffect(() => {
    const loadAssetsTotal = async () => {
      if (!userId) return;
      try {
        const res = await fetchAssetTotal(userId);
        console.log("-----------------");
        console.log(res);
        setAssetsTotalData(res);
        setMessage('')
      } catch (error) {
        setMessage(error.message || '取得に失敗しました。');
      }

    };
    loadAssetsTotal();
  }, []);

  const unrealizedPnl = assetsTotalData?.unrealizedPnl ?? 0;
  const unrealizedPnlRatio = assetsTotalData?.unrealizedPnlRatio ?? 0;
  const diffClass = getDiffClass(unrealizedPnl);

  const [holdingStockData, setHoldingStockData] = useState([]);
  useEffect(() => {
    const loadHoldinStock = async () => {
      if (!userId) return;
      try {
        const res = await fetchHoldingStock(userId);
        console.log("-----------------");
        console.log(res);
        setHoldingStockData(res);
        setMessage('')
      } catch (error) {
        setMessage(error.message || '取得に失敗しました。');
      }

    };
    loadHoldinStock();
  }, []);

  return (
    <div className="mypage-screen">
      <Header />
      <div className="assets-card">
        <div className="assets-top">
          <div className="assets-label">総資産</div>
          <div className="assets-total">${Number(assetsTotalData?.totalAssets ?? 0).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2, })}</div>
          <div className="assets-pnl">
            <span className="assets-pnl-label">評価損益</span>
            <span className={`value ${diffClass}`}>
              {unrealizedPnl > 0 ? "+$" : unrealizedPnl < 0 ? "-$" : "$"}{Math.abs(unrealizedPnl ?? 0)}
              {unrealizedPnlRatio != null ? `（${unrealizedPnlRatio > 0 ? "+" : unrealizedPnlRatio < 0 ? "-" : ""}${Math.abs(unrealizedPnlRatio)}%）` : ''}
            </span>
          </div>
        </div>
        <div className="assets-bottom">
          <div className="assets-bottom-item">
            <div className="assets-label">保有資産評価額</div>
            <div className="assets-bottom-value">${assetsTotalData?.holdingValue}</div>
          </div>
          <div className="assets-bottom-item">
            <div className="assets-label">買付可能額</div>
            <div className="assets-bottom-value">
              <span className="assets-bottom-value">
                ${assetsTotalData?.buyingPower}
              </span>
              <button type="button" className="assets-add-button">追加</button>
            </div>
          </div>
        </div>
      </div>
      <div>
        <div className='tab-header'>
          <button
            className={`tab-button ${activeTab === 'home' ? 'active' : ''}`}
            onClick={handleHomeTab}
          >
            ホーム
          </button>
          <button
            className={`tab-button ${activeTab === 'list' ? 'active' : ''}`}
            onClick={() => handleTabChange('list')}
          >
            銘柄一覧
          </button>
          <button
            className={`tab-button ${activeTab === 'holding' ? 'active' : ''}`}
            onClick={() => handleTabChange('holding')}
          >
            保有銘柄
          </button>
        </div>
        <div>
          {activeTab === 'home' && (
            <div>
              <StockListTable
                title="お気に入り銘柄"
                currentCount={favoriteData.currentFavoriteCount}
                maxCount={favoriteData.maxFavoriteCount}
                items={favoriteData.items}
                onToggleFavorite={handleRemoveFavorite}
                fromPath={`/mypage?tab=${activeTab}`}
              />

              <Pagination
                currentPage={currentPage}
                totalCount={favoriteData.totalFavorites}
                pageSize={PAGE_SIZE}
                onPageChange={setCurrentPage}
              />
            </div>
          )}
          {activeTab === 'list' && (
            <StockList fromPath={`/mypage?tab=${activeTab}`} />
          )}
          {activeTab === 'holding' && (
            <div>
              <HoldingStockTable
                items={holdingStockData}
                fromPath={`/mypage?tab=${activeTab}`}
              />
              <TotalAssetsChart
                userId = {userId}
              />
              <SimplePieChart
                items={holdingStockData}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default MyPage;