import React, { useState, useCallback, useEffect } from 'react';
import { fetchMarketsDetail } from '../api/MarketsApi';
import '../styles/StockPrice.css';

function getDiffClass(value) {
  const num = Number(value);
  if (Number.isNaN(num)) return "";
  if (num > 0) return "stock-detail-plus";
  if (num < 0) return "stock-detail-minus";
  return "";
}

function StockPrice({ tickerCode }) {
  const [data, setData] = useState(null);
  const [message, setMessage] = useState('');

  const loadDetail = useCallback(async () => {
    if (!tickerCode) return;
    try {
      const res = await fetchMarketsDetail(tickerCode);
      setData(res);
      setMessage('');
    } catch (error) {
      setMessage(error.message || '取得に失敗しました。');
    }
  }, [tickerCode]);

  useEffect(() => {
    loadDetail();
  }, [loadDetail]);

  if (message) return <div className="stock-detail-error">{message}</div>;
  if (!data) return <div className="stock-detail-loading">読み込み中...</div>;

  const diffClass = getDiffClass(data.priceChange)

  return (
    <div className="stock-detail-summary">
      <div className="stock-detail-line">
        <span className="label">銘柄コード：</span>
        <span className="value">{data.tickerCode}</span>
      </div>
      <div className="stock-detail-line stock-detail-name-line">
        <span className="label">銘柄名：</span>
        <span className="value">{data.stockName}</span>
      </div>
      <div className="stock-detail-line">
        <span className="label">取引市場：</span>
        <span className="value">{data.market}</span>
      </div>
      <div className="stock-detail-line">
        <span className="label">現在値：</span>
        <span className="value">{data.currentPrice}</span>
      </div>
      <div className="stock-detail-line">
        <span className="label">前日比（騰落率）：</span>
        <span className={`value ${diffClass}`}>
          {data.priceChange > 0 ? "+$" : data.priceChange < 0 ? "-$" :""}{Math.abs(data.priceChange)}
          {data.changeRate ? `（${
                    data.changeRate > 0 ? "+" : data.changeRate < 0 ? "-" : ""
                  }${Math.abs(data.changeRate)}%）` : ''}
        </span>
      </div>
      <div className="stock-detail-line">
        <span className="label">データ取得日：</span>
        <span className="value">{data.updatedAt}</span>
      </div>
    </div>
  );
}

export default StockPrice;