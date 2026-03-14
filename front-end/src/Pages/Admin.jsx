import { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import { searchProducts } from "../Services/catalogueService";
import {
  adminGetAllProducts,
  adminGetProduct,
  adminCreateProduct,
  adminUpdateProduct,
  adminDeleteProduct,
  adminUpdatePrice,
  adminCheckAvailability,
  adminGetVersion,
} from "../Services/adminService";

const STATUS_COLORS = {
  AVAILABLE: { color: "#15803d", bg: "#dcfce7" },
  OUT_OF_STOCK: { color: "#b45309", bg: "#fef9c3" },
  DISCONTINUED: { color: "#6b7280", bg: "#f3f4f6" },
};

const EMPTY_FORM = {
  name: "",
  author: "",
  isbn: "",
  description: "",
  imageUrl: "",
  price: "",
  genre: "",
  publisher: "",
  publicationYear: "",
  stockQuantity: "",
  status: "AVAILABLE",
};

const inputStyle = {
  padding: "10px 14px",
  borderRadius: 12,
  border: "1px solid #d1d5db",
  outline: "none",
  fontSize: "0.9rem",
  background: "white",
  width: "100%",
};

async function getToken(keycloak, login) {
  try {
    await keycloak.updateToken(30);
  } catch {
    login();
    throw new Error("Session expired");
  }
  return keycloak.token;
}

// ── Shared UI components ────────────────────────────────────────────────────

function Toast({ msg, type }) {
  if (!msg) return null;
  return (
    <div
      style={{
        position: "fixed",
        top: 24,
        right: 24,
        zIndex: 9999,
        padding: "14px 22px",
        borderRadius: 14,
        fontWeight: 700,
        fontSize: "0.9rem",
        background: type === "error" ? "#fef2f2" : "#f0fdf4",
        color: type === "error" ? "#ef4444" : "#16a34a",
        border: `1px solid ${type === "error" ? "#fecaca" : "#bbf7d0"}`,
        boxShadow: "0 10px 30px rgba(0,0,0,0.1)",
      }}
    >
      {type === "error" ? "⚠️" : "✓"} {msg}
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        zIndex: 1000,
        background: "rgba(15,23,42,0.5)",
        display: "grid",
        placeItems: "center",
        padding: 20,
      }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        style={{
          background: "white",
          borderRadius: 24,
          padding: 28,
          width: "100%",
          maxWidth: 600,
          maxHeight: "90vh",
          overflowY: "auto",
          boxShadow: "0 24px 60px rgba(0,0,0,0.2)",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: 22,
          }}
        >
          <h2 style={{ margin: 0, fontSize: "1.3rem", fontWeight: 800 }}>
            {title}
          </h2>
          <button
            onClick={onClose}
            style={{
              background: "none",
              border: "none",
              fontSize: "1.4rem",
              cursor: "pointer",
              color: "#6b7280",
            }}
          >
            ×
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}

function Field({ label, required, children }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
      <label style={{ fontWeight: 700, fontSize: "0.85rem", color: "#374151" }}>
        {label}
        {required && <span style={{ color: "#ef4444" }}> *</span>}
      </label>
      {children}
    </div>
  );
}

