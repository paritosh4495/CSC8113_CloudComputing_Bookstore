import { useState } from "react";
import { Link } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import { API } from "../config/api";

const STEPS = ["Delivery", "Payment", "Confirm"];

const inputStyle = {
  padding: "11px 14px",
  borderRadius: 12,
  border: "1px solid #c8d8e8",
  outline: "none",
  fontSize: "0.9rem",
  background: "white",
  width: "100%",
};

const labelStyle = {
  fontWeight: 700,
  fontSize: "0.82rem",
  color: "#374151",
  marginBottom: 6,
  display: "block",
};

function StepIndicator({ step }) {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 0, marginBottom: 40 }}>
      {STEPS.map((label, i) => {
        const done    = i < step;
        const current = i === step;
        return (
          <div key={label} style={{ display: "flex", alignItems: "center" }}>
            <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 6 }}>
              <div style={{
                width: 36, height: 36, borderRadius: 999,
                display: "grid", placeItems: "center",
                fontWeight: 800, fontSize: "0.85rem",
                background: done ? "#003a65" : current ? "#0073bc" : "#e5e7eb",
                color: done || current ? "white" : "#9ca3af",
                transition: "0.2s",
              }}>
                {done ? "✓" : i + 1}
              </div>
              <span style={{ fontSize: "0.78rem", fontWeight: 700, color: current || done ? "#003a65" : "#9ca3af" }}>
                {label}
              </span>
            </div>
            {i < STEPS.length - 1 && (
              <div style={{ width: 80, height: 2, background: done ? "#003a65" : "#e5e7eb", margin: "0 8px", marginBottom: 22, transition: "0.2s" }} />
            )}
          </div>
        );
      })}
    </div>
  );
}

