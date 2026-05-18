import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import MyPage from "./pages/MyPage";
import StockListPage from "./pages/StockListPage";
import StockDetailPage from "./pages/StockDetailPage";
import AdminPage from "./pages/AdminPage";
import "./styles/common.css";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage"
import OrderPage from "./pages/OrderPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/mypage" element={<MyPage />} />
        <Route path="/stocks" element={<StockListPage />} />
        <Route path="/stocks/:tickerCode" element={<StockDetailPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/order/:tickerCode" element={<OrderPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;