import { API } from "../config/api";

export async function searchProducts({ query, genre, author, minPrice, maxPrice, page = 1 } = {}) {
  const params = new URLSearchParams({ page: String(page) });

  if (query) params.set("query", query);
  if (genre) params.set("genre", genre);
  if (author) params.set("author", author);
  if (minPrice != null) params.set("minPrice", String(minPrice));
  if (maxPrice != null) params.set("maxPrice", String(maxPrice));

  const res = await fetch(`${API.catalog}/search?${params.toString()}`);
  if (!res.ok) throw new Error("Failed to search products");
  return res.json();
}


export async function getProductByCode(code) {
  const res = await fetch(`${API.catalog}/${code}`);
  if (!res.ok) throw new Error("Failed to fetch product");
  return res.json();
}
