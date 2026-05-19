import { fetchJson } from "./LoginRegisterApi"

const BASE_URL = "http://localhost:8080";
export const orderRegisterApi = async (orderData) => {
    return fetchJson(`${BASE_URL}/api/orders`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(orderData),
    });
};