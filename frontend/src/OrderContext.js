import { createContext, useState } from "react";


export const OrderContext = createContext();

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
        buyingPower: 0,
        holdingAmount: 0,
    });

    return (
        <OrderContext.Provider value={{order, setOrder}}>
            {children}
        </OrderContext.Provider>
    )
};