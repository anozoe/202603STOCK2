import React from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend } from 'recharts';
import '../styles/PieChart.css';





export const SimplePieChart = ({ items = [] }) => {
    const chartData = items.map((stock) => ({
        name: stock.tickerCode,
        value: Number(stock.totalMarketValue ?? 0),
    }));

    if (chartData.length === 0) {
        return (
            <section className="assets-section">
                <h2 className="assets-title">資産配分</h2>
                <div className="chart-container">
                    <p style={{ textAlign: 'center', color: '#666' }}>
                        保有銘柄がありません
                    </p>
                </div>
            </section>
        );
    }

    const getColor = (index, total) => {
    const hue = (index * 360) / total;
    return `hsl(${hue}, 65%, 55%)`;
    };

    return (
        <section className='assets-section'>
            <h2 className='assets-title'>資産配分</h2>
            <div>
                <div className="chart-container">
                    <PieChart width="100%" height={400}>
                        <Pie
                            data={chartData}
                            cx="50%"
                            cy="50%"
                            outerRadius={150}
                            dataKey="value"
                            label={({ name, percent }) =>
                                `${name} ${(percent * 100).toFixed(1)}%`}
                        >
                            {chartData.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={getColor(index, chartData.length)} />
                            ))}
                        </Pie>
                        <Tooltip formatter={(value) => `$${Number(value).toLocaleString()}`} />
                        <Legend />
                    </PieChart>
                </div>
            </div>
        </section>
    );
};

export default PieChart;