function ProductForm({
  initial = EMPTY_FORM,
  onSubmit,
  loading,
  submitLabel = "Save",
}) {
  const [form, setForm] = useState(initial);
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      ...form,
      price: parseFloat(form.price),
      publicationYear: form.publicationYear
        ? parseInt(form.publicationYear)
        : undefined,
      stockQuantity: parseInt(form.stockQuantity),
    });
  }

  return (
    <form
      onSubmit={handleSubmit}
      style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}
    >
      <Field label="Name" required>
        <input
          style={inputStyle}
          value={form.name}
          onChange={set("name")}
          required
        />
      </Field>
      <Field label="Author" required>
        <input
          style={inputStyle}
          value={form.author}
          onChange={set("author")}
          required
        />
      </Field>
      <Field label="ISBN">
        <input style={inputStyle} value={form.isbn} onChange={set("isbn")} />
      </Field>
      <Field label="Genre">
        <input style={inputStyle} value={form.genre} onChange={set("genre")} />
      </Field>
      <Field label="Price (£)" required>
        <input
          style={inputStyle}
          type="number"
          min="0.01"
          step="0.01"
          value={form.price}
          onChange={set("price")}
          required
        />
      </Field>
      <Field label="Stock Quantity" required>
        <input
          style={inputStyle}
          type="number"
          min="0"
          value={form.stockQuantity}
          onChange={set("stockQuantity")}
          required
        />
      </Field>
      <Field label="Publisher">
        <input
          style={inputStyle}
          value={form.publisher}
          onChange={set("publisher")}
        />
      </Field>
      <Field label="Publication Year">
        <input
          style={inputStyle}
          type="number"
          min="100"
          value={form.publicationYear}
          onChange={set("publicationYear")}
        />
      </Field>
      <Field label="Status" required>
        <select
          style={inputStyle}
          value={form.status}
          onChange={set("status")}
          required
        >
          <option value="AVAILABLE">Available</option>
          <option value="OUT_OF_STOCK">Out of Stock</option>
          <option value="DISCONTINUED">Discontinued</option>
        </select>
      </Field>
      <Field label="Image URL">
        <input
          style={inputStyle}
          value={form.imageUrl}
          onChange={set("imageUrl")}
          placeholder="https://..."
        />
      </Field>
      <div style={{ gridColumn: "1 / -1" }}>
        <Field label="Description">
          <textarea
            style={{
              ...inputStyle,
              resize: "vertical",
              minHeight: 80,
              fontFamily: "inherit",
            }}
            value={form.description}
            onChange={set("description")}
          />
        </Field>
      </div>
      <div style={{ gridColumn: "1 / -1" }}>
        <button
          type="submit"
          className="primary-btn"
          disabled={loading}
          style={{ width: "100%", padding: "13px", fontSize: "1rem" }}
        >
          {loading ? "Saving…" : submitLabel}
        </button>
      </div>
    </form>
  );
}

function PriceForm({ current, onSubmit, loading }) {
  const [price, setPrice] = useState(current);
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      <p style={{ margin: 0, color: "#6b7280" }}>
        Current price: <strong>£{Number(current).toFixed(2)}</strong>
      </p>
      <input
        type="number"
        min="0.01"
        step="0.01"
        value={price}
        onChange={(e) => setPrice(e.target.value)}
        style={inputStyle}
      />
      <button
        className="primary-btn"
        disabled={loading || !price}
        onClick={() => onSubmit(parseFloat(price))}
        style={{ padding: "13px" }}
      >
        {loading ? "Updating…" : "Update Price"}
      </button>
    </div>
  );
}

// ── Main component ──────────────────────────────────────────────────────────

