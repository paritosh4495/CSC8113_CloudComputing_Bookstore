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
                display: "grid", placeItems: "center",
              }}>
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none">
                  <rect x="3" y="16" width="18" height="3" rx="1.5" fill="white" opacity="0.35" />
                  <rect x="3" y="11" width="18" height="4" rx="1.5" fill="white" opacity="0.65" />
                  <line x1="7" y1="11" x2="7" y2="15" stroke="white" strokeWidth="1.2" opacity="0.5" />
                  <rect x="3" y="5" width="18" height="5" rx="1.5" fill="white" />
                  <line x1="7" y1="5" x2="7" y2="10" stroke="white" strokeWidth="1.2" opacity="0.6" />
                </svg>
              </div>
              <span style={{ color: "white", fontWeight: 900, fontSize: "1.05rem", letterSpacing: "-0.02em" }}>
                G1 Bookstore
              </span>
            </div>
            <p style={{ margin: 0, lineHeight: 1.8, fontSize: "0.9rem", maxWidth: 280 }}>
              Your smart bookstore. Thousands of titles across every genre, delivered fast.
            </p>
          </div>

          {/* Shop */}
          <div>
            <h4 style={{ color: "white", margin: "0 0 16px", fontWeight: 800, fontSize: "0.9rem", textTransform: "uppercase", letterSpacing: "0.06em" }}>
              Shop
            </h4>
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {[["All Books", "/"], ["Fiction", "/?q=fiction"], ["Science", "/?q=science"], ["My Cart", "/cart"]].map(([label, to]) => (
                <Link key={label} to={to}
                  style={{ color: "#94a3b8", fontSize: "0.9rem", transition: "color 0.2s" }}
                  onMouseEnter={e => e.target.style.color = "white"}
                  onMouseLeave={e => e.target.style.color = "#94a3b8"}>
                  {label}
                </Link>
              ))}
            </div>
          </div>

          {/* Contact */}
          <div>
            <h4 style={{ color: "white", margin: "0 0 16px", fontWeight: 800, fontSize: "0.9rem", textTransform: "uppercase", letterSpacing: "0.06em" }}>
              Contact
            </h4>
            <div style={{ display: "flex", flexDirection: "column", gap: 10, fontSize: "0.9rem" }}>
              <span>📍 Newcastle University</span>
              <span>Newcastle upon Tyne, NE1 7RU</span>
              <span>United Kingdom</span>
              <span style={{ marginTop: 4 }}>📧 group1@ncl.ac.uk</span>
              <span>🖥 CSC8113 Cloud Computing</span>
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
          <span>© 2026 G1 Bookstore. All rights reserved.</span>
          <span style={{ color: "#475569" }}>CSC8113 Group 1 · Newcastle University</span>
        </div>
      </div>
    </footer>
  );
}
