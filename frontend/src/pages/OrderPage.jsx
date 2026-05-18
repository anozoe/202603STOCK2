import React, { use, useCallback, useEffect, useState } from 'react'
import Header from '../components/Header'
import { IgrTabs, IgrTab } from "igniteui-react";
import "../styles/OrderPage.css"
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import StockPrice from '../components/StockPrice';
import { fetchMarketsDetail } from '../api/MarketsApi';
import { getLoginUserId } from '../utils/authHeader';
import { fetchAssetsStock, fetchAssetTotal, fetchStockAmount } from '../api/AssetsApi';


function OrderPage() {
    const { tickerCode } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const [data, setData] = useState(null);
    const [assetsTotalData, setAssetsTotalData] = useState(null);
    const [assetsStockData, setAssetsStockData] = useState(null);
    const [stockAmountData, setStockAmountData] = useState(null);
    const [message, setMessage] = useState('');

    const userId = getLoginUserId();

    const orderMethod = [
        { id: 1, message:"成行"},
        { id: 2, message:"指値"}
    ];

    const [radioButtonNumber, setRadioButtonNumber] = useState(1);
    const radioButtonChanged = (e) => {
        console.log(e.target.value);
        setRadioButtonNumber(Number(e.target.value));
    };
    const buttonClicked = () => {
        const selected = orderMethod.find(v => v.id === radioButtonNumber);
        alert(`${[selected.message]}`);
    }

    const [quantity, setQuantity] = useState(0);
    const [price, setPrice] = useState(0);
    const [activeTab, setActiveTab] = useState('buy');
    

    useEffect(() => {
        const loadDetail = async () => {
            if (!tickerCode) return;
            try {
                const res = await fetchMarketsDetail(tickerCode);
                setData(res);
                setMessage('');
            } catch (error) {
                setMessage(error.message || '取得に失敗しました。');
            }  
        };
        loadDetail();
    }, []);

    useEffect(() =>{
        const loadAssetsTotal = async () => {
            if (!userId) return;
            try{
                const res = await fetchAssetTotal(userId);
                console.log("-----------------");
                console.log(res);
                setAssetsTotalData(res);
                setMessage('')
            } catch (error) {
                setMessage(error.message || '取得に失敗しました。');
            }
    
        };
        loadAssetsTotal();
    }, []);

    useEffect(() => {
        const loadStockAmount = async () => {
            console.log("保有数量取得:", userId, tickerCode)
            if (!userId || !tickerCode) return;
            try {
                const res = await fetchStockAmount(userId, tickerCode);
                console.log(res);
                setStockAmountData(res);
            } catch (error){
                setMessage(error.message || '取得に失敗しました。')
            }
            
        };
        loadStockAmount();
    }, []);


    const currentPrice = data?.currentPrice ?? 0;
    const buyingPower = assetsTotalData?.buyingPower ?? 0;
    const estimatedAmount = quantity * (radioButtonNumber === 1 ? currentPrice : price);
    const buyingPowerAfterOrder = (activeTab === 'buy' ? buyingPower - estimatedAmount : buyingPower + estimatedAmount);
    const holdingAmount = stockAmountData?.sumHoldingAmount ?? 0;
    const isSellDisabled = holdingAmount <= 0;

  return (
    <div>
        <Header />
        <button> {/* TODO: 戻るボタンの遷移先作成 */}
         ↵戻る  

        </button>
        <div className='upper-object'>
            <StockPrice tickerCode={tickerCode}/>
        </div>
        <div className='under-object'>
            <div className='order-tab'>
                <div className='tab-header'>
                    <button 
                        className={`tab-button ${activeTab === 'buy' ? 'active' : ''}`}
                        onClick={() => setActiveTab('buy')}
                    >
                        買い注文
                    </button>
                    <button 
                        className={`tab-button ${activeTab === 'sell' ? 'active' : ''}`}
                        onClick={() => setActiveTab('sell')}
                        disabled={isSellDisabled}
                    >
                        売り注文
                    </button>
                </div>
                <div className='tab-content'>
                    {activeTab === 'buy' && (
                        <div>
                            <div className='buyingPowerNow'>
                                <div>買付可能額</div>
                                <div>${buyingPower}</div>
                            </div>
                            <div className='form-section'>
                                <div className='section-label'>数量</div>
                                <label className='value-label'>
                                    <input 
                                        type='number'
                                        min={1}
                                        max={10000}
                                        name='myInput' 
                                        value={quantity}
                                        onChange={(e) => setQuantity(Number(e.target.value))}
                                    />
                                    株
                                </label>
                                <div className='help-text'>1~10,000株で入力してください。</div>

                            </div>
                            <div className='form-section'>
                                <div className='section-label'>注文方法</div>
                                <div className='radio-group'>
                                    {orderMethod.map((val) => (
                                        <label key={val.id} className='radio-label'>
                                            <input 
                                                type="radio"
                                                name="name1"
                                                value={val.id}
                                                checked={radioButtonNumber === val.id}
                                                onChange={radioButtonChanged}
                                            />
                                            {val.message}
                                        </label>
                                    ))}
                                </div>
                                <div className='price-label'>
                                    <span>$</span>
                                    <input 
                                        type='number'
                                        value={radioButtonNumber === 1 ? "" : price}
                                        onChange={(e) => setPrice(Number(e.target.value))}
                                        disabled={radioButtonNumber === 1}
                                        placeholder={radioButtonNumber === 1 ? '-------' : ''}
                                    />
                                </div>
                                <div className="help-text">成行の場合、現在値で即時約定します。</div>
                            </div>
                            <div className='form-section'>
                                <div>執行条件</div>
                                <div>本日中のみ（17:00まで有効）</div>
                            </div>
                            <div className='summary'>
                                <div className='estimatedAmount'>
                                    <div>概算約定金額</div>
                                    <div>${estimatedAmount.toFixed(2)}</div>
                                </div>
                                <div className='buyingPowerAfterOrder'>
                                    <div>注文後の買付可能額</div>
                                    <div>${buyingPowerAfterOrder<0 ? 0 : buyingPowerAfterOrder}</div>
                                </div>
                            </div>
                            <button className='check-btn'>確認画面へ</button>
                        </div>
                    )}
                    {activeTab === 'sell' && (
                        <div>
                            <div className='buyingPowerNow'>
                                <div>保有数量</div>
                                <div>{holdingAmount}株</div>
                            </div>
                            <div className='form-section'>
                                <div className='section-label'>数量</div>
                                <label className='value-label'>
                                    <input 
                                        type='number'
                                        min={1}
                                        max={holdingAmount}
                                        name='myInput' 
                                        value={quantity}
                                        onChange={(e) => setQuantity(Number(e.target.value))}
                                    />
                                    株
                                </label>
                                <div className='help-text'>1~10,000株で入力してください。</div>

                            </div>
                            <div className='form-section'>
                                <div className='section-label'>注文方法</div>
                                <div className='radio-group'>
                                    {orderMethod.map((val) => (
                                        <label key={val.id} className='radio-label'>
                                            <input 
                                                type="radio"
                                                name="name1"
                                                value={val.id}
                                                checked={radioButtonNumber === val.id}
                                                onChange={radioButtonChanged}
                                            />
                                            {val.message}
                                        </label>
                                    ))}
                                </div>
                                <div className='price-label'>
                                    <span>$</span>
                                    <input 
                                        type='number'
                                        value={radioButtonNumber === 1 ? "" : price}
                                        onChange={(e) => setPrice(Number(e.target.value))}
                                        disabled={radioButtonNumber === 1}
                                        placeholder={radioButtonNumber === 1 ? '-------' : ''}
                                    />
                                </div>
                                <div className="help-text">成行の場合、現在値で即時約定します。</div>
                            </div>
                            <div className='form-section'>
                                <div>執行条件</div>
                                <div>本日中のみ（17:00まで有効）</div>
                            </div>
                            <div className='summary'>
                                <div className='estimatedAmount'>
                                    <div>概算約定金額</div>
                                    <div>${estimatedAmount.toFixed(2)}</div>
                                </div>
                                <div className='buyingPowerAfterOrder'>
                                    <div>注文後の買付可能額</div>
                                    <div>${buyingPowerAfterOrder<0 ? 0 : buyingPowerAfterOrder}</div>
                                </div>
                            </div>
                            <button className='check-btn'>確認画面へ</button>
                        </div>
                    )}
                </div>                   
            </div>
            <div className='orderBook'>板情報コンポーネント</div>
        </div>
    </div>

  )
}

export default OrderPage