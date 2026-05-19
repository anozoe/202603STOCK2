import { useState } from "react";
import { OrderContext } from "../OrderContext";

export const OrderProvider = ({ children }) => {
    const [order, setOrder] = useState({
        stockId: '',
        tickerCode: '',
        stockName: '',
        market: '',
        orderSide: 'buy',      // 'buy' | 'sell'
        orderMethod: 1,        // 1=成行, 2=指値
        quantity: 0,
        limitPrice: 0,
        currentPrice: 0,
    });

    return (
        <OrderContext.Provider value={{setOrder}}>
            {children}
        </OrderContext.Provider>
    );
}