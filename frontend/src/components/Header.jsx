import React from "react";
import "../styles/Header.css";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { getLoginUserName } from "../utils/authHeader";

const pageTitles = {
  "/admin": "管理者",
  "/mypage": "マイページ",
  "/stocks": "銘柄一覧",
  "/order": "注文"
};

function getTitle(pathname) {
  if (pageTitles[pathname]) return pageTitles[pathname];
  if (pathname.startsWith("/stocks/")) return "株価詳細";
  return "画面名";
}

function Header() {
  const location = useLocation();
  const navigate = useNavigate();

  const title = getTitle(location.pathname);
  const userName = getLoginUserName();

  const handleLogout = () => {
    localStorage.removeItem("loginUserId");
    localStorage.removeItem("loginUserName");
    localStorage.removeItem("loginUserEmail");
    localStorage.removeItem("loginUserRole");
    navigate("/");
  };

  return (
    <header className="header-top">
      <div className="header-title">{title}</div>
      <div className="header-link">
        <Link to="/mypage">{userName}</Link>
        <button className="logout-btn" onClick={handleLogout}>
          ログアウト
        </button>
      </div>
    </header>
  );
}

export default Header;