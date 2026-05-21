import React, { useEffect, useMemo, useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import { fetchAssetTotal } from "../api/AssetsHistoryApi";
import "../styles/TotalChart.css";

const formatDate = (iso) => {
  const d = new Date(iso);
  return `${d.getMonth() + 1}/${d.getDate()}`;
};

const formatYen = (v) => "¥" + Number(v).toLocaleString();

const formatYenShort = (v) => {
  if (v >= 1_000_000) return "¥" + (v / 1_000_000).toFixed(1) + "M";
  if (v >= 1_000) return "¥" + Math.round(v / 1_000) + "k";
  return "¥" + v;
};

// 日本株慣習: プラスは赤、マイナスは緑
const pnlClass = (pnl, base) => {
  if (pnl > 0) return `${base}--positive`;
  if (pnl < 0) return `${base}--negative`;
  return "";
};

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload || !payload.length) return null;
  const row = payload[0].payload;
  const pnl = row.unrealizedPnl;
  return (
    <div className="assets-tooltip">
      <div className="assets-tooltip__label">{label}</div>
      <div>{formatYen(row.totalAssets)}</div>
      <div className={pnlClass(pnl, "assets-tooltip__pnl")}>
        {pnl >= 0 ? "+" : ""}
        {formatYen(pnl)}
      </div>
    </div>
  );
};

export default function TotalAssetsChart({ userId = 10 }) {
  const [rawData, setRawData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    fetchAssetTotal(userId)
      .then((data) => {
        if (cancelled) return;
        setRawData(Array.isArray(data) ? data : []);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err?.message || "データ取得に失敗しました");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [userId]);

  const chartData = useMemo(
    () =>
      rawData
        .slice()
        .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
        .map((r) => ({ ...r, date: formatDate(r.createdAt) })),
    [rawData]
  );

  const yDomain = useMemo(() => {
    if (!chartData.length) return [0, 1];
    const vals = chartData.map((d) => d.totalAssets);
    const min = Math.min(...vals);
    const max = Math.max(...vals);
    const pad = (max - min) * 0.1 || 1000;
    return [Math.floor(min - pad), Math.ceil(max + pad)];
  }, [chartData]);

  return (
    <section className="assets-section">
      <h2 className="assets-title">総資産推移</h2>

      {loading && <div className="assets-state">読み込み中…</div>}
      {error && (
        <div className="assets-state assets-state--error">エラー: {error}</div>
      )}
      {!loading && !error && !chartData.length && (
        <div className="assets-state">データがありません</div>
      )}

      {!loading && !error && chartData.length > 0 && (
        <>
          <div className="assets-chart">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={chartData}
                margin={{ top: 8, right: 16, left: 8, bottom: 8 }}
              >
                <CartesianGrid
                  strokeDasharray="3 3"
                  stroke="#eee"
                  vertical={false}
                />
                <XAxis
                  dataKey="date"
                  tick={{ fontSize: 11, fill: "#666" }}
                  axisLine={{ stroke: "#d0d0d0" }}
                  tickLine={false}
                />
                <YAxis
                  domain={yDomain}
                  tickFormatter={formatYenShort}
                  tick={{ fontSize: 11, fill: "#666" }}
                  axisLine={false}
                  tickLine={false}
                  width={60}
                />
                <Tooltip content={<CustomTooltip />} />
                <Line
                  type="linear"
                  dataKey="totalAssets"
                  stroke="#378ADD"
                  strokeWidth={2}
                  dot={{ r: 3, fill: "#378ADD", strokeWidth: 0 }}
                  activeDot={{ r: 5 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>

          <StatsRow chartData={chartData} />
        </>
      )}
    </section>
  );
}

function StatsRow({ chartData }) {
  const last = chartData[chartData.length - 1];
  const max = Math.max(...chartData.map((d) => d.totalAssets));
  const min = Math.min(...chartData.map((d) => d.totalAssets));

  return (
    <div className="assets-stats">
      <Stat label="期間" value={`${chartData[0].date} - ${last.date}`} />
      <Stat label="最大資産" value={formatYen(max)} />
      <Stat label="最小資産" value={formatYen(min)} />
      <Stat
        label="最終損益"
        value={`${last.unrealizedPnl >= 0 ? "+" : ""}${formatYen(
          last.unrealizedPnl
        )}`}
        sign={last.unrealizedPnl}
      />
    </div>
  );
}

function Stat({ label, value, sign }) {
  const valueClass =
    sign === undefined
      ? "assets-stat__value"
      : `assets-stat__value ${pnlClass(sign, "assets-stat__value")}`;
  return (
    <div className="assets-stat">
      <div className="assets-stat__label">{label}</div>
      <div className={valueClass}>{value}</div>
    </div>
  );
}