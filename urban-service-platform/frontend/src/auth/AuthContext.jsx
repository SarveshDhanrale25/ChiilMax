import { createContext, useContext, useEffect, useState } from "react";
import { api, loginWithPassword, logoutSession, refreshCsrf } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        await refreshCsrf();
        const { data } = await api.get("/auth/me");
        if (active) setUser(data);
      } catch {
        if (active) setUser(null);
      } finally {
        if (active) setInitializing(false);
      }
    })();
    return () => { active = false; };
  }, []);

  const login = async (email, password) => {
    const account = await loginWithPassword(email, password);
    setUser(account);
    return account;
  };

  const logout = async () => {
    await logoutSession();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, initializing, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);