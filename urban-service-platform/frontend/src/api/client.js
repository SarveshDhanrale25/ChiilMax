import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  withCredentials: true,
  timeout: 15000,
});

let csrfToken;
let csrfPromise;

export async function refreshCsrf() {
  csrfToken = undefined;
  if (!csrfPromise) {
    csrfPromise = api
      .get("/auth/csrf")
      .then(({ data }) => {
        csrfToken = data.token;
        return csrfToken;
      })
      .finally(() => {
        csrfPromise = undefined;
      });
  }
  return csrfPromise;
}

api.interceptors.request.use(async (config) => {
  const method = config.method?.toLowerCase();
  if (["post", "put", "patch", "delete"].includes(method)) {
    if (!csrfToken) {
      await refreshCsrf();
    }
    config.headers["X-XSRF-TOKEN"] = csrfToken;
  }
  return config;
});

export function extractErrorMessage(error) {
  return (
    error.response?.data?.message ||
    error.message ||
    "Something went wrong. Please try again."
  );
}

export async function loginWithPassword(email, password) {
  await refreshCsrf();
  await api.post("/auth/login", new URLSearchParams({ email, password }), {
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
  });
  await refreshCsrf();
  const { data } = await api.get("/auth/me");
  return data;
}

export async function logoutSession() {
  await api.post("/auth/logout");
  await refreshCsrf();
}