import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import Header from "../components/Header";
import { fetchStockDetail } from "../api/stockApi";
import "../styles/StockDetailPage.css";

function marketLabel(code) {
  const map = {
    1: "NASDAQ",
    2: "NYSE",
    3: "AMEX",
  };
  return map[code] || "-";
}

function formatPriceWithDollar(value) {
  if (value === null || value === undefined || value === "") return "-";
  return `$${Number(value).toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function formatPercent(value) {
  if (value === null || value === undefined || value === "") return "-";
  return `${Number(value).toFixed(2)}%`;
}

function formatNumber(value) {
  if (value === null || value === undefined || value === "") return "-";
  return Number(value).toLocaleString();
}

function formatDateYYYYMMDD(value) {
  if (!value) return "-";

  const raw = String(value).slice(0, 10);
  const parts = raw.split("-");
  if (parts.length === 3) {
    return `${parts[0]}/${parts[1]}/${parts[2]}`;
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "-";

  const yyyy = String(date.getFullYear());
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yyyy}/${mm}/${dd}`;
}

function getDiffClass(value) {
  const num = Number(value);
  if (Number.isNaN(num)) return "";
  if (num > 0) return "stock-detail-plus";
  if (num < 0) return "stock-detail-minus";
  return "";
}

function calcChangeRate(currentPrice, priceChange, changeRate) {
  if (changeRate !== null && changeRate !== undefined && changeRate !== "") {
    return Number(changeRate);
  }

  const current = Number(currentPrice);
  const diff = Number(priceChange);

  if (Number.isNaN(current) || Number.isNaN(diff)) return null;

  const previous = current - diff;
  if (previous === 0) return null;

  return (diff / previous) * 100;
}

function normalizeChartData(points) {
  if (!Array.isArray(points)) return [];

  return points.map((point) => ({
    date: point.date,
    openPrice: Number(point.openPrice ?? 0),
    highPrice: Number(point.highPrice ?? 0),
    lowPrice: Number(point.lowPrice ?? 0),
    closePrice: Number(point.closePrice ?? 0),
    volume: Number(point.volume ?? 0),
    movingAverage5:
      point.movingAverage5 !== null && point.movingAverage5 !== undefined
        ? Number(point.movingAverage5)
        : null,
  }));
}

function withMovingAverage5(points) {
  return points.map((point, index) => {
    if (point.movingAverage5 !== null && point.movingAverage5 !== undefined) {
      return point;
    }

    const start = Math.max(0, index - 4);
    const target = points.slice(start, index + 1);
    const avg =
      target.reduce((sum, item) => sum + Number(item.closePrice || 0), 0) /
      target.length;

    return {
      ...point,
      movingAverage5: avg,
    };
  });
}

function formatChartDate(value) {
  return String(value).slice(5).replace("-", "/");
}

function buildPriceChart(points, width, height, chartType) {
  if (!points || points.length === 0) return null;

  const padding = { top: 18, right: 56, bottom: 44, left: 86 };
  const plotWidth = width - padding.left - padding.right;
  const plotHeight = height - padding.top - padding.bottom;

  const lows = points.map((p) => Number(p.lowPrice));
  const highs = points.map((p) => Number(p.highPrice));
  const minValue = Math.min(...lows);
  const maxValue = Math.max(...highs);
  const range = maxValue - minValue || 1;
  const displayMin = minValue - range * 0.08;
  const displayMax = maxValue + range * 0.08;
  const displayRange = displayMax - displayMin || 1;

  const baseUsableWidth = width - padding.left - padding.right;
  const bodyWidth = Math.max(
    8,
    Math.min(18, baseUsableWidth / Math.max(points.length * 2.8, 8))
  );

  const xStart = padding.left;
  const xEnd = width - padding.right;
  const usableWidth = xEnd - xStart;
  
  const getX = (index) => {
    if (points.length === 1) return xStart + usableWidth / 2;

    const step = usableWidth / points.length;
    return xStart + step * index + step / 2;
  };

  const getY = (value) =>
    padding.top + ((displayMax - Number(value)) / displayRange) * plotHeight;

  const yLines = Array.from({ length: 6 }, (_, index) => {
    const value = displayMin + (displayRange * (5 - index)) / 5;
    const y = padding.top + (plotHeight * index) / 5;
    return { value, y };
  });

  const linePoints = points
    .map((p, i) => `${getX(i)},${getY(p.closePrice)}`)
    .join(" ");

  return {
    padding,
    getX,
    getY,
    yLines,
    linePoints,
    bodyWidth,
    chartType,
    width,
    height,
    xStart,
    xEnd,
  };
}

