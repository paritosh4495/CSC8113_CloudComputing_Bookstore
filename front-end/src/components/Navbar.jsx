import { Link, useNavigate, createSearchParams, useSearchParams } from "react-router-dom";
import { useState, useEffect } from "react";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";

function getInitials(user) {
  const source = user?.name || user?.username || user?.email || "U";
  const parts = source.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return `${parts[0][0] || ""}${parts[1][0] || ""}`.toUpperCase();
}

function getUsername(user) {
  return user?.username || user?.name || user?.email || "User";
}

function BrandLogo() {
  return (
    <div className="brand-mark" aria-hidden="true">
      <svg viewBox="0 0 24 24" width="22" height="22" fill="none">
        <path d="M4 6.5C4 5.67 4.67 5 5.5 5H18a2 2 0 0 1 2 2v10.5a.5.5 0 0 1-.8.4C18.13 17.1 16.88 16.5 15 16.5H6.5A2.5 2.5 0 0 0 4 19V6.5Z" fill="currentColor" opacity="0.18" />
        <path d="M6.5 5H18a2 2 0 0 1 2 2v10.5a.5.5 0 0 1-.8.4C18.13 17.1 16.88 16.5 15 16.5H6.5A2.5 2.5 0 0 0 4 19V7.5A2.5 2.5 0 0 1 6.5 5Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
        <path d="M8 8.5h8M8 11h8M8 13.5h5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" />
      </svg>
    </div>
  );
}

function CartIcon() {
  return (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none">
      <path d="M3 4h2l1.2 7.2A2 2 0 0 0 8.18 13H17a2 2 0 0 0 1.95-1.55L20 7H7" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="9" cy="19" r="1.6" fill="currentColor" />
      <circle cx="17" cy="19" r="1.6" fill="currentColor" />
    </svg>
  );
}

function SearchIcon() {
  return (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none">
      <circle cx="11" cy="11" r="6.5" stroke="currentColor" strokeWidth="1.8" />
      <path d="M16 16l4 4" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </svg>
  );
}

export default function Navbar() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { count } = useCart();
  const { user, login, logout } = useAuth();

  const currentQuery = searchParams.get("q") || "";
  const [search, setSearch] = useState(currentQuery);

  // Sync input when URL changes (e.g. browser back/forward or Reset Filters)
  useEffect(() => {
    setSearch(currentQuery);
  }, [currentQuery]);

  // Debounced live search — fires 400ms after user stops typing
  useEffect(() => {
    const timer = setTimeout(() => {
      const q = search.trim();
      // Don't navigate if nothing changed
      if (q === currentQuery) return;
      navigate({
        pathname: "/",
        search: q ? `?${createSearchParams({ q })}` : "",
      });
    }, 400);

    return () => clearTimeout(timer);
  }, [search]);

  // Instant search on Enter
  function handleSubmit(e) {
    e.preventDefault();
    const q = search.trim();
    navigate({
      pathname: "/",
      search: q ? `?${createSearchParams({ q })}` : "",
    });
  }

  const initials = getInitials(user);
  const username = getUsername(user);

  return (
    <header className="topbar">
      <div className="container topbar-inner">
        <Link to="/" className="brand" aria-label="Go to homepage">
          <BrandLogo />
          <div className="brand-copy">
            <span className="brand-title">CloudLeaf Books</span>
            <span className="brand-subtitle">Smart bookstore</span>
          </div>
        </Link>

        <form className="nav-search" onSubmit={handleSubmit} role="search">
          <span className="nav-search-icon"><SearchIcon /></span>
          <input
            type="search"
            placeholder="Search books, authors, ISBN..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            aria-label="Search books"
          />
        </form>

        <div className="nav-actions">
          {user ? (
            <>
              <Link to="/cart" className="cart-btn" aria-label="Open cart">
                <CartIcon />
                {count > 0 && <span className="cart-badge">{count}</span>}
              </Link>

              <div className="profile-wrap" title={username}>
                <Link to="/admin" className="profile-chip" aria-label={username}>
                  <span className="profile-avatar">{initials}</span>
                </Link>
                <div className="profile-tooltip">{username}</div>
              </div>

              <button type="button" className="logout-btn" onClick={logout}>
                Logout
              </button>
            </>
          ) : (
            <button type="button" className="login-btn" onClick={login}>
              Login
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
