import { API } from "../config/api";

const authHeaders = (token) => ({
  "Content-Type": "application/json",
  "Authorization": `Bearer ${token}`,
});

export async function adminGetAllProducts(token, page = 1) {
  const res = await fetch(`${API.catalog}?page=${page}`, { headers: authHeaders(token) });
  if (!res.ok) throw new Error("Failed to fetch products");
  return res.json();
}

export async function adminGetProduct(token, code) {
  const res = await fetch(`${API.catalog}/${code}`, { headers: authHeaders(token) });
  if (!res.ok) throw new Error("Failed to fetch product");
  return res.json();
}

export async function adminCreateProduct(token, data) {
  const res = await fetch(`${API.catalog}`, {
    method: "POST",
    headers: authHeaders(token),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to create product");
  return res.json();
}

export async function adminUpdateProduct(token, code, data) {
  const res = await fetch(`${API.catalog}/${code}`, {
    method: "PUT",
    headers: authHeaders(token),
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to update product");
  return res.json();
}

export async function adminDeleteProduct(token, code) {
  const res = await fetch(`${API.catalog}/${code}`, {
    method: "DELETE",
    headers: authHeaders(token),
  });
  if (!res.ok) throw new Error("Failed to delete product");
}

export async function adminUpdatePrice(token, code, newPrice) {
  const res = await fetch(`${API.catalog}/${code}/price?newPrice=${newPrice}`, {
    method: "PATCH",
    headers: authHeaders(token),
  });
  if (!res.ok) throw new Error("Failed to update price");
  return res.json();
}

export async function adminCheckAvailability(token, code) {
  const res = await fetch(`${API.catalog}/${code}/availability`, { headers: authHeaders(token) });
  if (!res.ok) throw new Error("Failed to check availability");
  return res.json();
}