function buildMaChart(points, width, height) {
  if (!points || points.length === 0) return null;

  const padding = { top: 18, right: 58, bottom: 44, left: 72 };
  const plotWidth = width - padding.left - padding.right;
  const plotHeight = height - padding.top - padding.bottom;

  const values = points.map((p) =>
    Number(p.movingAverage5 ?? p.closePrice ?? 0)
  );
  const minValue = Math.min(...values);
  const maxValue = Math.max(...values);
  const range = maxValue - minValue || 1;
  const displayMin = minValue - range * 0.1;
  const displayMax = maxValue + range * 0.1;
  const displayRange = displayMax - displayMin || 1;

  const edgePadding = plotWidth * 0.03;
  const innerWidth = plotWidth - edgePadding * 2;

  const getX = (index) => {
    if (points.length === 1) {
      return padding.left + plotWidth / 2;
    }

    const step = innerWidth / (points.length - 1);
    return padding.left + edgePadding + step * index;
  };

  const getY = (value) =>
    padding.top + ((displayMax - Number(value)) / displayRange) * plotHeight;

  const yLines = Array.from({ length: 6 }, (_, index) => {
    const value = displayMin + (displayRange * (5 - index)) / 5;
    const y = padding.top + (plotHeight * index) / 5;
    return { value, y };
  });

  const linePoints = points
    .map((p, i) => `${getX(i)},${getY(p.movingAverage5 ?? p.closePrice)}`)
    .join(" ");

  return {
    padding,
    getX,
    getY,
    yLines,
    linePoints,
    width,
    height,
  };
}

function PriceTrendChart({ data, chartType }) {
  const width = 690;
  const height = 210;
  const chart = useMemo(
    () => buildPriceChart(data, width, height, chartType),
    [data, chartType]
  );

  if (!data.length || !chart) {
    return <div className="stock-detail-chart-empty">チャートデータがありません。</div>;
  }

  return (
    <svg className="stock-detail-chart-svg" viewBox={`0 0 ${width} ${height}`}>
      {chart.yLines.map((line) => (
        <g key={`price-grid-${line.y}`}>
          <line
            x1={chart.xStart}
            y1={line.y}
            x2={chart.xEnd}
            y2={line.y}
            className="stock-detail-grid-line"
          />
          <text
            x={chart.xStart - 8}
            y={line.y + 4}
            textAnchor="end"
            className="stock-detail-axis-label"
          >
            {Math.round(line.value)}
          </text>
        </g>
      ))}

      <line
        x1={chart.xStart}
        y1={height - chart.padding.bottom}
        x2={chart.xEnd}
        y2={height - chart.padding.bottom}
        className="stock-detail-axis-line"
      />
      <line
        x1={chart.xStart}
        y1={chart.padding.top}
        x2={chart.xStart}
        y2={height - chart.padding.bottom}
        className="stock-detail-axis-line"
      />

      {chartType === "line" && (
        <>
          <polyline
            fill="none"
            points={chart.linePoints}
            className="stock-detail-line-series"
          />
          {data.map((point, index) => (
            <circle
              key={`price-point-${point.date}-${index}`}
              cx={chart.getX(index)}
              cy={chart.getY(point.closePrice)}
              r="4"
              className="stock-detail-line-dot"
            />
          ))}
        </>
      )}

      {chartType === "candle" &&
        data.map((point, index) => {
          const x = chart.getX(index);
          const highY = chart.getY(point.highPrice);
          const lowY = chart.getY(point.lowPrice);
          const openY = chart.getY(point.openPrice);
          const closeY = chart.getY(point.closePrice);

          const bodyTop = Math.min(openY, closeY);
          const bodyHeight = Math.max(Math.abs(openY - closeY), 2);
          const isUp = point.closePrice >= point.openPrice;

          return (
            <g key={`candle-${point.date}-${index}`}>
              <line
                x1={x}
                y1={highY}
                x2={x}
                y2={lowY}
                className="stock-detail-candle-wick"
              />
              <rect
                x={x - chart.bodyWidth / 2}
                y={bodyTop}
                width={chart.bodyWidth}
                height={bodyHeight}
                className={
                  isUp
                    ? "stock-detail-candle-body-up"
                    : "stock-detail-candle-body-down"
                }
              />
            </g>
          );
        })
      }

      {data.map((point, index) => {
        const today = new Date().toISOString().slice(0, 10);
        const isToday = point.date === today;

        const showLabel =
          index % 7 === 0 || index === 0 && !isToday;

        if (!showLabel) return null;

        return (
          <text
            key={`price-x-${point.date}-${index}`}
            x={chart.getX(index)}
            y={height - 14}
            textAnchor="middle"
            className="stock-detail-axis-label"
          >
            {formatChartDate(point.date)}
          </text>
        );
      })}
    </svg>
  );
}

