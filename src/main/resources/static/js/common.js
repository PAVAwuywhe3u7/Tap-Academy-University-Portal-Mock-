const authStore = {
  get token() {
    return localStorage.getItem("token");
  },
  get role() {
    return localStorage.getItem("userRole");
  },
  get name() {
    return localStorage.getItem("userName");
  },
  setSession(payload) {
    localStorage.setItem("token", payload.token);
    localStorage.setItem("userRole", payload.role);
    localStorage.setItem("userName", payload.name);
    localStorage.setItem("userId", String(payload.userId));
  },
  clear() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    localStorage.removeItem("userName");
    localStorage.removeItem("userId");
  }
};

function parseApiError(responseBody, fallbackMessage) {
  if (!responseBody) {
    return fallbackMessage;
  }
  if (typeof responseBody.message === "string" && responseBody.message.trim().length > 0) {
    return responseBody.message;
  }
  return fallbackMessage;
}

function setButtonLoading(buttonId, spinnerId, loading) {
  const button = document.getElementById(buttonId);
  const spinner = document.getElementById(spinnerId);
  if (!button || !spinner) {
    return;
  }

  const label = button.querySelector(".btn-label");
  button.disabled = loading;
  if (label) {
    label.classList.toggle("d-none", loading);
  }
  spinner.classList.toggle("d-none", !loading);
}

async function authFetch(url, options = {}) {
  const headers = options.headers ? { ...options.headers } : {};
  if (authStore.token) {
    headers.Authorization = `Bearer ${authStore.token}`;
  }

  const response = await fetch(url, {
    ...options,
    headers
  });

  let body = null;
  try {
    body = await response.json();
  } catch (error) {
    body = null;
  }

  if (!response.ok) {
    throw new Error(parseApiError(body, "Request failed"));
  }

  return body;
}

function requireRole(allowedRoles) {
  if (!authStore.token || !allowedRoles.includes(authStore.role)) {
    window.location.href = "/login-page";
    return false;
  }
  return true;
}

function attachLogout(buttonId = "logoutBtn") {
  const btn = document.getElementById(buttonId);
  if (!btn) {
    return;
  }
  btn.addEventListener("click", () => {
    authStore.clear();
    window.location.href = "/login-page";
  });
}

function activateSidebar(navId, callback) {
  const nav = document.getElementById(navId);
  if (!nav) {
    return;
  }

  const links = nav.querySelectorAll(".nav-link");
  links.forEach((link) => {
    link.addEventListener("click", (event) => {
      event.preventDefault();
      links.forEach((item) => item.classList.remove("active"));
      link.classList.add("active");
      const target = link.getAttribute("data-target");
      callback(target);
    });
  });
}
