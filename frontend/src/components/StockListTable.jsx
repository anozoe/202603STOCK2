import { Link } from "react-router-dom";
import "../styles/StockListTable.css";

function formatPrice(value) {
  if (value === null || value === undefined || value === "") return "-";
  return Number(value).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function formatPercent(value) {
  if (value === null || value === undefined || value === "") return "-";
  return `${Number(value).toFixed(2)}%`;
}

function formatMarketCapThousandDollar(value) {
  if (value === null || value === undefined || value === "") return "-";
  return Math.round(Number(value) / 1000).toLocaleString();
}

function marketLabel(code) {
  const map = {
    1: "NASDAQ",
    2: "NYSE",
    3: "AMEX",
  };
  return map[code] || "-";
}

function getDiffClass(value) {
  const num = Number(value);
  if (Number.isNaN(num)) return "";
  if (num > 0) return "stock-value-plus";
  if (num < 0) return "stock-value-minus";
  return "";
}

function renderSignedPrice(value) {
  const num = Number(value);
  if (value === null || value === undefined || value === "" || Number.isNaN(num)) {
    return "---";
  }
  if (num > 0) return `+${formatPrice(num)}`;
  if (num < 0) return `-${formatPrice(Math.abs(num))}`;
  return formatPrice(num);
}

function renderSignedPercent(value) {
  const num = Number(value);
  if (value === null || value === undefined || value === "" || Number.isNaN(num)) {
    return "0.00%";
  }
  if (num > 0) return `+${formatPercent(num)}`;
  if (num < 0) return `-${formatPercent(Math.abs(num))}`;
  return formatPercent(num);
}

function StockListTable({
  title,
  currentCount,
  maxCount,
  items,
  onToggleFavorite,
  fromPath,
}) {
  return (
    <div className="stock-list-table-section">
      <div className="stock-list-table-header">
        <h2 className="stock-list-table-title">{title}</h2>
        <div className="stock-list-favorite-count">
          お気に入り　{currentCount}/{maxCount}件
        </div>
      </div>

      <table className="stock-list-table">
        <thead>
          <tr>
            <th>銘柄コード</th>
            <th>銘柄名</th>
            <th>市場</th>
            <th>現在値</th>
            <th>前日比</th>
            <th>騰落率</th>
            <th>時価総額（千ドル）</th>
            <th>お気に入り</th>
          </tr>
        </thead>
        <tbody>
          {items.length === 0 ? (
            <tr>
              <td colSpan="8" className="stock-list-empty">
                該当する銘柄はありません。
              </td>
            </tr>
          ) : (
            items.map((item) => (
              <tr key={item.tickerCode}>
                <td>{item.tickerCode}</td>
                <td>
                  <Link
                    to={`/stocks/${item.tickerCode}`}
                    state={{ fromPath }}
                    className="stock-name-link"
                  >
                    {item.stockName}
                  </Link>
                </td>
                <td>{item.market}</td>
                <td>{formatPrice(item.currentPrice)}</td>
                <td className={getDiffClass(item.priceChange)}>
                  {renderSignedPrice(item.priceChange)}
                </td>
                <td className={getDiffClass(item.changeRate)}>
                  {renderSignedPercent(item.changeRate)}
                </td>
                <td>{formatMarketCapThousandDollar(item.marketCap)}</td>
                <td>
                  <button
                    type="button"
                    className={`favorite-button ${item.favorite ? "is-favorite" : "is-not-favorite"}`}
                    onClick={() => onToggleFavorite(item.tickerCode, item.favorite)}
                  >
                    {item.favorite ? "★" : "☆"}
                  </button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export default StockListTable;