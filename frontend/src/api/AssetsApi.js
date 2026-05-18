import { fetchJson } from "./LoginRegisterApi"


const BASE_URL = "http://localhost:8080/api/users/me"

export async function fetchAssetTotal(userId) {
    return fetchJson(`${BASE_URL}/assets?userId=${userId}`)
}

export async function fetchAssetsStock(userId) {
    return fetchJson(`${BASE_URL}/stock?userId=${userId}`)
}

export async function fetchStockAmount(userId, tickerCode) {
    return fetchJson(`${BASE_URL}/amount?userId=${userId}&tickerCode=${tickerCode}`)
}