import { useEffect, useState } from "react";
import { api, extractErrorMessage } from "../api/client";
import { useAuth } from "../auth/AuthContext";

export default function MyBookings() {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  const loadBookings = () => {
    setLoading(true);
    api.get("/bookings")
      .then((res) => setBookings(res.data))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadBookings(); }, []);

  const updateStatus = async (booking, nextStatus) => {
    setBusyId(booking.id);
    setError("");
    try {
      const { data } = await api.patch(`/bookings/${booking.id}/status`, {
        status: nextStatus,
        version: booking.version,
      });
      setBookings((prev) => prev.map((b) => (b.id === data.id ? data : b)));
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusyId(null);
    }
  };

  if (loading) return <div className="container py-4">Loading bookings...</div>;

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>{user.role === "PROVIDER" ? "Job Queue & Assigned Bookings" : "Bookings"}</h2>
        <button className="btn btn-outline-primary btn-sm" onClick={loadBookings}>Refresh</button>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="table-responsive">
        <table className="table table-bordered align-middle bg-white">
          <thead className="table-light">
            <tr>
              <th>ID</th>
              <th>Service</th>
              <th>Date & Slot</th>
              <th>Address</th>
              <th>Price</th>
              <th>Provider</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {bookings.length === 0 && (
              <tr><td colSpan="8" className="text-center text-muted py-4">No bookings found.</td></tr>
            )}
            {bookings.map((b) => (
              <tr key={b.id}>
                <td>#{b.id}</td>
                <td><strong>{b.packageTitle}</strong><br /><small className="text-muted">{b.categoryName}</small></td>
                <td>{b.scheduledDate}<br /><small>{b.timeSlot}</small></td>
                <td><small>{b.fullAddress}</small></td>
                <td>₹{b.totalAmount}</td>
                <td>{b.providerName}</td>
                <td><span className="badge bg-info text-dark">{b.status}</span></td>
                <td>
                  {user.role === "CUSTOMER" && ["PENDING", "ASSIGNED"].includes(b.status) && (
                    <button className="btn btn-sm btn-outline-danger" disabled={busyId === b.id} onClick={() => updateStatus(b, "CANCELLED")}>
                      Cancel
                    </button>
                  )}
                  {user.role === "PROVIDER" && b.status === "PENDING" && (
                    <button className="btn btn-sm btn-primary" disabled={busyId === b.id} onClick={() => updateStatus(b, "ASSIGNED")}>
                      Accept Job
                    </button>
                  )}
                  {user.role === "PROVIDER" && b.status === "ASSIGNED" && (
                    <button className="btn btn-sm btn-warning" disabled={busyId === b.id} onClick={() => updateStatus(b, "IN_PROGRESS")}>
                      Start Job
                    </button>
                  )}
                  {user.role === "PROVIDER" && b.status === "IN_PROGRESS" && (
                    <button className="btn btn-sm btn-success" disabled={busyId === b.id} onClick={() => updateStatus(b, "COMPLETED")}>
                      Complete
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}