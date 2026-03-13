import { Routes, Route, Navigate } from "react-router-dom";

import Catalogue  from "./Pages/Catalogue";
import BookDetails from "./Pages/BookDetails";
import Cart       from "./Pages/Cart";
import Checkout   from "./Pages/Checkout";
import Admin      from "./Pages/Admin";
import Login      from "./Pages/Login";
import Navbar     from "./components/Navbar";
import Footer from "./components/Footer";

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/"          element={<Catalogue />} />
        <Route path="/catalogue" element={<Navigate to="/" replace />} />
        <Route path="/login"     element={<Login />} />
        <Route path="/book/:code" element={<BookDetails />} />  {/* ← only one, using :code */}
        <Route path="/cart"      element={<Cart />} />
        <Route path="/checkout"  element={<Checkout />} />
        <Route path="/admin"     element={<Admin />} />
        <Route path="*"          element={<Navigate to="/" replace />} />
      </Routes>
      <Footer />
    </>
  );
}