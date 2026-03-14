const base = window.ENV?.API_BASE || "http://localhost:9000";

export const API = {
  catalog: `${base}/catalog`,
  cart:    `${base}/cart`,
};
