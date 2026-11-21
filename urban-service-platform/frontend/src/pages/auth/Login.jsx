import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { extractErrorMessage } from "../../api/client";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      const account = await login(email, password);
      if (account.role === "ADMIN") navigate("/admin");
      else if (account.role === "PROVIDER") navigate("/bookings");
      else navigate("/services");
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container py-5" style={{ maxWidth: 440 }}>
      <div className="card shadow-sm">
        <div className="card-body p-4">
          <h3 className="mb-3">Sign In</h3>
          {error && <div className="alert alert-danger">{error}</div>}
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="mb-3">
              <label className="form-label">Password</label>
              <input type="password" className="form-control" value={password} onChange={(e) => setPassword(e.target.value)} required />
            </div>
            <button type="submit" className="btn btn-primary w-100" disabled={submitting}>
              {submitting ? "Signing in..." : "Sign In"}
            </button>
          </form>
          <p className="mt-3 mb-0 text-center small">
            New customer? <Link to="/register">Create an account</Link>
          </p>
        </div>
      </div>
    </div>
  );
}