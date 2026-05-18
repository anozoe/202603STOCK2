import { fetchJson } from "./LoginRegisterApi";

export async function fetchMarketsDetail(tickerCode) {
    return fetchJson(`http://localhost:8080/api/markets/${tickerCode}`);
}