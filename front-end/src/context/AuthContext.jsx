import { createContext, useContext, useEffect, useRef, useState } from "react";
import keycloak from "../config/keycloak";

const AuthContext = createContext(null);

function buildUser(kc) {
  if (!kc.authenticated || !kc.tokenParsed) return null;
  const t = kc.tokenParsed;
  return {
    id:       t.sub,
    username: t.preferred_username,
    name:     t.name || t.preferred_username,
    email:    t.email,
    roles:    t.realm_access?.roles || [],
  };
}

export function AuthProvider({ children }) {
  const [user,    setUser]    = useState(null);
  const [loading, setLoading] = useState(true);
  const initialized           = useRef(false);

  useEffect(() => {
    if (initialized.current) return;
    initialized.current = true;

    keycloak.onAuthSuccess  = () => setUser(buildUser(keycloak));
    keycloak.onAuthLogout   = () => setUser(null);
    keycloak.onTokenExpired = () =>
      keycloak.updateToken(30).catch(() => setUser(null));

    keycloak
      .init({
        onLoad: undefined,
        checkLoginIframe: false,
        pkceMethod: "S256",
      })
      .then((authenticated) => {
        if (authenticated) setUser(buildUser(keycloak));
      })
      .catch((err) => console.error("Keycloak init failed:", err))
      .finally(() => setLoading(false));
  }, []);

  const login  = () => keycloak.login();
  const logout = () => keycloak.logout({ redirectUri: window.location.origin });

  if (loading) {
    return (
      <div style={{ minHeight: "100vh", display: "grid", placeItems: "center" }}>
        <span style={{ color: "#6366f1", fontWeight: 800 }}>Loading…</span>
      </div>
    );
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, keycloak }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
