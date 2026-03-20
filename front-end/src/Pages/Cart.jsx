import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

function QuantityControl({ item, onUpdate, onRemove }) {
  const [qty, setQty] = useState(item.quantity);
  const [busy, setBusy] = useState(false);

  async function change(newQty) {
    if (newQty < 1) return;
    setBusy(true);
    setQty(newQty);
    await onUpdate(item.id, newQty);
    setBusy(false);
  }

  return (
    <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
      <button
        onClick={() => change(qty - 1)}
        disabled={busy || qty <= 1}
        style={{
          width: 32,
          height: 32,
          borderRadius: 999,
          border: "1px solid #d1d5db",
          background: "white",
          fontWeight: 900,
          cursor: "pointer",
          fontSize: "1rem",
        }}
      >
        −
      </button>
      <span style={{ minWidth: 24, textAlign: "center", fontWeight: 700 }}>
        {qty}
      </span>
      <button
        onClick={() => change(qty + 1)}
        disabled={busy}
        style={{
          width: 32,
          height: 32,
          borderRadius: 999,
          border: "1px solid #d1d5db",
          background: "white",
          fontWeight: 900,
          cursor: "pointer",
          fontSize: "1rem",
        }}
      >
        +
      </button>
      <button
        onClick={() => onRemove(item.id)}
        disabled={busy}
        style={{
          marginLeft: 8,
          background: "none",
          border: "none",
          color: "#ef4444",
          cursor: "pointer",
          fontWeight: 700,
          fontSize: "0.85rem",
        }}
      >
        Remove
      </button>
    </div>
  );
}