function MovingAverageChart({ data }) {
  const width = 690;
  const height = 180;
  const chart = useMemo(() => buildMaChart(data, width, height), [data]);

  if (!data.length || !chart) {
    return <div className="stock-detail-chart-empty">移動平均データがありません。</div>;
  }

  return (
    <svg className="stock-detail-chart-svg" viewBox={`0 0 ${width} ${height}`}>
      {chart.yLines.map((line) => (
        <g key={`ma-grid-${line.y}`}>
          <line
            x1={chart.padding.left}
            y1={line.y}
            x2={width - chart.padding.right}
            y2={line.y}
            className="stock-detail-grid-line"
          />
          <text
            x={chart.padding.left - 8}
            y={line.y + 4}
            textAnchor="end"
            className="stock-detail-axis-label"
          >
            {Math.round(line.value)}
          </text>
        </g>
      ))}

      <line
        x1={chart.padding.left}
        y1={height - chart.padding.bottom}
        x2={width - chart.padding.right}
        y2={height - chart.padding.bottom}
        className="stock-detail-axis-line"
      />
      <line
        x1={chart.padding.left}
        y1={chart.padding.top}
        x2={chart.padding.left}
        y2={height - chart.padding.bottom}
        className="stock-detail-axis-line"
      />

      <polyline
        fill="none"
        points={chart.linePoints}
        className="stock-detail-ma-series-solid"
      />

      {data.map((point, index) => {
        const today = new Date().toISOString().slice(0, 10);
        const isToday = point.date === today;

        const showLabel =
          index % 7 === 0 || index === 0 && !isToday;

        if (!showLabel) return null;

        return (
          <text
            key={`ma-x-${point.date}-${index}`}
            x={chart.getX(index)}
            y={height - 14}
            textAnchor="middle"
            className="stock-detail-axis-label"
          >
            {formatChartDate(point.date)}
          </text>
        );
      })}

      <text x={width - 30} y={height / 2} className="stock-detail-ma-legend-text">
        5日
      </text>
    </svg>
  );
}
function StockDetailPage() {
  const { tickerCode } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [data, setData] = useState(null);
  const [message, setMessage] = useState("");
  const [activeTab, setActiveTab] = useState("overview");
  const [chartType, setChartType] = useState("candle");
  const [period, setPeriod] = useState("week");

  const loadDetail = useCallback(async () => {
    try {
      const res = await fetchStockDetail(tickerCode);
      setData(res.data);
      setMessage("");
    } catch (error) {
      setMessage(error.message || "取得に失敗しました。");
    }
  }, [tickerCode]);

  useEffect(() => {
    loadDetail();
  }, [loadDetail]);

  function handleBack() {
    if (location.state?.fromPath) {
      navigate(location.state.fromPath);
      return;
    }
    if (location.state?.from) {
      navigate(location.state.from);
      return;
    }
    navigate(-1);
  }

  if (!data) {
    return (
      <div className="stock-detail-screen">
        <Header />
        <div className="stock-detail-page-body">読み込み中...</div>
      </div>
    );
  }

  const rawChartPoints =
    period === "week" ? data.weekChart || [] : data.monthChart || [];
  const chartPoints = withMovingAverage5(normalizeChartData(rawChartPoints));
  const changeRate = calcChangeRate(
    data.currentPrice,
    data.priceChange,
    data.changeRate
  );
  const diffClass = getDiffClass(data.priceChange);

  return (
    <div className="stock-detail-screen">
      <Header />

      <div className="stock-detail-page-body">
        {message && <div className="page-message">{message}</div>}

        <button
          type="button"
          className="stock-detail-back-button"
          onClick={handleBack}
        >
          ↵戻る
        </button>

        <div className="stock-detail-summary">
          <div className="stock-detail-line">
            <strong>銘柄コード：</strong>
            {data.tickerCode}
          </div>
          <div className="stock-detail-line stock-detail-name-line">
            <strong>銘柄名：</strong>
            {data.stockName}
          </div>
          <div className="stock-detail-line">
            <strong>取引市場：</strong>
            {marketLabel(data.market)}
          </div>
          <div className="stock-detail-line">
            <strong>現在値：</strong>
            {formatPriceWithDollar(data.currentPrice)}
          </div>
          <div className="stock-detail-line">
            <strong>前日比（騰落率）：</strong>
            <span className={diffClass}>
              {Number(data.priceChange) > 0
                ? "+"
                : Number(data.priceChange) < 0
                ? "-"
                : ""}
              {formatPriceWithDollar(Math.abs(Number(data.priceChange || 0)))}
              {changeRate !== null
                ? `（${
                    changeRate > 0 ? "+" : changeRate < 0 ? "-" : ""
                  }${formatPercent(Math.abs(changeRate))}）`
                : ""}
            </span>
          </div>
          <div className="stock-detail-line">
            <strong>データ取得日：</strong>
            {formatDateYYYYMMDD(data.fetchedAt)}
          </div>
        </div>

        <div className="stock-detail-tab-row">
          <button
            type="button"
            className={`stock-detail-tab-button ${
              activeTab === "overview" ? "active" : ""
            }`}
            onClick={() => setActiveTab("overview")}
          >
            概要
          </button>
          <button
            type="button"
            className={`stock-detail-tab-button ${
              activeTab === "chart" ? "active" : ""
            }`}
            onClick={() => setActiveTab("chart")}
          >
            チャート
          </button>
        </div>

        {activeTab === "overview" ? (
          <div className="stock-detail-overview">
            <div className="stock-detail-section-title">四本値（最新）</div>

            <div className="stock-detail-ohlc-grid">
              <div className="stock-detail-ohlc-box">
                始値 {formatPriceWithDollar(data.overview?.openPrice)}
              </div>
              <div className="stock-detail-ohlc-box">
                高値 {formatPriceWithDollar(data.overview?.highPrice)}
              </div>
              <div className="stock-detail-ohlc-box">
                安値 {formatPriceWithDollar(data.overview?.lowPrice)}
              </div>
              <div className="stock-detail-ohlc-box">
                終値 {formatPriceWithDollar(data.overview?.closePrice)}
              </div>
            </div>

            <div className="stock-detail-section-title">指標</div>

            <table className="stock-detail-indicator-table">
              <tbody>
                <tr>
                  <td className="stock-detail-indicator-label">配当利回り</td>
                  <td>{formatPercent(data.overview?.dividendYield)}</td>
                </tr>
                <tr>
                  <td className="stock-detail-indicator-label">PER</td>
                  <td>{data.overview?.per ?? "-"}</td>
                </tr>
                <tr>
                  <td className="stock-detail-indicator-label">PBR</td>
                  <td>{data.overview?.pbr ?? "-"}</td>
                </tr>
                <tr>
                  <td className="stock-detail-indicator-label">ROE</td>
                  <td>{formatPercent(data.overview?.roe)}</td>
                </tr>
                <tr>
                  <td className="stock-detail-indicator-label">出来高</td>
                  <td>{formatNumber(chartPoints[chartPoints.length - 1]?.volume)}</td>
                </tr>
              </tbody>
            </table>
          </div>
        ) : (
          <div className="stock-detail-chart-tab">
            <div className="stock-detail-chart-block">
              <div className="stock-detail-chart-title">株価推移</div>

              <div className="stock-detail-chart-frame">
                <div className="stock-detail-chart-top-buttons">
                  <button
                    type="button"
                    className={`stock-detail-switch-button ${
                      chartType === "candle" ? "active" : ""
                    }`}
                    onClick={() => setChartType("candle")}
                  >
                    ローソク
                  </button>
                  <button
                    type="button"
                    className={`stock-detail-switch-button ${
                      chartType === "line" ? "active" : ""
                    }`}
                    onClick={() => setChartType("line")}
                  >
                    折れ線
                  </button>
                </div>

                <PriceTrendChart data={chartPoints} chartType={chartType} />

                <div className="stock-detail-chart-bottom-buttons">
                  <button
                    type="button"
                    className={`stock-detail-period-button ${
                      period === "week" ? "active" : ""
                    }`}
                    onClick={() => setPeriod("week")}
                  >
                    1週
                  </button>
                  <button
                    type="button"
                    className={`stock-detail-period-button ${
                      period === "month" ? "active" : ""
                    }`}
                    onClick={() => setPeriod("month")}
                  >
                    1か月
                  </button>
                </div>
              </div>
            </div>

            <div className="stock-detail-chart-block stock-detail-ma-block">
              <div className="stock-detail-chart-title">移動平均（5日）</div>

              <div className="stock-detail-ma-frame">
                <MovingAverageChart data={chartPoints} />

                <div className="stock-detail-chart-bottom-buttons">
                  <button
                    type="button"
                    className={`stock-detail-period-button ${
                      period === "week" ? "active" : ""
                    }`}
                    onClick={() => setPeriod("week")}
                  >
                    1週
                  </button>
                  <button
                    type="button"
                    className={`stock-detail-period-button ${
                      period === "month" ? "active" : ""
                    }`}
                    onClick={() => setPeriod("month")}
                  >
                    1か月
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default StockDetailPage;