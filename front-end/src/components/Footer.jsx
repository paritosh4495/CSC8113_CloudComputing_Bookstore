import { Link } from "react-router-dom";

export default function Footer() {
  return (
    <footer style={{
      background: "#0f172a",
      color: "#94a3b8",
      padding: "48px 0 28px",
      marginTop: "auto",
    }}>
      <div className="container">
        <div style={{
          display: "grid",
          gridTemplateColumns: "2fr 1fr 1fr",
          gap: 40,
          marginBottom: 40,
        }}>

          {/* Brand */}
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 14 }}>
              <div style={{
                width: 36, height: 36, borderRadius: 10,
                background: "linear-gradient(135deg, #4f46e5, #7c3aed)",
                display: "grid", placeItems: "center", fontSize: "1.1rem",
              }}>
                📚
              </div>
              <span style={{ color: "white", fontWeight: 900, fontSize: "1.05rem", letterSpacing: "-0.02em" }}>
                CloudLeaf Books
              </span>
            </div>
            <p style={{ margin: 0, lineHeight: 1.8, fontSize: "0.9rem", maxWidth: 280 }}>
              Your smart bookstore. Thousands of titles across every genre,
              delivered fast.
            </p>
          </div>

          {/* Shop links */}
          <div>
            <h4 style={{ color: "white", margin: "0 0 16px", fontWeight: 800, fontSize: "0.9rem", textTransform: "uppercase", letterSpacing: "0.06em" }}>
              Shop
            </h4>
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              <Link to="/" style={{ color: "#94a3b8", fontSize: "0.9rem", transition: "color 0.2s" }}
                onMouseEnter={e => e.target.style.color = "white"}
                onMouseLeave={e => e.target.style.color = "#94a3b8"}>
                All Books
              </Link>
              <Link to="/?q=fiction" style={{ color: "#94a3b8", fontSize: "0.9rem", transition: "color 0.2s" }}
                onMouseEnter={e => e.target.style.color = "white"}
                onMouseLeave={e => e.target.style.color = "#94a3b8"}>
                Fiction
              </Link>
              <Link to="/?q=science" style={{ color: "#94a3b8", fontSize: "0.9rem", transition: "color 0.2s" }}
                onMouseEnter={e => e.target.style.color = "white"}
                onMouseLeave={e => e.target.style.color = "#94a3b8"}>
                Science
              </Link>
              <Link to="/cart" style={{ color: "#94a3b8", fontSize: "0.9rem", transition: "color 0.2s" }}
                onMouseEnter={e => e.target.style.color = "white"}
                onMouseLeave={e => e.target.style.color = "#94a3b8"}>
                My Cart
              </Link>
            </div>
          </div>

          {/* Account links */}
          <div>
            <h4 style={{ color: "white", margin: "0 0 16px", fontWeight: 800, fontSize: "0.9rem", textTransform: "uppercase", letterSpacing: "0.06em" }}>
              Account
            </h4>
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              <Link to="/login" style={{ color: "#94a3b8", fontSize: "0.9rem", transition: "color 0.2s" }}
                onMouseEnter={e => e.target.style.color = "white"}
                onMouseLeave={e => e.target.style.color = "#94a3b8"}>
                Login
              </Link>
              <Link to="/admin" style={{ color: "#94a3b8", fontSize: "0.9rem", transition: "color 0.2s" }}
                onMouseEnter={e => e.target.style.color = "white"}
                onMouseLeave={e => e.target.style.color = "#94a3b8"}>
                Admin Panel
              </Link>
            </div>
          </div>
        </div>

        {/* Bottom bar */}
        <div style={{
          borderTop: "1px solid #1e293b",
          paddingTop: 20,
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          flexWrap: "wrap",
          gap: 10,
          fontSize: "0.82rem",
        }}>
          <span>© 2026 CloudLeaf Books. All rights reserved.</span>
          <span style={{ color: "#475569" }}>CSC8113 Group 1 · Newcastle University</span>
        </div>
      </div>
    </footer>
  );
}
