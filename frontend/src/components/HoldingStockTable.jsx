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

function formatAmount(value) {
  if (value === null || value === undefined || value === "") return "-";
  return Number(value).toLocaleString();
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

// 損益 = 評価額合計 - 取得額合計
function calcPnl(totalMarketValue, totalCost) {
  const mv = Number(totalMarketValue);
  const cost = Number(totalCost);
  if (Number.isNaN(mv) || Number.isNaN(cost)) return null;
  return mv - cost;
}

// 損益率 = 損益 / 取得額合計 * 100
function calcPnlRatio(totalMarketValue, totalCost) {
  const mv = Number(totalMarketValue);
  const cost = Number(totalCost);
  if (Number.isNaN(mv) || Number.isNaN(cost) || cost === 0) return null;
  return ((mv - cost) / cost) * 100;
}

function HoldingStockTable({ items, fromPath }) {
    const filteredItems = items.filter((item) => Number(item.totalHoldingAmount) > 0);
  return (
    <div className="stock-list-table-section">
      <div className="stock-list-table-header">
        <h2 className="stock-list-table-title">保有銘柄</h2>
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
            <th>保有数量</th>
            <th>評価額</th>
            <th>損益</th>
            <th>損益率</th>
          </tr>
        </thead>
        <tbody>
          {filteredItems.length === 0 ? (
            <tr>
              <td colSpan="10" className="stock-list-empty">
                保有銘柄はありません。
              </td>
            </tr>
          ) : (
            filteredItems.map((item) => {
              const pnl = calcPnl(item.totalMarketValue, item.totalCost);
              const pnlRatio = calcPnlRatio(item.totalMarketValue, item.totalCost);
              return (
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
                  <td>{formatAmount(item.totalHoldingAmount)}</td>
                  <td>{formatPrice(item.totalMarketValue)}</td>
                  <td className={getDiffClass(pnl)}>
                    {renderSignedPrice(pnl)}
                  </td>
                  <td className={getDiffClass(pnlRatio)}>
                    {renderSignedPercent(pnlRatio)}
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}

export default HoldingStockTable;