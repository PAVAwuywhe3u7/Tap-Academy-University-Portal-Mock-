if (!requireRole(["ADMIN"])) {
  throw new Error("Unauthorized");
}

attachLogout();

document.getElementById("adminName").textContent = authStore.name || "Admin";

const adminSections = ["overviewSection", "usersSection", "coursesSection", "attendanceSection"];
activateSidebar("adminNav", (target) => {
  adminSections.forEach((id) => document.getElementById(id).classList.add("d-none"));
  document.getElementById(target).classList.remove("d-none");

  if (target === "usersSection") {
    loadUsers();
  }
  if (target === "coursesSection") {
    loadCourses();
  }
  if (target === "attendanceSection") {
    syncReportCourseOptions();
  }
});

let cachedCourses = [];

async function loadStats() {
  const body = await authFetch("/api/admin/stats");
  const stats = body.data;
  document.getElementById("totalUsers").textContent = stats.totalUsers;
  document.getElementById("totalStudents").textContent = stats.totalStudents;
  document.getElementById("totalFaculty").textContent = stats.totalFaculty;
  document.getElementById("totalAdmins").textContent = stats.totalAdmins;
  document.getElementById("totalCourses").textContent = stats.totalCourses;
  document.getElementById("totalAssignments").textContent = stats.totalAssignments;
}

async function loadUsers() {
  const body = await authFetch("/api/admin/users");
  const users = body.data || [];
  const tableBody = document.getElementById("usersBody");

  tableBody.innerHTML = users.map((user) => `
    <tr>
      <td>${user.name}</td>
      <td>${user.email}</td>
      <td><span class="badge text-bg-light">${user.role}</span></td>
      <td>${user.enabled ? "Active" : "Disabled"}</td>
      <td><button class="btn btn-sm btn-outline-danger" data-user-id="${user.id}">Delete</button></td>
    </tr>
  `).join("");

  if (users.length === 0) {
    tableBody.innerHTML = "<tr><td colspan='5' class='text-center text-secondary'>No users found.</td></tr>";
  }

  tableBody.querySelectorAll("button[data-user-id]").forEach((button) => {
    button.addEventListener("click", async () => {
      const id = button.getAttribute("data-user-id");
      if (!confirm("Delete this user?")) {
        return;
      }

      try {
        await authFetch(`/api/admin/users/${id}`, { method: "DELETE" });
        await loadUsers();
        await loadStats();
      } catch (err) {
        alert(err.message);
      }
    });
  });
}

async function loadCourses() {
  const body = await authFetch("/api/admin/courses");
  const courses = body.data || [];
  cachedCourses = courses;
  const tableBody = document.getElementById("coursesBody");

  tableBody.innerHTML = courses.map((course) => `
    <tr>
      <td>${course.code}</td>
      <td>${course.title}</td>
      <td>${course.department}</td>
      <td>${course.facultyName}</td>
      <td><button class="btn btn-sm btn-outline-danger" data-course-id="${course.id}">Delete</button></td>
    </tr>
  `).join("");

  if (courses.length === 0) {
    tableBody.innerHTML = "<tr><td colspan='5' class='text-center text-secondary'>No courses found.</td></tr>";
  }

  tableBody.querySelectorAll("button[data-course-id]").forEach((button) => {
    button.addEventListener("click", async () => {
      const id = button.getAttribute("data-course-id");
      if (!confirm("Delete this course?")) {
        return;
      }

      try {
        await authFetch(`/api/admin/courses/${id}`, { method: "DELETE" });
        await loadCourses();
        await loadStats();
      } catch (err) {
        alert(err.message);
      }
    });
  });

  syncReportCourseOptions();
}

function syncReportCourseOptions() {
  const select = document.getElementById("reportClass");
  if (!select) {
    return;
  }

  select.innerHTML = "<option value=''>All</option>";
  cachedCourses.forEach((course) => {
    const option = document.createElement("option");
    option.value = course.code;
    option.textContent = `${course.code} - ${course.title}`;
    select.appendChild(option);
  });
}

document.getElementById("saveUserBtn")?.addEventListener("click", async () => {
  const payload = {
    name: document.getElementById("userName").value.trim(),
    email: document.getElementById("userEmail").value.trim(),
    password: document.getElementById("userPassword").value,
    role: document.getElementById("userRole").value
  };

  try {
    await authFetch("/api/admin/users", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    document.getElementById("userForm").reset();
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById("userModal"));
    modal.hide();
    await loadUsers();
    await loadStats();
  } catch (err) {
    alert(err.message);
  }
});

document.getElementById("saveCourseBtn")?.addEventListener("click", async () => {
  const payload = {
    code: document.getElementById("courseCode").value.trim(),
    title: document.getElementById("courseTitle").value.trim(),
    department: document.getElementById("courseDepartment").value.trim(),
    facultyName: document.getElementById("courseFaculty").value.trim(),
    active: true
  };

  try {
    await authFetch("/api/admin/courses", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    document.getElementById("courseForm").reset();
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById("courseModal"));
    modal.hide();
    await loadCourses();
    await loadStats();
  } catch (err) {
    alert(err.message);
  }
});

document.getElementById("attendanceReportForm")?.addEventListener("submit", async (event) => {
  event.preventDefault();
  const className = document.getElementById("reportClass").value;
  const startDate = document.getElementById("startDate").value;
  const endDate = document.getElementById("endDate").value;

  const query = new URLSearchParams();
  if (className) {
    query.append("className", className);
  }
  query.append("startDate", startDate);
  query.append("endDate", endDate);

  try {
    const body = await authFetch(`/api/attendance/report?${query.toString()}`);
    const rows = body.data || [];
    const tableBody = document.getElementById("attendanceReportBody");
    tableBody.innerHTML = rows.map((row) => `
      <tr>
        <td>${row.studentName}</td>
        <td>${row.className}</td>
        <td>${row.totalClasses}</td>
        <td>${row.presentClasses}</td>
        <td>${row.attendancePercentage}%</td>
      </tr>
    `).join("");

    if (rows.length === 0) {
      tableBody.innerHTML = "<tr><td colspan='5' class='text-center text-secondary'>No report data available.</td></tr>";
    }
  } catch (err) {
    document.getElementById("attendanceReportBody").innerHTML = `<tr><td colspan='5' class='text-danger text-center'>${err.message}</td></tr>`;
  }
});

document.getElementById("exportBtn")?.addEventListener("click", () => {
  const rows = Array.from(document.querySelectorAll("#attendanceReportBody tr"));
  if (rows.length === 0) {
    alert("No data to export");
    return;
  }

  const header = "Student,Class,Total,Present,Percentage";
  const lines = rows.map((row) => Array.from(row.children).map((cell) => `"${cell.textContent.trim().replace(/"/g, "'")}"`).join(","));
  const csv = [header, ...lines].join("\n");

  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = "attendance-report.csv";
  link.click();
});

(async function initAdminDashboard() {
  const today = new Date();
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
  document.getElementById("startDate").value = firstDay.toISOString().split("T")[0];
  document.getElementById("endDate").value = today.toISOString().split("T")[0];

  try {
    await loadStats();
    await loadUsers();
    await loadCourses();
  } catch (err) {
    console.error(err);
  }
})();
