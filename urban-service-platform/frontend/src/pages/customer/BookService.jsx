import { useEffect, useState } from "react";
import { api, extractErrorMessage } from "../../api/client";

const TIME_SLOTS = ["09:00-10:30", "11:00-12:30", "14:00-15:30", "16:30-18:00"];

export default function BookService() {
  const [packages, setPackages] = useState([]);
  const [addresses, setAddresses] = useState([]);
  const [selectedPkg, setSelectedPkg] = useState(null);
  const [addressId, setAddressId] = useState("");
  const [scheduledDate, setScheduledDate] = useState("");
  const [timeSlot, setTimeSlot] = useState(TIME_SLOTS[0]);
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState({ type: "", text: "" });

  useEffect(() => {
    Promise.all([api.get("/catalog/packages"), api.get("/customer/addresses")])
      .then(([pkgRes, addrRes]) => {
        setPackages(pkgRes.data);
        setAddresses(addrRes.data);
        if (addrRes.data.length > 0) setAddressId(String(addrRes.data[0].id));
      })
      .catch((err) => setFeedback({ type: "danger", text: extractErrorMessage(err) }));
  }, []);

  const handleBook = async (e) => {
    e.preventDefault();
    if (!selectedPkg || !addressId || !scheduledDate) return;
    setSubmitting(true);
    setFeedback({ type: "", text: "" });

    try {
      await api.post("/bookings", {
        packageId: selectedPkg.id,
        addressId: Number(addressId),
        scheduledDate,
        timeSlot,
      });
      setFeedback({ type: "success", text: `Booked "${selectedPkg.title}" successfully!` });
      setSelectedPkg(null);
    } catch (err) {
      setFeedback({ type: "danger", text: extractErrorMessage(err) });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container py-4">
      <h2 className="mb-3">At-Home Services</h2>
      {feedback.text && <div className={`alert alert-${feedback.type}`}>{feedback.text}</div>}

      <div className="row g-3">
        {packages.map((pkg) => (
          <div className="col-md-4" key={pkg.id}>
            <div className="card h-100 shadow-sm">
              <div className="card-body d-flex flex-column">
                <span className="badge bg-secondary align-self-start mb-2">{pkg.categoryName}</span>
                <h5 className="card-title">{pkg.title}</h5>
                <p className="card-text text-muted small flex-grow-1">{pkg.description}</p>
                <div className="d-flex justify-content-between align-items-center mt-3">
                  <strong>₹{pkg.price} · {pkg.durationMinutes} mins</strong>
                  <button className="btn btn-primary btn-sm" onClick={() => setSelectedPkg(pkg)}>
                    Select
                  </button>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {selectedPkg && (
        <div className="card mt-4 border-primary">
          <div className="card-header bg-primary text-white">
            Confirm Booking: {selectedPkg.title} (₹{selectedPkg.price})
          </div>
          <form className="card-body row g-3" onSubmit={handleBook}>
            <div className="col-md-4">
              <label className="form-label">Service Address</label>
              <select className="form-select" value={addressId} onChange={(e) => setAddressId(e.target.value)} required>
                <option value="">-- Choose Saved Address --</option>
                {addresses.map((a) => (
                  <option key={a.id} value={a.id}>{a.label} - {a.addressLine}, {a.city}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label">Date</label>
              <input type="date" className="form-control" value={scheduledDate} onChange={(e) => setScheduledDate(e.target.value)} required />
            </div>
            <div className="col-md-4">
              <label className="form-label">Time Slot</label>
              <select className="form-select" value={timeSlot} onChange={(e) => setTimeSlot(e.target.value)}>
                {TIME_SLOTS.map((slot) => <option key={slot} value={slot}>{slot}</option>)}
              </select>
            </div>
            <div className="col-12 d-flex gap-2">
              <button type="submit" className="btn btn-success" disabled={submitting}>
                {submitting ? "Booking..." : "Confirm Booking"}
              </button>
              <button type="button" className="btn btn-outline-secondary" onClick={() => setSelectedPkg(null)}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}