import { BrowserRouter, Link, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import BookService from "./pages/customer/BookService";
import Addresses from "./pages/customer/Addresses";
import MyBookings from "./pages/MyBookings";
import AdminDashboard from "./pages/admin/AdminDashboard";

function Navbar() {
  const { user, logout } = useAuth();

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark px-3">
      <Link className="navbar-brand fw-bold" to="/">UrbanService</Link>
      <div className="navbar-nav me-auto">
        {user?.role === "CUSTOMER" && (
          <>
            <Link className="nav-link" to="/services">Book Services</Link>
            <Link className="nav-link" to="/addresses">My Addresses</Link>
          </>
        )}
        {user && <Link className="nav-link" to="/bookings">Bookings</Link>}
        {user?.role === "ADMIN" && <Link className="nav-link" to="/admin">Admin Panel</Link>}
      </div>
      <div className="d-flex align-items-center gap-2 text-white">
        {user ? (
          <>
            <span className="small">{user.fullName} ({user.role})</span>
            <button className="btn btn-outline-light btn-sm" onClick={logout}>Logout</button>
          </>
        ) : (
          <>
            <Link className="btn btn-outline-light btn-sm" to="/login">Login</Link>
            <Link className="btn btn-primary btn-sm" to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

function HomeRedirect() {
  const { user, initializing } = useAuth();
  if (initializing) return null;
  if (!user) return <Navigate to="/login" replace />;
  if (user.role === "ADMIN") return <Navigate to="/admin" replace />;
  if (user.role === "PROVIDER") return <Navigate to="/bookings" replace />;
  return <Navigate to="/services" replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route path="/" element={<HomeRedirect />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/services" element={<ProtectedRoute allowedRoles={["CUSTOMER"]}><BookService /></ProtectedRoute>} />
          <Route path="/addresses" element={<ProtectedRoute allowedRoles={["CUSTOMER"]}><Addresses /></ProtectedRoute>} />
          <Route path="/bookings" element={<ProtectedRoute allowedRoles={["CUSTOMER", "PROVIDER", "ADMIN"]}><MyBookings /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminDashboard /></ProtectedRoute>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}