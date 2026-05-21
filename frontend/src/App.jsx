import { useMemo, useState } from "react";
import "./App.css";

const methods = ["CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "BANK_TRANSFER", "CASH"];
const categories = ["hotel_room", "salon_chair", "meeting_room", "car", "desk", "table"];

function App() {
  const [token, setToken] = useState(localStorage.getItem("token") || "");
  const [baseUrl, setBaseUrl] = useState("http://localhost:8080");
  const [activeTab, setActiveTab] = useState("auth");
  const [output, setOutput] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const authHeader = useMemo(
    () => (token ? { Authorization: `Bearer ${token}` } : {}),
    [token]
  );

  const handleError = (err) => {
    if (err?.name === "TypeError") {
      setError("Cannot reach API. Make sure backend is running and CORS is enabled.");
      return;
    }
    setError(err.message || "Request failed.");
  };

  const parseResponse = async (response) => {
    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
      ? await response.json()
      : await response.text();

    if (!response.ok) {
      const msg =
        typeof payload === "object"
          ? payload.message || payload.error || JSON.stringify(payload)
          : payload || `HTTP ${response.status}`;
      throw new Error(msg);
    }
    return payload;
  };

  const api = async (path, options = {}) => {
    setLoading(true);
    setError("");
    try {
      const response = await fetch(`${baseUrl}${path}`, {
        ...options,
        headers: {
          "Content-Type": "application/json",
          ...authHeader,
          ...(options.headers || {}),
        },
      });
      const data = await parseResponse(response);
      setOutput(data);
      return data;
    } catch (err) {
      handleError(err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const register = async (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const data = await api("/api/v1/auth/register", {
      method: "POST",
      body: JSON.stringify(Object.fromEntries(fd.entries())),
    });
    if (data.token) {
      localStorage.setItem("token", data.token);
      setToken(data.token);
    }
  };

  const login = async (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const data = await api("/api/v1/auth/login", {
      method: "POST",
      body: JSON.stringify(Object.fromEntries(fd.entries())),
    });
    if (data.token) {
      localStorage.setItem("token", data.token);
      setToken(data.token);
    }
  };

  const createResource = async (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const payload = Object.fromEntries(fd.entries());
    await api("/api/v1/resources", {
      method: "POST",
      body: JSON.stringify({
        ...payload,
        capacity: Number(payload.capacity),
        price: Number(payload.price),
        features: String(payload.features || "")
          .split(",")
          .map((x) => x.trim())
          .filter(Boolean),
      }),
    });
  };

  const createBooking = async (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const payload = Object.fromEntries(fd.entries());
    const body = {
      resourceId: Number(payload.resourceId),
      startTime: new Date(payload.startTime).toISOString(),
      endTime: new Date(payload.endTime).toISOString(),
      quantity: Number(payload.quantity || 1),
      notes: payload.notes || "",
      payment: {
        method: payload.method,
        amount: Number(payload.amount),
        cardNumber: payload.cardNumber || "",
        expiryDate: payload.expiryDate || "",
        cvv: payload.cvv || "",
      },
    };
    await api("/api/v1/bookings", { method: "POST", body: JSON.stringify(body) });
  };

  const getAvailability = async (e) => {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const startTime = new Date(fd.get("startTime")).toISOString();
    const endTime = new Date(fd.get("endTime")).toISOString();
    await api(
      `/api/v1/resources/availability?startTime=${encodeURIComponent(startTime)}&endTime=${encodeURIComponent(endTime)}`
    );
  };

  const getMe = async () => api("/api/v1/users/me");

  const logout = () => {
    localStorage.removeItem("token");
    setToken("");
    setOutput({ message: "Logged out." });
  };

  return (
    <div className="layout">
      <aside className="sidebar">
        <h1>Booking Frontend</h1>
        <p className="muted">Production-style UI for your API</p>
        <label>API URL</label>
        <input value={baseUrl} onChange={(e) => setBaseUrl(e.target.value)} />
        <div className="tokenBox">
          <small>JWT Status</small>
          <strong>{token ? "Authenticated" : "Not Authenticated"}</strong>
        </div>
        <nav>
          {["auth", "resources", "bookings", "profile"].map((tab) => (
            <button
              key={tab}
              className={activeTab === tab ? "nav active" : "nav"}
              onClick={() => setActiveTab(tab)}
            >
              {tab.toUpperCase()}
            </button>
          ))}
        </nav>
        <button className="ghost" onClick={logout}>
          Logout
        </button>
      </aside>

      <main className="main">
        {activeTab === "auth" && (
          <section className="panelGrid">
            <form className="panel" onSubmit={register}>
              <h2>Register</h2>
              <input name="fullName" placeholder="Full Name" required />
              <input name="email" placeholder="Email" type="email" required />
              <input name="password" placeholder="Password" type="password" required />
              <input name="phoneNumber" placeholder="+27..." />
              <button disabled={loading}>{loading ? "Please wait..." : "Register"}</button>
            </form>
            <form className="panel" onSubmit={login}>
              <h2>Login</h2>
              <input name="email" placeholder="Email" type="email" required />
              <input name="password" placeholder="Password" type="password" required />
              <button disabled={loading}>{loading ? "Please wait..." : "Login"}</button>
            </form>
          </section>
        )}

        {activeTab === "resources" && (
          <section className="panelGrid">
            <form className="panel" onSubmit={getAvailability}>
              <h2>Check Availability</h2>
              <label>Start Time</label>
              <input name="startTime" type="datetime-local" required />
              <label>End Time</label>
              <input name="endTime" type="datetime-local" required />
              <button disabled={loading}>Fetch Resources</button>
            </form>
            <form className="panel" onSubmit={createResource}>
              <h2>Create Resource</h2>
              <input name="resourceCode" placeholder="RESOURCE-002" required />
              <select name="category" required>
                {categories.map((category) => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
              <input name="location" placeholder="Executive Room" />
              <input name="capacity" placeholder="Capacity" type="number" min="1" required />
              <input name="price" placeholder="Price" type="number" min="1" step="0.01" required />
              <textarea name="description" placeholder="Description" />
              <input name="features" placeholder="Projector, Whiteboard" />
              <button disabled={loading}>Create Resource</button>
            </form>
          </section>
        )}

        {activeTab === "bookings" && (
          <section className="panelGrid">
            <form className="panel" onSubmit={createBooking}>
              <h2>Create Booking</h2>
              <input name="resourceId" type="number" placeholder="Resource ID" required />
              <label>Start Time</label>
              <input name="startTime" type="datetime-local" required />
              <label>End Time</label>
              <input name="endTime" type="datetime-local" required />
              <input name="quantity" type="number" min="1" max="10" defaultValue="1" />
              <textarea name="notes" placeholder="Notes" />
              <h3>Payment</h3>
              <select name="method" required>
                {methods.map((method) => (
                  <option key={method} value={method}>
                    {method}
                  </option>
                ))}
              </select>
              <input name="amount" type="number" min="1" step="0.01" placeholder="Amount" required />
              <input name="cardNumber" placeholder="Card Number (optional)" />
              <input name="expiryDate" placeholder="MM/YY (optional)" />
              <input name="cvv" placeholder="CVV (optional)" />
              <button disabled={loading}>Create Booking</button>
            </form>
            <div className="panel">
              <h2>Quick Actions</h2>
              <button onClick={() => api("/api/v1/bookings/user")}>My Bookings</button>
              <button onClick={getMe}>My Profile</button>
              <p className="muted small">
                Start/Complete/Cancel and refund can be added next with modal actions once your primary
                flow is confirmed.
              </p>
            </div>
          </section>
        )}

        {activeTab === "profile" && (
          <section className="panel">
            <h2>User Profile</h2>
            <p className="muted">Uses `GET /api/v1/users/me` with current JWT.</p>
            <button onClick={getMe}>Load Profile</button>
          </section>
        )}

        <section className="panel outputPanel">
          <h2>Response</h2>
          {error ? <div className="error">{error}</div> : null}
          <pre>{output ? JSON.stringify(output, null, 2) : "No response yet."}</pre>
        </section>
      </main>
    </div>
  );
}

export default App;
