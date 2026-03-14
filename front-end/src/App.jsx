import { Routes, Route, Navigate, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import { useEffect } from "react";

import Catalogue   from "./Pages/Catalogue";
import BookDetails from "./Pages/BookDetails";
import Cart        from "./Pages/Cart";
import Checkout    from "./Pages/Checkout";
import Admin       from "./Pages/Admin";
import Login       from "./Pages/Login";
import Navbar      from "./components/Navbar";
import Footer      from "./components/Footer";

function AdminRoute({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/" replace />;
  if (!user.roles.includes("admin")) return <Navigate to="/" replace />;
  return children;
}

function AdminRedirect() {
  const { user } = useAuth();
  const navigate  = useNavigate();
  const location  = useLocation();

  useEffect(() => {
    if (user?.roles.includes("admin") && location.pathname !== "/admin") {
      navigate("/admin", { replace: true });
    }
  }, [user]);

  return null;
}

export default function App() {
  return (
    <>
      <AdminRedirect />
      <Navbar />
      <Routes>
        <Route path="/"           element={<Catalogue />} />
        <Route path="/catalogue"  element={<Navigate to="/" replace />} />
        <Route path="/book/:code" element={<BookDetails />} />
        <Route path="/cart"       element={<Cart />} />
        <Route path="/checkout"   element={<Checkout />} />
        <Route path="/admin"      element={<AdminRoute><Admin /></AdminRoute>} />
        <Route path="*"           element={<Navigate to="/" replace />} />
      </Routes>
      <Footer />
    </>
  );
}
