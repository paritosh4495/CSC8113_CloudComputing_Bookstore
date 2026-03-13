import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { getProductByCode } from "../Services/catalogueService";

const STATUS_CONFIG = {
  AVAILABLE:    { label: "Available",    color: "#15803d", bg: "#dcfce7", dot: "#22c55e" },
  OUT_OF_STOCK: { label: "Out of Stock", color: "#b45309", bg: "#fef9c3", dot: "#f59e0b" },
  DISCONTINUED: { label: "Discontinued", color: "#6b7280", bg: "#f3f4f6", dot: "#9ca3af" },
};

function DetailRow({ label, value }) {
  if (!value) return null;
  return (
    <div style={{ display: "flex", gap: 12, padding: "10px 0", borderBottom: "1px solid #f3f4f6" }}>
      <span style={{ minWidth: 130, color: "#6b7280", fontSize: "0.9rem", fontWeight: 700 }}>
        {label}
      </span>
      <span style={{ color: "#111827", fontSize: "0.9rem" }}>{value}</span>
    </div>
  );
}

export default function BookDetail() {
  const { code } = useParams();
  const navigate = useNavigate();
  const { addItem } = useCart();

  const [book,    setBook]    = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);
  const [added,   setAdded]   = useState(false);

  useEffect(() => {
    setLoading(true);
    getProductByCode(code)
      .then(setBook)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [code]);

  function handleAddToCart() {
    if (!book || book.status !== "AVAILABLE") return;
    addItem(book, 1);
    setAdded(true);
    setTimeout(() => setAdded(false), 2000);
  }

  if (loading) {
    return (
      <div className="page-shell">
        <div className="container">
          <div style={{ display: "grid", gridTemplateColumns: "420px 1fr", gap: 28 }}>
            <div style={{ height: 500, borderRadius: 24, background: "linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%)", backgroundSize: "200% 100%", animation: "shimmer 1.4s infinite" }} />
            <div style={{ display: "flex", flexDirection: "column", gap: 16, paddingTop: 20 }}>
              {[200, 120, 80, 80, 80].map((w, i) => (
                <div key={i} style={{ height: 20, width: w, borderRadius: 999, background: "linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%)", backgroundSize: "200% 100%", animation: "shimmer 1.4s infinite" }} />
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-shell">
        <div className="container">
          <div style={{ padding: 40, textAlign: "center", color: "#ef4444", background: "#fef2f2", borderRadius: 18 }}>
            <p style={{ fontSize: "1.1rem", fontWeight: 700 }}>⚠️ {error}</p>
            <button className="ghost-btn small-btn" style={{ marginTop: 16 }} onClick={() => navigate("/")}>
              ← Back to catalogue
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!book) return null;

  const status = STATUS_CONFIG[book.status] || STATUS_CONFIG.DISCONTINUED;
  const isAvailable = book.status === "AVAILABLE";

  return (
    <div className="page-shell">
      <div className="container">

        {/* Breadcrumb */}
        <nav style={{ marginBottom: 24, fontSize: "0.9rem", color: "#6b7280", display: "flex", gap: 8, alignItems: "center" }}>
          <Link to="/" style={{ color: "#4f46e5", fontWeight: 700 }}>Catalogue</Link>
          <span>›</span>
          <span style={{ color: "#111827", fontWeight: 600 }}>{book.name}</span>
        </nav>

        {/* Main layout */}
        <div className="details-layout">

          {/* Left — image */}
          <div>
            {book.imageUrl ? (
              <img
                src={book.imageUrl}
                alt={book.name}
                className="details-image"
                onError={(e) => { e.currentTarget.style.display = "none"; }}
              />
            ) : (
              <div style={{ width: "100%", height: 500, borderRadius: 24, background: "linear-gradient(135deg, #e0e7ff, #ede9fe)", display: "grid", placeItems: "center", fontSize: "6rem" }}>
                📖
              </div>
            )}
          </div>

          {/* Right — content */}
          <div style={{ display: "flex", flexDirection: "column", gap: 0 }}>

            {/* Genre tag */}
            {book.genre && (
              <span style={{ display: "inline-block", background: "#eef2ff", color: "#4338ca", padding: "6px 14px", borderRadius: 999, fontSize: "0.82rem", fontWeight: 700, width: "fit-content", marginBottom: 14 }}>
                {book.genre}
              </span>
            )}

            <h1 style={{ margin: "0 0 8px", fontSize: "clamp(1.8rem, 4vw, 2.6rem)", lineHeight: 1.1, letterSpacing: "-0.03em", color: "#0f172a" }}>
              {book.name}
            </h1>

            <p style={{ margin: "0 0 20px", color: "#6b7280", fontSize: "1rem" }}>
              by <strong style={{ color: "#374151" }}>{book.author}</strong>
            </p>

            {/* Price + availability */}
            <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 24 }}>
              <span style={{ fontSize: "2rem", fontWeight: 900, color: "#0f172a", letterSpacing: "-0.02em" }}>
                £{Number(book.price).toFixed(2)}
              </span>
              <span style={{ display: "inline-flex", alignItems: "center", gap: 6, padding: "6px 14px", borderRadius: 999, fontSize: "0.82rem", fontWeight: 700, background: status.bg, color: status.color }}>
                <span style={{ width: 7, height: 7, borderRadius: 999, background: status.dot, flexShrink: 0 }} />
                {status.label}
              </span>
            </div>

            {/* Add to cart */}
            <div style={{ display: "flex", gap: 12, marginBottom: 32, flexWrap: "wrap" }}>
              <button
                className={`primary-btn${added ? " btn-added" : ""}`}
                style={{ padding: "14px 28px", fontSize: "1rem", opacity: isAvailable ? 1 : 0.5 }}
                disabled={!isAvailable}
                onClick={handleAddToCart}
              >
                {added ? "✓ Added to Cart!" : isAvailable ? "Add to Cart" : "Unavailable"}
              </button>
              <button
                className="ghost-btn"
                style={{ padding: "14px 28px", fontSize: "1rem" }}
                onClick={() => navigate("/")}
              >
                ← Back
              </button>
            </div>

            {/* Description */}
            {book.description && (
              <div style={{ marginBottom: 28, padding: 20, background: "#f8fafc", borderRadius: 16, border: "1px solid #e5e7eb" }}>
                <p style={{ margin: 0, lineHeight: 1.8, color: "#374151", fontSize: "0.95rem" }}>
                  {book.description}
                </p>
              </div>
            )}

            {/* Book details table */}
            <div style={{ background: "white", border: "1px solid #e5e7eb", borderRadius: 16, padding: "4px 16px" }}>
              <DetailRow label="Publisher"        value={book.publisher} />
              <DetailRow label="Published"        value={book.publicationYear} />
              <DetailRow label="ISBN"             value={book.isbn} />
              <DetailRow label="Stock"            value={isAvailable ? `${book.stockQuantity} in stock` : "—"} />
            </div>

          </div>
        </div>

      </div>
    </div>
  );
}
