import { useEffect, useState } from "react";
import { api, extractErrorMessage } from "../../api/client";

export default function Addresses() {
  const [list, setList] = useState([]);
  const [form, setForm] = useState({ label: "Home", addressLine: "", city: "", pincode: "" });
  const [error, setError] = useState("");

  const load = () => api.get("/customer/addresses").then((res) => setList(res.data)).catch((err) => setError(extractErrorMessage(err)));
  useEffect(() => { load(); }, []);

  const addAddress = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await api.post("/customer/addresses", form);
      setForm({ label: "Home", addressLine: "", city: "", pincode: "" });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const remove = async (id) => {
    try {
      await api.delete(`/customer/addresses/${id}`);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="container py-4">
      <h2 className="mb-3">Saved Addresses</h2>
      {error && <div className="alert alert-danger">{error}</div>}
      <form className="card card-body mb-4 row g-2" onSubmit={addAddress}>
        <div className="col-md-2">
          <input className="form-control" placeholder="Label (Home)" value={form.label} onChange={(e) => setForm({ ...form, label: e.target.value })} required />
        </div>
        <div className="col-md-5">
          <input className="form-control" placeholder="Flat, Street, Landmark" value={form.addressLine} onChange={(e) => setForm({ ...form, addressLine: e.target.value })} required />
        </div>
        <div className="col-md-2">
          <input className="form-control" placeholder="City" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} required />
        </div>
        <div className="col-md-2">
          <input className="form-control" placeholder="Pincode" value={form.pincode} onChange={(e) => setForm({ ...form, pincode: e.target.value })} required />
        </div>
        <div className="col-md-1">
          <button className="btn btn-primary w-100" type="submit">Add</button>
        </div>
      </form>

      <div className="list-group">
        {list.map((a) => (
          <div key={a.id} className="list-group-item d-flex justify-content-between align-items-center">
            <div>
              <strong>{a.label}</strong> — {a.addressLine}, {a.city} ({a.pincode})
            </div>
            <button className="btn btn-sm btn-outline-danger" onClick={() => remove(a.id)}>Delete</button>
          </div>
        ))}
      </div>
    </div>
  );
}