export default function Cart() {
  const { user, login } = useAuth();
  const { cart, loading, error, fetchCart, updateItem, removeItem, emptyCart } =
    useCart();
  const navigate = useNavigate();

  useEffect(() => {
    if (user) fetchCart();
  }, [user]);

  if (!user) {
    return (
      <div className="page-shell">
        <div className="container">
          <div
            style={{
              maxWidth: 480,
              margin: "60px auto",
              textAlign: "center",
              padding: 40,
              background: "white",
              borderRadius: 24,
              border: "1px solid #e5e7eb",
            }}
          >
            <div style={{ fontSize: "3rem", marginBottom: 16 }}>🔒</div>
            <h2 style={{ margin: "0 0 10px" }}>Sign in to view your cart</h2>
            <p style={{ color: "#6b7280", marginBottom: 24 }}>
              Your cart is saved to your account. Log in to see what's in there.
            </p>
            <button className="primary-btn" onClick={login}>
              Login to continue
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (loading && !cart) {
    return (
      <div className="page-shell">
        <div className="container">
          <div
            style={{
              padding: 60,
              textAlign: "center",
              color: "#6b7280",
              fontWeight: 700,
            }}
          >
            Loading your cart…
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-shell">
        <div className="container">
          <div
            style={{
              padding: 32,
              background: "#fef2f2",
              borderRadius: 18,
              color: "#ef4444",
              textAlign: "center",
            }}
          >
            ⚠️ {error}
            <br />
            <button
              className="ghost-btn small-btn"
              style={{ marginTop: 16 }}
              onClick={fetchCart}
            >
              Try again
            </button>
          </div>
        </div>
      </div>
    );
  }

  const items = cart?.items || [];
  const isEmpty = items.length === 0;

  if (isEmpty) {
    return (
      <div className="page-shell">
        <div className="container">
          <div
            style={{
              maxWidth: 480,
              margin: "60px auto",
              textAlign: "center",
              padding: 40,
              background: "white",
              borderRadius: 24,
              border: "1px solid #e5e7eb",
            }}
          >
            <div style={{ fontSize: "3rem", marginBottom: 16 }}>🛒</div>
            <h2 style={{ margin: "0 0 10px" }}>Your cart is empty</h2>
            <p style={{ color: "#6b7280", marginBottom: 24 }}>
              Looks like you haven't added anything yet.
            </p>
            <Link to="/" className="primary-btn">
              Browse Books
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-shell">
      <div className="container">
        <div style={{ marginBottom: 28 }}>
          <p style={{ color: "#4f46e5", fontWeight: 800, margin: "0 0 4px" }}>
            Your Cart
          </p>
          <h1 style={{ margin: 0, fontSize: "2rem", letterSpacing: "-0.03em" }}>
            {cart.totalItems} {cart.totalItems === 1 ? "item" : "items"}
          </h1>
        </div>

        <div
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 340px",
            gap: 28,
            alignItems: "start",
          }}
        >
          {/* Items list */}
          <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            {items.map((item) => (
              <div
                key={item.id}
                style={{
                  background: "white",
                  border: "1px solid #e5e7eb",
                  borderRadius: 20,
                  padding: 20,
                  display: "flex",
                  gap: 18,
                  alignItems: "center",
                }}
              >
                {/* Book placeholder image */}
                {item.imageUrl ? (
                  <img
                    src={item.imageUrl}
                    alt={item.productName}
                    style={{
                      width: 64,
                      height: 80,
                      borderRadius: 10,
                      objectFit: "cover",
                      flexShrink: 0,
                    }}
                  />
                ) : (
                  <div
                    style={{
                      width: 64,
                      height: 80,
                      borderRadius: 10,
                      background: "linear-gradient(135deg, #e0e7ff, #ede9fe)",
                      display: "grid",
                      placeItems: "center",
                      fontSize: "1.6rem",
                      flexShrink: 0,
                    }}
                  >
                    📖
                  </div>
                )}

                <div style={{ flex: 1, minWidth: 0 }}>
                  <h3
                    style={{
                      margin: "0 0 4px",
                      fontSize: "1rem",
                      fontWeight: 800,
                      color: "#111827",
                    }}
                  >
                    {item.productName}
                  </h3>
                  <p
                    style={{
                      margin: "0 0 12px",
                      color: "#6b7280",
                      fontSize: "0.85rem",
                    }}
                  >
                    Code: {item.productCode}
                  </p>
                  <QuantityControl
                    item={item}
                    onUpdate={updateItem}
                    onRemove={removeItem}
                  />
                </div>

                <div style={{ textAlign: "right", flexShrink: 0 }}>
                  <div
                    style={{
                      fontWeight: 900,
                      fontSize: "1.1rem",
                      color: "#111827",
                    }}
                  >
                    £{Number(item.subTotal).toFixed(2)}
                  </div>
                  <div
                    style={{
                      color: "#9ca3af",
                      fontSize: "0.8rem",
                      marginTop: 2,
                    }}
                  >
                    £{Number(item.unitPrice).toFixed(2)} each
                  </div>
                </div>
              </div>
            ))}

            <button
              className="ghost-btn small-btn"
              style={{
                alignSelf: "flex-start",
                color: "#ef4444",
                borderColor: "#fecaca",
              }}
              onClick={emptyCart}
            >
              🗑 Clear entire cart
            </button>
          </div>

          {/* Order summary */}
          <div
            style={{
              background: "white",
              border: "1px solid #e5e7eb",
              borderRadius: 24,
              padding: 24,
              position: "sticky",
              top: 100,
            }}
          >
            <h3
              style={{
                margin: "0 0 20px",
                fontSize: "1.1rem",
                fontWeight: 800,
              }}
            >
              Order Summary
            </h3>

            <div
              style={{
                display: "flex",
                flexDirection: "column",
                gap: 12,
                marginBottom: 20,
              }}
            >
              {items.map((item) => (
                <div
                  key={item.id}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    fontSize: "0.9rem",
                    color: "#374151",
                  }}
                >
                  <span
                    style={{
                      maxWidth: 180,
                      overflow: "hidden",
                      textOverflow: "ellipsis",
                      whiteSpace: "nowrap",
                    }}
                  >
                    {item.productName} × {item.quantity}
                  </span>
                  <span style={{ fontWeight: 700 }}>
                    £{Number(item.subTotal).toFixed(2)}
                  </span>
                </div>
              ))}
            </div>

            <div
              style={{
                borderTop: "2px solid #f3f4f6",
                paddingTop: 16,
                marginBottom: 20,
              }}
            >
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <span style={{ fontWeight: 800, fontSize: "1rem" }}>Total</span>
                <span
                  style={{
                    fontWeight: 900,
                    fontSize: "1.4rem",
                    color: "#0f172a",
                  }}
                >
                  £{Number(cart.totalPrice).toFixed(2)}
                </span>
              </div>
            </div>

            <button
              className="primary-btn"
              style={{ width: "100%", padding: "14px", fontSize: "1rem" }}
              onClick={() => navigate("/checkout")}
            >
              Proceed to Checkout →
            </button>

            <Link
              to="/"
              style={{
                display: "block",
                textAlign: "center",
                marginTop: 14,
                color: "#6b7280",
                fontSize: "0.9rem",
              }}
            >
              ← Continue shopping
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
