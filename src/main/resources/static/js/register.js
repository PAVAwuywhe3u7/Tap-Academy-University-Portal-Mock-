const registerForm = document.getElementById("registerForm");
const registerError = document.getElementById("registerError");
const registerSuccess = document.getElementById("registerSuccess");

registerForm?.addEventListener("submit", async (event) => {
  event.preventDefault();
  registerError.classList.add("d-none");
  registerSuccess.classList.add("d-none");

  const payload = {
    name: document.getElementById("name").value.trim(),
    email: document.getElementById("email").value.trim(),
    password: document.getElementById("password").value,
    role: document.getElementById("role").value
  };

  setButtonLoading("registerBtn", "registerSpinner", true);
  try {
    const response = await fetch("/api/auth/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    const body = await response.json();
    if (!response.ok || !body.success) {
      throw new Error(parseApiError(body, "Registration failed"));
    }

    registerSuccess.textContent = "Registration complete. Redirecting to login...";
    registerSuccess.classList.remove("d-none");
    registerForm.reset();
    setTimeout(() => {
      window.location.href = "/login-page";
    }, 1200);
  } catch (error) {
    registerError.textContent = error.message;
    registerError.classList.remove("d-none");
  } finally {
    setButtonLoading("registerBtn", "registerSpinner", false);
  }
});