function OrderSummary({ cart }) {
  const items = cart?.items || [];
  return (
    <div style={{ background: "white", border: "1px solid #c8d8e8", borderRadius: 20, padding: 22, position: "sticky", top: 100 }}>
      <h3 style={{ margin: "0 0 18px", fontSize: "1rem", fontWeight: 800, color: "#051435" }}>
        Order Summary
      </h3>
      <div style={{ display: "flex", flexDirection: "column", gap: 10, marginBottom: 16 }}>
        {items.map((item) => (
          <div key={item.id} style={{ display: "flex", justifyContent: "space-between", gap: 12, fontSize: "0.88rem" }}>
            <span style={{ color: "#374151", flex: 1, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              {item.productName} <span style={{ color: "#9ca3af" }}>×{item.quantity}</span>
            </span>
            <span style={{ fontWeight: 700, color: "#051435", flexShrink: 0 }}>
              £{Number(item.subTotal).toFixed(2)}
            </span>
          </div>
        ))}
      </div>
      <div style={{ borderTop: "2px solid #f0f4f8", paddingTop: 14 }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span style={{ fontWeight: 800 }}>Total</span>
          <span style={{ fontWeight: 900, fontSize: "1.3rem", color: "#003a65" }}>
            £{Number(cart?.totalPrice || 0).toFixed(2)}
          </span>
        </div>
      </div>
    </div>
  );
}

export default function Checkout() {
  const { user, login, keycloak } = useAuth();
  const { cart, emptyCart }       = useCart();

  const [step,       setStep]       = useState(0);
  const [placing,    setPlacing]    = useState(false);
  const [placeError, setPlaceError] = useState(null);
  const [orderDone,  setOrderDone]  = useState(null);

  const [delivery, setDelivery] = useState({
    fullName: user?.name  || "",
    email:    user?.email || "",
    address:  "",
    city:     "",
    postcode: "",
    country:  "United Kingdom",
  });

  // ← All useState calls are inside the component now
  const [payment, setPayment] = useState({
    holderName: "",
    digits:     "",
    validUntil: "",
    pin:        "",
  });

  // ── Not logged in ─────────────────────────────────────────────────────────
  if (!user) {
    return (
      <div className="page-shell">
        <div className="container">
          <div style={{ maxWidth: 480, margin: "60px auto", textAlign: "center", padding: 40, background: "white", borderRadius: 24, border: "1px solid #c8d8e8" }}>
            <div style={{ fontSize: "3rem", marginBottom: 16 }}>🔒</div>
            <h2 style={{ margin: "0 0 10px" }}>Sign in to checkout</h2>
            <p style={{ color: "#6b7280", marginBottom: 24 }}>You need to be logged in to place an order.</p>
            <button className="primary-btn" onClick={login}>Login to continue</button>
          </div>
        </div>
      </div>
    );
  }

  const items = cart?.items || [];

  // ── Empty cart ────────────────────────────────────────────────────────────
  if (items.length === 0 && !orderDone) {
    return (
      <div className="page-shell">
        <div className="container">
          <div style={{ maxWidth: 480, margin: "60px auto", textAlign: "center", padding: 40, background: "white", borderRadius: 24, border: "1px solid #c8d8e8" }}>
            <div style={{ fontSize: "3rem", marginBottom: 16 }}>🛒</div>
            <h2 style={{ margin: "0 0 10px" }}>Your cart is empty</h2>
            <p style={{ color: "#6b7280", marginBottom: 24 }}>Add some books before checking out.</p>
            <Link to="/" className="primary-btn">Browse Books</Link>
          </div>
        </div>
      </div>
    );
  }

  // ── Order success ─────────────────────────────────────────────────────────
  if (orderDone) {
    return (
      <div className="page-shell">
        <div className="container">
          <div style={{ maxWidth: 560, margin: "40px auto", textAlign: "center", padding: 48, background: "white", borderRadius: 28, border: "1px solid #c8d8e8", boxShadow: "0 20px 50px rgba(0,58,101,0.1)" }}>
            <div style={{ width: 80, height: 80, borderRadius: 999, background: "linear-gradient(135deg,#003a65,#0073bc)", display: "grid", placeItems: "center", margin: "0 auto 24px", fontSize: "2rem", color: "white" }}>
              ✓
            </div>
            <h1 style={{ margin: "0 0 10px", fontSize: "1.8rem", color: "#051435", letterSpacing: "-0.03em" }}>
              Order Placed!
            </h1>
            <p style={{ color: "#4a6580", marginBottom: 6 }}>
              Thank you, <strong>{delivery.fullName}</strong>.
            </p>
            <p style={{ color: "#4a6580", marginBottom: 28 }}>
              A confirmation has been sent to <strong>{delivery.email}</strong>.
            </p>
            <div style={{ background: "#f0f4f8", borderRadius: 16, padding: 20, marginBottom: 28, textAlign: "left" }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
                <span style={{ color: "#4a6580", fontSize: "0.88rem" }}>Order number</span>
                <span style={{ fontWeight: 800, fontFamily: "monospace", color: "#003a65" }}>{orderDone.orderNumber}</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
                <span style={{ color: "#4a6580", fontSize: "0.88rem" }}>Items</span>
                <span style={{ fontWeight: 700 }}>{orderDone.totalItems}</span>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ color: "#4a6580", fontSize: "0.88rem" }}>Total paid</span>
                <span style={{ fontWeight: 900, color: "#003a65", fontSize: "1.1rem" }}>£{orderDone.totalPrice}</span>
              </div>
            </div>
            <Link to="/" className="primary-btn">Continue Shopping</Link>
          </div>
        </div>
      </div>
    );
  }

  // ── Step 0: Delivery ──────────────────────────────────────────────────────
  function DeliveryStep() {
    const set   = (k) => (e) => setDelivery((d) => ({ ...d, [k]: e.target.value }));
    const valid = delivery.fullName && delivery.email && delivery.address && delivery.city && delivery.postcode;

    return (
      <div>
        <h2 style={{ margin: "0 0 24px", fontSize: "1.3rem", color: "#051435" }}>Delivery Details</h2>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
          <div style={{ gridColumn: "1 / -1" }}>
            <label style={labelStyle}>Full Name *</label>
            <input style={inputStyle} value={delivery.fullName} onChange={set("fullName")} placeholder="Jane Smith" />
          </div>
          <div style={{ gridColumn: "1 / -1" }}>
            <label style={labelStyle}>Email Address *</label>
            <input style={inputStyle} type="email" value={delivery.email} onChange={set("email")} placeholder="jane@example.com" />
          </div>
          <div style={{ gridColumn: "1 / -1" }}>
            <label style={labelStyle}>Street Address *</label>
            <input style={inputStyle} value={delivery.address} onChange={set("address")} placeholder="123 Main Street" />
          </div>
          <div>
            <label style={labelStyle}>City *</label>
            <input style={inputStyle} value={delivery.city} onChange={set("city")} placeholder="Newcastle upon Tyne" />
          </div>
          <div>
            <label style={labelStyle}>Postcode *</label>
            <input style={inputStyle} value={delivery.postcode} onChange={set("postcode")} placeholder="NE1 7RU" />
          </div>
          <div style={{ gridColumn: "1 / -1" }}>
            <label style={labelStyle}>Country</label>
            <input style={inputStyle} value={delivery.country} onChange={set("country")} />
          </div>
        </div>
        <button
          className="primary-btn"
          disabled={!valid}
          onClick={() => setStep(1)}
          style={{ marginTop: 24, padding: "13px 32px", fontSize: "1rem", opacity: valid ? 1 : 0.5 }}
        >
          Continue to Payment →
        </button>
      </div>
    );
  }

  // ── Step 1: Payment ───────────────────────────────────────────────────────
function PaymentStep() {
  return (
    <div>
      <h2 style={{ margin: "0 0 24px", fontSize: "1.3rem", color: "#051435" }}>
        Payment
      </h2>

      {/* Demo notice */}
      <div style={{
        background: "linear-gradient(135deg, #dceefb, #e0f0ff)",
        border: "1px solid #b8ddf5",
        borderRadius: 16,
        padding: 24,
        marginBottom: 24,
        textAlign: "center",
      }}>
        <div style={{ fontSize: "2.5rem", marginBottom: 12 }}>🎓</div>
        <p style={{ margin: 0, fontWeight: 800, color: "#003a65", fontSize: "1rem" }}>
          Demo Mode — No Payment Required
        </p>
      </div>

      {/* Order total reminder */}
      <div style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "16px 20px",
        background: "#f0f4f8",
        borderRadius: 14,
        marginBottom: 24,
      }}>
        <span style={{ fontWeight: 700, color: "#374151" }}>Order Total</span>
        <span style={{ fontWeight: 900, fontSize: "1.4rem", color: "#003a65" }}>
          £{Number(cart?.totalPrice || 0).toFixed(2)}
        </span>
      </div>

      <div style={{ display: "flex", gap: 12 }}>
        <button type="button" className="ghost-btn" onClick={() => setStep(0)} style={{ padding: "13px 24px" }}>
          ← Back
        </button>
        <button
          type="button"
          className="primary-btn"
          onClick={() => setStep(2)}
          style={{ padding: "13px 32px", fontSize: "1rem" }}
        >
          Review Order →
        </button>
      </div>
    </div>
  );
}



  // ── Step 2: Confirm ───────────────────────────────────────────────────────
  function ConfirmStep() {
    function Row({ label, value }) {
      return (
        <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 0", borderBottom: "1px solid #f0f4f8", fontSize: "0.9rem" }}>
          <span style={{ color: "#4a6580", fontWeight: 700 }}>{label}</span>
          <span style={{ color: "#051435", fontWeight: 600 }}>{value}</span>
        </div>
      );
    }

    async function placeOrder() {
      setPlacing(true);
      setPlaceError(null);

      try {
        await keycloak.updateToken(30);
        const t = keycloak.token;

        // Reserve stock for every cart item — stops on first failure
        for (const item of cart.items) {
          const res = await fetch(`${API.catalog}/${item.productCode}/reserve`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "Authorization": `Bearer ${t}`,
            },
            body: JSON.stringify({ quantity: item.quantity }),
          });

          if (!res.ok) {
            throw new Error(`"${item.productName}" is no longer available in the requested quantity.`);
          }
        }

        // All reserved — build order record
        const orderNumber = `G1-${Date.now().toString(36).toUpperCase()}`;
        const orderRecord = {
          orderNumber,
          placedAt:   new Date().toISOString(),
          delivery,
          items:      cart.items,
          totalItems: cart.totalItems,
          totalPrice: Number(cart.totalPrice).toFixed(2),
        };

        // Save to localStorage
        const existing = JSON.parse(localStorage.getItem("g1_orders") || "[]");
        localStorage.setItem("g1_orders", JSON.stringify([orderRecord, ...existing]));

        // Clear cart on backend
        await fetch(`${API.cart}`, {
          method: "DELETE",
          headers: { "Authorization": `Bearer ${t}` },
        });

        emptyCart();
        setOrderDone(orderRecord);

      } catch (err) {
        setPlaceError(err.message);
      } finally {
        setPlacing(false);
      }
    }

    return (
      <div>
        <h2 style={{ margin: "0 0 24px", fontSize: "1.3rem", color: "#051435" }}>Review Your Order</h2>

        <div style={{ display: "grid", gap: 16, marginBottom: 24 }}>
          <div style={{ background: "#f0f4f8", borderRadius: 16, padding: 18 }}>
            <p style={{ margin: "0 0 12px", fontWeight: 800, color: "#003a65", fontSize: "0.82rem", textTransform: "uppercase", letterSpacing: "0.06em" }}>
              📦 Delivery
            </p>
            <Row label="Name"     value={delivery.fullName} />
            <Row label="Email"    value={delivery.email} />
            <Row label="Address"  value={`${delivery.address}, ${delivery.city}`} />
            <Row label="Postcode" value={delivery.postcode} />
            <Row label="Country"  value={delivery.country} />
          </div>

          <div style={{ background: "#f0f4f8", borderRadius: 16, padding: 18 }}>
            <p style={{ margin: "0 0 12px", fontWeight: 800, color: "#003a65", fontSize: "0.82rem", textTransform: "uppercase", letterSpacing: "0.06em" }}>
              💳 Billing
            </p>
            <Row label="Reference" value={`•••• •••• •••• ${payment.digits.replace(/\s/g, "").slice(-4)}`} />
            <Row label="Name"      value={payment.holderName} />
            <Row label="Valid"     value={payment.validUntil} />
          </div>
        </div>

        {placeError && (
          <div style={{ padding: 14, marginBottom: 16, borderRadius: 12, background: "#fef2f2", border: "1px solid #fecaca", color: "#dc2626", fontWeight: 700, fontSize: "0.9rem" }}>
            ⚠️ {placeError}
          </div>
        )}

        <div style={{ display: "flex", gap: 12 }}>
          <button className="ghost-btn" onClick={() => setStep(1)} style={{ padding: "13px 24px" }} disabled={placing}>
            ← Back
          </button>
          <button
            className="primary-btn"
            onClick={placeOrder}
            disabled={placing}
            style={{ flex: 1, padding: "14px", fontSize: "1rem" }}
          >
            {placing ? "Placing Order…" : `Place Order · £${Number(cart?.totalPrice || 0).toFixed(2)}`}
          </button>
        </div>
      </div>
    );
  }

  // ── Main layout ───────────────────────────────────────────────────────────
  return (
    <div className="page-shell">
      <div className="container">
        <div style={{ marginBottom: 32 }}>
          <p style={{ color: "#0073bc", fontWeight: 800, margin: "0 0 4px" }}>Checkout</p>
          <h1 style={{ margin: 0, fontSize: "2rem", letterSpacing: "-0.03em", color: "#051435" }}>
            Complete Your Order
          </h1>
        </div>

        <StepIndicator step={step} />

        <div style={{ display: "grid", gridTemplateColumns: "1fr 360px", gap: 28, alignItems: "start" }}>
          <div style={{ background: "white", border: "1px solid #c8d8e8", borderRadius: 24, padding: 28 }}>
            {step === 0 && <DeliveryStep />}
            {step === 1 && <PaymentStep />}
            {step === 2 && <ConfirmStep />}
          </div>
          <OrderSummary cart={cart} />
        </div>
      </div>
    </div>
  );
}
