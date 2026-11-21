import { useEffect, useState } from "react";
import { api, extractErrorMessage } from "../../api/client";

export default function AdminDashboard() {
  const [categories, setCategories] = useState([]);
  const [catForm, setCatForm] = useState({ name: "", description: "" });
  const [pkgForm, setPkgForm] = useState({ categoryId: "", title: "", description: "", price: "", durationMinutes: 60 });
  const [provForm, setProvForm] = useState({ fullName: "", email: "", phone: "", password: "" });
  const [msg, setMsg] = useState({ type: "", text: "" });

  const loadCategories = () => api.get("/catalog/categories").then((res) => {
    setCategories(res.data);
    if (res.data.length > 0 && !pkgForm.categoryId) {
      setPkgForm((p) => ({ ...p, categoryId: String(res.data[0].id) }));
    }
  });

  useEffect(() => { loadCategories(); }, []);

  const submitCategory = async (e) => {
    e.preventDefault();
    try {
      await api.post("/admin/catalog/categories", catForm);
      setCatForm({ name: "", description: "" });
      setMsg({ type: "success", text: "Category added." });
      loadCategories();
    } catch (err) {
      setMsg({ type: "danger", text: extractErrorMessage(err) });
    }
  };

  const submitPackage = async (e) => {
    e.preventDefault();
    try {
      await api.post("/admin/catalog/packages", {
        ...pkgForm,
        categoryId: Number(pkgForm.categoryId),
        price: Number(pkgForm.price),
        durationMinutes: Number(pkgForm.durationMinutes),
      });
      setPkgForm({ ...pkgForm, title: "", description: "", price: "" });
      setMsg({ type: "success", text: "Service package published." });
    } catch (err) {
      setMsg({ type: "danger", text: extractErrorMessage(err) });
    }
  };

  const submitProvider = async (e) => {
    e.preventDefault();
    try {
      await api.post("/admin/providers", provForm);
      setProvForm({ fullName: "", email: "", phone: "", password: "" });
      setMsg({ type: "success", text: "Provider account created." });
    } catch (err) {
      setMsg({ type: "danger", text: extractErrorMessage(err) });
    }
  };

  return (
    <div className="container py-4">
      <h2 className="mb-3">Admin Operations</h2>
      {msg.text && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}

      <div className="row g-4">
        <div className="col-md-4">
          <div className="card card-body">
            <h5>1. Add Service Category</h5>
            <form onSubmit={submitCategory}>
              <input className="form-control mb-2" placeholder="Category Name (e.g. Salon)" value={catForm.name} onChange={(e) => setCatForm({ ...catForm, name: e.target.value })} required />
              <textarea className="form-control mb-2" placeholder="Description" value={catForm.description} onChange={(e) => setCatForm({ ...catForm, description: e.target.value })} />
              <button className="btn btn-primary btn-sm">Create Category</button>
            </form>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card card-body">
            <h5>2. Add Service Package</h5>
            <form onSubmit={submitPackage}>
              <select className="form-select mb-2" value={pkgForm.categoryId} onChange={(e) => setPkgForm({ ...pkgForm, categoryId: e.target.value })} required>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
              <input className="form-control mb-2" placeholder="Package Title" value={pkgForm.title} onChange={(e) => setPkgForm({ ...pkgForm, title: e.target.value })} required />
              <input className="form-control mb-2" type="number" step="0.01" placeholder="Price (₹)" value={pkgForm.price} onChange={(e) => setPkgForm({ ...pkgForm, price: e.target.value })} required />
              <input className="form-control mb-2" type="number" placeholder="Duration (mins)" value={pkgForm.durationMinutes} onChange={(e) => setPkgForm({ ...pkgForm, durationMinutes: e.target.value })} required />
              <textarea className="form-control mb-2" placeholder="Included work details" value={pkgForm.description} onChange={(e) => setPkgForm({ ...pkgForm, description: e.target.value })} />
              <button className="btn btn-primary btn-sm">Publish Package</button>
            </form>
          </div>
        </div>

        <div className="col-md-4">
          <div className="card card-body">
            <h5>3. Onboard Service Provider</h5>
            <form onSubmit={submitProvider}>
              <input className="form-control mb-2" placeholder="Provider Full Name" value={provForm.fullName} onChange={(e) => setProvForm({ ...provForm, fullName: e.target.value })} required />
              <input className="form-control mb-2" type="email" placeholder="Provider Email" value={provForm.email} onChange={(e) => setProvForm({ ...provForm, email: e.target.value })} required />
              <input className="form-control mb-2" placeholder="Phone" value={provForm.phone} onChange={(e) => setProvForm({ ...provForm, phone: e.target.value })} required />
              <input className="form-control mb-2" type="password" minLength={8} placeholder="Temp Password" value={provForm.password} onChange={(e) => setProvForm({ ...provForm, password: e.target.value })} required />
              <button className="btn btn-success btn-sm">Create Provider</button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}