export default function Admin() {
  const { keycloak, login } = useAuth();

  const [tab, setTab] = useState("products");
  const [products, setProducts] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [page, setPage] = useState(1);
  const [version, setVersion] = useState(null);
  const [loading, setLoading] = useState(false);
  const [actionBusy, setActionBusy] = useState(null);

  // Search
  const [search, setSearch] = useState("");
  const [debouncedQ, setDebouncedQ] = useState("");

  // Modals
  const [editProduct, setEditProduct] = useState(null);
  const [priceModal, setPriceModal] = useState(null);
  const [availModal, setAvailModal] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  const [toast, setToast] = useState(null);

  function showToast(msg, type = "success") {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3500);
  }

  const token = useCallback(() => getToken(keycloak, login), [keycloak, login]);

  // Debounce search input — 400ms
  useEffect(() => {
    const t = setTimeout(() => {
      setDebouncedQ(search);
      setPage(1);
    }, 400);
    return () => clearTimeout(t);
  }, [search]);

  // Load products — uses search endpoint when query present, list endpoint when not
  const loadProducts = useCallback(async () => {
    setLoading(true);
    try {
      let res;
      if (debouncedQ.trim()) {
        // Server-side search — same endpoint as home page
        res = await searchProducts({ query: debouncedQ.trim(), page });
      } else {
        const t = await token();
        res = await adminGetAllProducts(t, page);
      }
      setProducts(res.data || []);
      setTotalPages(res.totalPages || 1);
    } catch (err) {
      showToast(err.message, "error");
    } finally {
      setLoading(false);
    }
  }, [debouncedQ, page]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  useEffect(() => {
    token()
      .then((t) => adminGetVersion(t).then(setVersion))
      .catch(() => {});
  }, []);

  // ── Actions ──────────────────────────────────────────────────────────────

  async function handleCreate(data) {
    setActionBusy("create");
    try {
      const t = await token();
      await adminCreateProduct(t, data);
      showToast("Book created successfully");
      setTab("products");
      loadProducts();
    } catch (err) {
      showToast(err.message, "error");
    } finally {
      setActionBusy(null);
    }
  }

  async function handleUpdate(code, data) {
    setActionBusy(code);
    try {
      const t = await token();
      await adminUpdateProduct(t, code, data);
      showToast("Book updated");
      setEditProduct(null);
      loadProducts();
    } catch (err) {
      showToast(err.message, "error");
    } finally {
      setActionBusy(null);
    }
  }

  async function handleDelete(code) {
    setActionBusy(code);
    try {
      const t = await token();
      await adminDeleteProduct(t, code);
      showToast("Book deleted");
      setDeleteConfirm(null);
      loadProducts();
    } catch (err) {
      showToast(err.message, "error");
    } finally {
      setActionBusy(null);
    }
  }

  async function handlePriceUpdate(code, newPrice) {
    setActionBusy(code);
    try {
      const t = await token();
      await adminUpdatePrice(t, code, newPrice);
      showToast(`Price updated to £${newPrice}`);
      setPriceModal(null);
      loadProducts();
    } catch (err) {
      showToast(err.message, "error");
    } finally {
      setActionBusy(null);
    }
  }

  async function handleCheckAvail(code) {
    setActionBusy(code);
    try {
      const t = await token();
      const data = await adminCheckAvailability(t, code);
      setAvailModal(data);
    } catch (err) {
      showToast(err.message, "error");
    } finally {
      setActionBusy(null);
    }
  }

  async function openEdit(code) {
    setActionBusy(code);
    try {
      const t = await token();
      const data = await adminGetProduct(t, code);
      setEditProduct(data);
    } catch (err) {
      showToast(err.message, "error");
    } finally {
      setActionBusy(null);
    }
  }

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <div className="page-shell">
      <div className="container">
        {toast && <Toast msg={toast.msg} type={toast.type} />}

        {/* Header */}
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "flex-start",
            marginBottom: 28,
            flexWrap: "wrap",
            gap: 14,
          }}
        >
          <div>
            <p style={{ color: "#4f46e5", fontWeight: 800, margin: "0 0 4px" }}>
              Admin Dashboard
            </p>
            <h1
              style={{ margin: 0, fontSize: "2rem", letterSpacing: "-0.03em" }}
            >
              Catalogue Management
            </h1>
          </div>
          <button
            className="primary-btn"
            onClick={() => setTab("add")}
            style={{ padding: "12px 22px" }}
          >
            + Add New Book
          </button>
        </div>

        {/* Tabs */}
        <div
          style={{
            display: "flex",
            gap: 8,
            marginBottom: 24,
            borderBottom: "2px solid #e5e7eb",
          }}
        >
          {[
            ["products", "📚 All Products"],
            ["add", "➕ Add Product"],
          ].map(([key, label]) => (
            <button
              key={key}
              onClick={() => setTab(key)}
              style={{
                padding: "10px 18px",
                borderRadius: "12px 12px 0 0",
                border: "none",
                cursor: "pointer",
                fontWeight: 700,
                fontSize: "0.9rem",
                background: tab === key ? "white" : "transparent",
                color: tab === key ? "#4f46e5" : "#6b7280",
                borderBottom: tab === key ? "2px solid white" : "none",
                marginBottom: tab === key ? -2 : 0,
              }}
            >
              {label}
            </button>
          ))}
        </div>

        {/* ── TAB: All Products ── */}
        {tab === "products" && (
          <div>
            {/* Server-side search */}
            <div
              style={{
                marginBottom: 16,
                display: "flex",
                alignItems: "center",
                gap: 10,
                background: "white",
                border: "1px solid #e2e8f0",
                borderRadius: 999,
                padding: "0 16px",
                maxWidth: 400,
                boxShadow: "0 4px 12px rgba(15,23,42,0.05)",
              }}
            >
              <svg
                viewBox="0 0 24 24"
                width="16"
                height="16"
                fill="none"
                style={{ flexShrink: 0, color: "#94a3b8" }}
              >
                <circle
                  cx="11"
                  cy="11"
                  r="6.5"
                  stroke="currentColor"
                  strokeWidth="1.8"
                />
                <path
                  d="M16 16l4 4"
                  stroke="currentColor"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                />
              </svg>
              <input
                type="search"
                placeholder="Search by name, author, ISBN…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{
                  border: "none",
                  outline: "none",
                  padding: "11px 0",
                  width: "100%",
                  fontSize: "0.9rem",
                  background: "transparent",
                }}
              />
            </div>

            {loading ? (
              <div
                style={{
                  padding: 60,
                  textAlign: "center",
                  color: "#6b7280",
                  fontWeight: 700,
                }}
              >
                Loading…
              </div>
            ) : (
              <>
                <div
                  style={{
                    background: "white",
                    border: "1px solid #e5e7eb",
                    borderRadius: 20,
                    overflow: "hidden",
                  }}
                >
                  <table
                    style={{
                      width: "100%",
                      borderCollapse: "collapse",
                      fontSize: "0.88rem",
                    }}
                  >
                    <thead>
                      <tr
                        style={{
                          background: "#f8fafc",
                          borderBottom: "2px solid #e5e7eb",
                        }}
                      >
                        {[
                          "Code",
                          "Name",
                          "Author",
                          "Price",
                          "Status",
                          "Actions",
                        ].map((h) => (
                          <th
                            key={h}
                            style={{
                              padding: "14px 16px",
                              textAlign: "left",
                              fontWeight: 800,
                              color: "#374151",
                              whiteSpace: "nowrap",
                            }}
                          >
                            {h}
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {products.length === 0 && (
                        <tr>
                          <td
                            colSpan={6}
                            style={{
                              padding: 40,
                              textAlign: "center",
                              color: "#6b7280",
                            }}
                          >
                            No products found.
                          </td>
                        </tr>
                      )}
                      {products.map((p, i) => {
                        const sc =
                          STATUS_COLORS[p.status] || STATUS_COLORS.DISCONTINUED;
                        const busy = actionBusy === p.code;
                        return (
                          <tr
                            key={p.code}
                            style={{
                              borderBottom: "1px solid #f3f4f6",
                              background: i % 2 === 0 ? "white" : "#fafafa",
                            }}
                          >
                            <td
                              style={{
                                padding: "12px 16px",
                                fontWeight: 700,
                                color: "#4f46e5",
                                fontFamily: "monospace",
                              }}
                            >
                              {p.code}
                            </td>
                            <td
                              style={{
                                padding: "12px 16px",
                                fontWeight: 700,
                                color: "#111827",
                                maxWidth: 200,
                              }}
                            >
                              <div
                                style={{
                                  overflow: "hidden",
                                  textOverflow: "ellipsis",
                                  whiteSpace: "nowrap",
                                }}
                              >
                                {p.name}
                              </div>
                            </td>
                            <td
                              style={{ padding: "12px 16px", color: "#374151" }}
                            >
                              {p.author}
                            </td>
                            <td
                              style={{ padding: "12px 16px", fontWeight: 800 }}
                            >
                              £{Number(p.price).toFixed(2)}
                            </td>
                            <td style={{ padding: "12px 16px" }}>
                              <span
                                style={{
                                  padding: "4px 10px",
                                  borderRadius: 999,
                                  fontSize: "0.78rem",
                                  fontWeight: 700,
                                  background: sc.bg,
                                  color: sc.color,
                                }}
                              >
                                {p.status}
                              </span>
                            </td>
                            <td style={{ padding: "12px 16px" }}>
                              <div
                                style={{
                                  display: "flex",
                                  gap: 6,
                                  flexWrap: "wrap",
                                }}
                              >
                                <button
                                  className="ghost-btn small-btn"
                                  disabled={busy}
                                  onClick={() => openEdit(p.code)}
                                  style={{
                                    fontSize: "0.78rem",
                                    padding: "6px 10px",
                                  }}
                                >
                                  ✏️ Edit
                                </button>
                                <button
                                  className="ghost-btn small-btn"
                                  disabled={busy}
                                  onClick={() => setPriceModal(p)}
                                  style={{
                                    fontSize: "0.78rem",
                                    padding: "6px 10px",
                                  }}
                                >
                                  💷 Price
                                </button>
                                <button
                                  className="ghost-btn small-btn"
                                  disabled={busy}
                                  onClick={() => handleCheckAvail(p.code)}
                                  style={{
                                    fontSize: "0.78rem",
                                    padding: "6px 10px",
                                  }}
                                >
                                  📦 Avail
                                </button>
                                <button
                                  disabled={busy}
                                  onClick={() => setDeleteConfirm(p)}
                                  style={{
                                    fontSize: "0.78rem",
                                    padding: "6px 10px",
                                    borderRadius: 999,
                                    border: "1px solid #fecaca",
                                    background: "#fef2f2",
                                    color: "#ef4444",
                                    cursor: "pointer",
                                    fontWeight: 700,
                                  }}
                                >
                                  🗑 Delete
                                </button>
                              </div>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>

                {totalPages > 1 && (
                  <div
                    style={{
                      display: "flex",
                      gap: 10,
                      justifyContent: "center",
                      marginTop: 20,
                    }}
                  >
                    <button
                      className="ghost-btn small-btn"
                      disabled={page === 1}
                      onClick={() => setPage(1)}
                    >
                      « First
                    </button>
                    <button
                      className="ghost-btn small-btn"
                      disabled={page === 1}
                      onClick={() => setPage((p) => p - 1)}
                    >
                      ‹ Prev
                    </button>
                    <span
                      style={{
                        padding: "8px 14px",
                        background: "white",
                        border: "1px solid #e5e7eb",
                        borderRadius: 999,
                        fontWeight: 700,
                      }}
                    >
                      {page} / {totalPages}
                    </span>
                    <button
                      className="ghost-btn small-btn"
                      disabled={page === totalPages}
                      onClick={() => setPage((p) => p + 1)}
                    >
                      Next ›
                    </button>
                    <button
                      className="ghost-btn small-btn"
                      disabled={page === totalPages}
                      onClick={() => setPage(totalPages)}
                    >
                      Last »
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        )}

        {/* ── TAB: Add Product ── */}
        {tab === "add" && (
          <div
            style={{
              background: "white",
              border: "1px solid #e5e7eb",
              borderRadius: 20,
              padding: 28,
            }}
          >
            <h2 style={{ margin: "0 0 22px", fontSize: "1.2rem" }}>
              Add New Book
            </h2>
            <ProductForm
              onSubmit={handleCreate}
              loading={actionBusy === "create"}
              submitLabel="Create Book"
            />
          </div>
        )}

        {/* ── MODAL: Edit ── */}
        {editProduct && (
          <Modal
            title={`Edit — ${editProduct.name}`}
            onClose={() => setEditProduct(null)}
          >
            <ProductForm
              initial={editProduct}
              onSubmit={(data) => handleUpdate(editProduct.code, data)}
              loading={actionBusy === editProduct.code}
              submitLabel="Save Changes"
            />
          </Modal>
        )}

        {/* ── MODAL: Price ── */}
        {priceModal && (
          <Modal
            title={`Update Price — ${priceModal.name}`}
            onClose={() => setPriceModal(null)}
          >
            <PriceForm
              current={priceModal.price}
              onSubmit={(price) => handlePriceUpdate(priceModal.code, price)}
              loading={actionBusy === priceModal.code}
            />
          </Modal>
        )}

        {/* ── MODAL: Availability ── */}
        {availModal && (
          <Modal title="Stock Availability" onClose={() => setAvailModal(null)}>
            <div style={{ textAlign: "center", padding: "20px 0" }}>
              <div style={{ fontSize: "3rem", marginBottom: 12 }}>
                {availModal.available ? "✅" : "❌"}
              </div>
              <p
                style={{
                  fontWeight: 800,
                  fontSize: "1.1rem",
                  margin: "0 0 8px",
                }}
              >
                Code: {availModal.code}
              </p>
              <p style={{ color: "#6b7280", margin: "0 0 6px" }}>
                Status:{" "}
                <strong>
                  {availModal.available ? "Available" : "Not Available"}
                </strong>
              </p>
              <p style={{ color: "#6b7280", margin: 0 }}>
                Quantity: <strong>{availModal.availableQuantity}</strong>
              </p>
            </div>
          </Modal>
        )}

        {/* ── MODAL: Delete Confirm ── */}
        {deleteConfirm && (
          <Modal title="Confirm Delete" onClose={() => setDeleteConfirm(null)}>
            <div style={{ textAlign: "center", padding: "10px 0 20px" }}>
              <div style={{ fontSize: "2.5rem", marginBottom: 12 }}>🗑️</div>
              <p style={{ fontWeight: 700, fontSize: "1rem", marginBottom: 6 }}>
                Are you sure you want to delete:
              </p>
              <p
                style={{
                  color: "#4f46e5",
                  fontWeight: 900,
                  fontSize: "1.1rem",
                  marginBottom: 24,
                }}
              >
                {deleteConfirm.name}
              </p>
              <div
                style={{ display: "flex", gap: 12, justifyContent: "center" }}
              >
                <button
                  className="ghost-btn"
                  onClick={() => setDeleteConfirm(null)}
                >
                  Cancel
                </button>
                <button
                  disabled={actionBusy === deleteConfirm.code}
                  onClick={() => handleDelete(deleteConfirm.code)}
                  style={{
                    padding: "12px 24px",
                    borderRadius: 999,
                    border: "none",
                    background: "#ef4444",
                    color: "white",
                    fontWeight: 800,
                    cursor: "pointer",
                  }}
                >
                  {actionBusy === deleteConfirm.code
                    ? "Deleting…"
                    : "Yes, Delete"}
                </button>
              </div>
            </div>
          </Modal>
        )}
      </div>
    </div>
  );
}
