import { fetchJson } from "./LoginRegisterApi"


const BASE_URL = "http://localhost:8080/api/total/history"

export async function fetchAssetTotal(userId) {
    return fetchJson(`${BASE_URL}?userId=${userId}`)
}