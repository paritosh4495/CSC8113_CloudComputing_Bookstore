import { API } from "../config/api";

// All cart endpoints require the Bearer token
const headers = (token) => ({
  "Content-Type": "application/json",
  "Authorization": `Bearer ${token}`,
});

export async function getCart(token) {
  const res = await fetch(`${API.cart}`, {
    headers: headers(token),
  });
  if (!res.ok) throw new Error("Failed to fetch cart");
  return res.json();
}

export async function addItemToCart(token, productCode, quantity = 1) {
  const res = await fetch(`${API.cart}/items`, {
    method: "POST",
    headers: headers(token),
    body: JSON.stringify({ productCode, quantity }),
  });
  if (!res.ok) throw new Error("Failed to add item to cart");
  return res.json();
}

export async function updateCartItem(token, itemId, quantity) {
  const res = await fetch(`${API.cart}/items/${itemId}`, {
    method: "PUT",
    headers: headers(token),
    body: JSON.stringify({ quantity }),
  });
  if (!res.ok) throw new Error("Failed to update item");
  return res.json();
}

export async function deleteCartItem(token, itemId) {
  const res = await fetch(`${API.cart}/items/${itemId}`, {
    method: "DELETE",
    headers: headers(token),
  });
  if (!res.ok) throw new Error("Failed to remove item");
}

export async function clearCart(token) {
  const res = await fetch(`${API.cart}`, {
    method: "DELETE",
    headers: headers(token),
  });
  if (!res.ok) throw new Error("Failed to clear cart");
}
