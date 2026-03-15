import { useState } from "react";
import { Link } from "react-router-dom";
import { useCart } from "../context/CartContext";

const STATUS_CONFIG = {
  AVAILABLE: {
    label: "Available",
    color: "#15803d",
    bg: "#dcfce7",
    dot: "#22c55e",
  },
  OUT_OF_STOCK: {
    label: "Out of Stock",
    color: "#b45309",
    bg: "#fef9c3",
    dot: "#f59e0b",
  },
  DISCONTINUED: {
    label: "Discontinued",
    color: "#6b7280",
    bg: "#f3f4f6",
    dot: "#9ca3af",
  },
};

export default function BookCard({ book }) {
  const { addItem } = useCart();
  const [added, setAdded] = useState(false);

  const derivedStatus =
    book.status === "AVAILABLE" && book.stockQuantity <= 0
      ? "OUT_OF_STOCK"
      : book.status;
  const isAvailable = derivedStatus === "AVAILABLE";
  const status = STATUS_CONFIG[derivedStatus] || STATUS_CONFIG.DISCONTINUED; // <-- Fixed!

  function handleAddToCart(e) {
    e.preventDefault();
    if (!isAvailable) return;
    addItem(book, 1);
    setAdded(true);
    setTimeout(() => setAdded(false), 1500);
  }

  return (
    <article className="book-card">
      <div className="book-image-wrap">
        {book.imageUrl ? (
          <img
            src={book.imageUrl}
            alt={book.name}
            className="book-image"
            onError={(e) => {
              e.currentTarget.style.display = "none";
            }}
          />
        ) : (
          <div className="book-image-placeholder">📖</div>
        )}
      </div>

      <div className="book-content">
        {/* Price + availability always on white — clean and readable */}
        <div className="book-meta-row">
          <span className="book-price">£{Number(book.price).toFixed(2)}</span>
          <span
            className="availability-pill"
            style={{ background: status.bg, color: status.color }}
          >
            <span
              className="availability-dot"
              style={{ background: status.dot }}
            />
            {status.label}
          </span>
        </div>

        <h3 className="book-title">{book.name}</h3>
        <p className="book-author">{book.author}</p>

        <div className="book-actions">
          <Link to={`/book/${book.code}`} className="primary-btn small-btn">
            View
          </Link>
          <button
            className={`ghost-btn small-btn${added ? " btn-added" : ""}`}
            type="button"
            disabled={!isAvailable}
            onClick={handleAddToCart}
          >
            {added ? "✓ Added!" : isAvailable ? "Add to Cart" : "Unavailable"}
          </button>
        </div>
      </div>
    </article>
  );
}
