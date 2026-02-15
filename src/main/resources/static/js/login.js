const loginForm = document.getElementById("loginForm");
const loginError = document.getElementById("loginError");

if (authStore.token && authStore.role) {
  const directPath = authStore.role === "ADMIN" ? "/admin-dashboard" : authStore.role === "FACULTY" ? "/faculty-dashboard" : "/student-dashboard";
  window.location.href = directPath;
}

loginForm?.addEventListener("submit", async (event) => {
  event.preventDefault();
  loginError.classList.add("d-none");

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  setButtonLoading("loginBtn", "loginSpinner", true);
  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ email, password })
    });

    const body = await response.json();
    if (!response.ok || !body.success) {
      throw new Error(parseApiError(body, "Invalid credentials"));
    }

    authStore.setSession(body.data);
    window.location.href = body.data.redirectUrl || "/student-dashboard";
  } catch (error) {
    loginError.textContent = error.message;
    loginError.classList.remove("d-none");
  } finally {
    setButtonLoading("loginBtn", "loginSpinner", false);
  }
});
