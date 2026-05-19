import React, { useContext, useState } from 'react'
import Header from '../components/Header'
import { OrderContext } from '../OrderContext'
import '../styles/OrderCheckPage.css'
import { useNavigate } from 'react-router-dom';
import { orderRegisterApi } from '../api/OrderApi';
import { getLoginUserId } from '../utils/authHeader';

function OrderCheckPage() {
    const { order } = useContext(OrderContext);
    const navigate = useNavigate();
    const [error, setError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const userId = getLoginUserId();

    const handleBackOrder = () => {
        navigate(`/order/${order.tickerCode}`);
    };

    const handleSubmit = async () => {
        console.log('order from context:', order); 
        setIsSubmitting(true);
        setError(null);
        const orderData ={
            stockId: order.stockId,
            userId: userId, 
            tickerCode: order.tickerCode,
            orderQuantity: order.quantity,
            orderSide: order.orderSide == 'buy' ? 1 : 2,
            orderMethod: order.orderMethod,
            limitPrice: order.orderMethod == 1 ? order.currentPrice : order.limitPrice,
            holdingAmount: order.holdingAmount,
            buyingPower: order.buyingPower,
            currentPrice: order.currentPrice,
        };
        console.log('orderData to send:', orderData);  
        try {
            const response = await orderRegisterApi(orderData);
            navigate(`/mypage`, { state: { response } });
        } catch (e) {
            setError('注文の送信に失敗しました。');
            setIsSubmitting(false);
        }
    };

  return (
    <div className='order-check-page'>
        <Header />
        <span></span>

        <div className='order-card'>
            <div className='order-card-header'>
                注文確認
            </div>

            <div className='stock-info'>
                <div>{order.tickerCode}</div>
                <div>{order.stockName}</div>
                <div>{order.market}</div>
            </div>

            <div className='order-details'>
                <div className='detail-row'>
                    <div className='detail-label'>種別</div>
                    <div className='detail-value'>{order.orderSide === 'buy' ? '買い' : '売り'}</div>
                </div>
                <div className='detail-row'>
                    <div className='detail-label'>注文方法</div>
                    <div className='detail-value'>{order.orderMethod === 1 ? '成行' : '指値'}</div>
                </div>
                <div className='detail-row'>
                    <div className='detail-label'>数量</div>
                    <div className='detail-value'>{order.quantity}株</div>
                </div>
                <div className='detail-row'>
                    <div className='detail-label'>価格</div>
                    <div className='detail-value'>${order.orderMethod == 1 ? order.currentPrice : order.limitPrice}</div>
                </div> 
                <div className='detail-row detail-row-last'>
                    <div className='detail-label'>執行条件</div>
                    <div className='detail-value detail-value-bold'>本日中（17:00まで）</div>
                </div>
            </div>

            <div className='notice-box'>
                <div>注文を確定すると取り消しができません。</div>
                <div>内容をご確認のうえ、「注文を確定する」を押してください。</div>
            </div>

            <div className='button-container'>
                <button 
                    className='action-button'
                    onClick={handleBackOrder}
                    disabled={isSubmitting}
                >
                    戻る
                </button>
                <button 
                    className='action-button'
                    onClick={handleSubmit}
                    disabled={isSubmitting}
                >
                    注文を確定する
                </button>
            </div>
        </div>
    </div>
  )
}

export default OrderCheckPage