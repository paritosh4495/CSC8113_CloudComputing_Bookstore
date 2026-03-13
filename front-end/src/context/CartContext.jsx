import { createContext, useContext, useState, useCallback, useEffect } from "react";
import { useAuth } from "./AuthContext";
import {
  getCart,
  addItemToCart,
  updateCartItem,
  deleteCartItem,
  clearCart,
} from "../Services/cartService";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { user, keycloak, login } = useAuth();

  const [cart,    setCart]    = useState(null);
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);

  async function getToken() {
    try {
      await keycloak.updateToken(30);
    } catch {
      login();
      throw new Error("Session expired — please log in again");
    }
    return keycloak.token;
  }

  const fetchCart = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError(null);
    try {
      const token = await getToken();
      const data  = await getCart(token);
      setCart(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [user]);

  // ← Auto-fetch on login and page refresh
  useEffect(() => {
    if (user) {
      fetchCart();
    } else {
      setCart(null);
    }
  }, [user]);

  async function addItem(book, quantity = 1) {
    if (!user) { login(); return; }
    try {
      const token = await getToken();
      const data  = await addItemToCart(token, book.code, quantity);
      setCart(data);
    } catch (err) {
      setError(err.message);
    }
  }

 async function updateItem(itemId, quantity) {
  if (!user) return;
  // Update only the specific item locally — never replace whole cart
  setCart((prev) => {
    if (!prev) return prev;
    const updatedItems = prev.items.map((i) =>
      i.id === itemId
        ? { ...i, quantity, subTotal: i.unitPrice * quantity }
        : i
    );
    const totalPrice = updatedItems.reduce((sum, i) => sum + i.subTotal, 0);
    const totalItems = updatedItems.reduce((sum, i) => sum + i.quantity, 0);
    return { ...prev, items: updatedItems, totalPrice, totalItems };
  });

  // Fire API in background — no setCart on response
  try {
    const token = await getToken();
    await updateCartItem(token, itemId, quantity);
  } catch (err) {
    setError(err.message);
    fetchCart(); // only re-fetch to recover on error
  }
}


  async function removeItem(itemId) {
    if (!user) return;
    // Optimistic update for snappy UI
    setCart((prev) => {
      if (!prev) return prev;
      const updated = prev.items.filter((i) => i.id !== itemId);
      const totalPrice = updated.reduce((sum, i) => sum + i.subTotal, 0);
      return { ...prev, items: updated, totalItems: updated.length, totalPrice };
    });
    try {
      const token = await getToken();
      await deleteCartItem(token, itemId);
      fetchCart(); // sync with backend after delete
    } catch (err) {
      setError(err.message);
      fetchCart(); // recover on error
    }
  }

  async function emptyCart() {
    if (!user) return;
    try {
      const token = await getToken();
      await clearCart(token);
      setCart((prev) => prev ? { ...prev, items: [], totalItems: 0, totalPrice: 0 } : prev);
    } catch (err) {
      setError(err.message);
    }
  }

  const count = cart?.totalItems ?? 0;

  return (
    <CartContext.Provider value={{
      cart,
      count,
      loading,
      error,
      fetchCart,
      addItem,
      updateItem,
      removeItem,
      emptyCart,
    }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used inside CartProvider");
  return ctx;
}
