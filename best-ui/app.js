const state = {
  token: localStorage.getItem("booking_token") || "",
  lastReference: "",
};

const el = (id) => document.getElementById(id);
const logOutput = el("logOutput");
const meOutput = el("meOutput");

const toIso = (value) => (value ? new Date(value).toISOString() : null);
const parseFeatures = (raw) => raw.split(",").map((x) => x.trim()).filter(Boolean);

function setToken(token) {
  state.token = token || "";
  localStorage.setItem("booking_token", state.token);
}

function getBaseUrl() {
  return el("baseUrl").value.trim().replace(/\/$/, "");
}

function writeLog(title, data) {
  const block = `\n=== ${title} ===\n${JSON.stringify(data, null, 2)}\n`;
  logOutput.textContent = `${block}${logOutput.textContent}`.slice(0, 14000);
}

async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;

  const response = await fetch(`${getBaseUrl()}${path}`, { ...options, headers });
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw { status: response.status, data };
  }
  return data;
}

function attachFormSubmit(formId, handler) {
  el(formId).addEventListener("submit", async (event) => {
    event.preventDefault();
    const fd = new FormData(event.currentTarget);
    const payload = Object.fromEntries(fd.entries());
    try {
      const result = await handler(payload);
      writeLog(formId, result);
    } catch (error) {
      writeLog(`${formId} ERROR`, error);
    }
  });
}

attachFormSubmit("registerForm", (payload) =>
  api("/api/v1/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  }).then((res) => {
    setToken(res.token);
    return res;
  })
);

attachFormSubmit("loginForm", (payload) =>
  api("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  }).then((res) => {
    setToken(res.token);
    return res;
  })
);

el("meBtn").addEventListener("click", async () => {
  try {
    const data = await api("/api/v1/users/me");
    meOutput.textContent = JSON.stringify(data, null, 2);
    writeLog("users/me", data);
  } catch (error) {
    writeLog("users/me ERROR", error);
  }
});

attachFormSubmit("availabilityForm", ({ startTime, endTime }) =>
  api(
    `/api/v1/resources/availability?startTime=${encodeURIComponent(toIso(startTime))}&endTime=${encodeURIComponent(toIso(endTime))}`
  )
);

attachFormSubmit("createResourceForm", (payload) =>
  api("/api/v1/resources", {
    method: "POST",
    body: JSON.stringify({
      resourceCode: payload.resourceCode,
      category: payload.category,
      location: payload.location,
      capacity: Number(payload.capacity),
      price: Number(payload.price),
      description: payload.description,
      features: parseFeatures(payload.features || ""),
    }),
  })
);

attachFormSubmit("createBookingForm", (payload) =>
  api("/api/v1/bookings", {
    method: "POST",
    body: JSON.stringify({
      resourceId: Number(payload.resourceId),
      startTime: toIso(payload.startTime),
      endTime: toIso(payload.endTime),
      quantity: payload.quantity ? Number(payload.quantity) : undefined,
      notes: payload.notes || "",
      payment: {
        method: payload.method,
        amount: Number(payload.amount),
        cardNumber: payload.cardNumber || "",
        expiryDate: payload.expiryDate || "",
        cvv: payload.cvv || "",
      },
    }),
  }).then((res) => {
    state.lastReference = res.bookingReference || "";
    return res;
  })
);

attachFormSubmit("bookingByRefForm", ({ reference }) =>
  api(`/api/v1/bookings/${encodeURIComponent(reference)}`).then((res) => {
    state.lastReference = reference;
    return res;
  })
);

el("myBookingsBtn").addEventListener("click", async () => {
  try {
    const data = await api("/api/v1/bookings/user");
    writeLog("bookings/user", data);
  } catch (error) {
    writeLog("bookings/user ERROR", error);
  }
});

el("startBookingBtn").addEventListener("click", async () => {
  const reference = prompt("Booking reference to START", state.lastReference);
  if (!reference) return;
  try {
    const data = await api(`/api/v1/bookings/${encodeURIComponent(reference)}/start`, { method: "POST" });
    state.lastReference = reference;
    writeLog("booking/start", data);
  } catch (error) {
    writeLog("booking/start ERROR", error);
  }
});

el("completeBookingBtn").addEventListener("click", async () => {
  const reference = prompt("Booking reference to COMPLETE", state.lastReference);
  if (!reference) return;
  try {
    const data = await api(`/api/v1/bookings/${encodeURIComponent(reference)}/complete`, { method: "POST" });
    state.lastReference = reference;
    writeLog("booking/complete", data);
  } catch (error) {
    writeLog("booking/complete ERROR", error);
  }
});

el("cancelBookingBtn").addEventListener("click", async () => {
  const reference = prompt("Booking reference to CANCEL", state.lastReference);
  if (!reference) return;
  try {
    const data = await api(`/api/v1/bookings/${encodeURIComponent(reference)}`, { method: "DELETE" });
    state.lastReference = reference;
    writeLog("booking/cancel", data || { ok: true, status: 204 });
  } catch (error) {
    writeLog("booking/cancel ERROR", error);
  }
});

attachFormSubmit("processPaymentForm", (payload) =>
  api(`/api/v1/payments/process/${payload.bookingId}`, {
    method: "POST",
    body: JSON.stringify({
      method: payload.method,
      amount: Number(payload.amount),
      cardNumber: payload.cardNumber || "",
      expiryDate: payload.expiryDate || "",
      cvv: payload.cvv || "",
    }),
  })
);

attachFormSubmit("refundForm", ({ bookingId }) =>
  api(`/api/v1/payments/refund/${bookingId}`, { method: "POST" })
);

el("logoutBtn").addEventListener("click", () => {
  setToken("");
  writeLog("logout", { ok: true });
});

if (state.token) {
  writeLog("session", { message: "Restored token from localStorage" });
}
