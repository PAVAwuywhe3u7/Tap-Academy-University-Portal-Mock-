if (!requireRole(["STUDENT"])) {
  throw new Error("Unauthorized");
}

attachLogout();

document.getElementById("studentName").textContent = authStore.name || "Student";

const sections = ["overviewSection", "attendanceSection", "assignmentsSection", "submitSection"];
activateSidebar("studentNav", (target) => {
  sections.forEach((id) => document.getElementById(id).classList.add("d-none"));
  document.getElementById(target).classList.remove("d-none");
  if (target === "attendanceSection") {
    loadAttendance();
  }
  if (target === "assignmentsSection") {
    loadAssignments();
  }
});

let currentUserId = Number(localStorage.getItem("userId")) || null;

async function loadCurrentUser() {
  const body = await authFetch("/api/auth/me");
  currentUserId = body.data.id;
  localStorage.setItem("userId", String(currentUserId));
}

async function loadOverview() {
  const body = await authFetch("/api/student/dashboard");
  const dashboard = body.data;
  document.getElementById("attendancePercent").textContent = `${dashboard.attendancePercentage}%`;
  document.getElementById("submittedCount").textContent = dashboard.submittedAssignments;
  document.getElementById("averageGrade").textContent = dashboard.averageGrade;
}

async function loadAttendance() {
  if (!currentUserId) {
    await loadCurrentUser();
  }

  const loading = document.getElementById("attendanceLoading");
  const tableWrap = document.getElementById("attendanceTableWrap");
  const tbody = document.getElementById("attendanceTableBody");
  loading.classList.remove("d-none");
  tableWrap.classList.add("d-none");

  try {
    const body = await authFetch(`/api/attendance/student/${currentUserId}`);
    const records = body.data || [];
    tbody.innerHTML = records.map((record) => `
      <tr>
        <td>${record.className}</td>
        <td>${record.date}</td>
        <td><span class="status-badge ${record.status === "PRESENT" ? "status-present" : "status-absent"}">${record.status}</span></td>
      </tr>
    `).join("");

    if (records.length === 0) {
      tbody.innerHTML = "<tr><td colspan='3' class='text-center text-secondary'>No attendance records found.</td></tr>";
    }
  } catch (error) {
    tbody.innerHTML = `<tr><td colspan='3' class='text-danger text-center'>${error.message}</td></tr>`;
  } finally {
    loading.classList.add("d-none");
    tableWrap.classList.remove("d-none");
  }
}

async function loadAssignments() {
  if (!currentUserId) {
    await loadCurrentUser();
  }

  const wrapper = document.getElementById("assignmentCards");
  wrapper.innerHTML = "<div class='loading-spinner'><span class='spinner-border text-primary'></span>Loading assignments...</div>";

  try {
    const body = await authFetch(`/api/assignments/student/${currentUserId}`);
    const assignments = body.data || [];
    if (assignments.length === 0) {
      wrapper.innerHTML = "<p class='text-secondary'>No assignments submitted yet.</p>";
      return;
    }

    wrapper.innerHTML = assignments.map((item) => {
      const gradeClass = item.grade === "A" ? "grade-a" : item.grade === "B" ? "grade-b" : "grade-c";
      const gradeBadge = item.grade ? `<span class='grade-badge ${gradeClass}'>${item.grade}</span>` : "<span class='badge text-bg-secondary'>Pending</span>";
      const submittedAt = item.submissionDate ? item.submissionDate.replace("T", " ") : "-";
      const assignmentTitle = item.assignmentTitle ? item.assignmentTitle : "General Submission";
      return `
      <div class='col-md-6'>
        <div class='card-feedback p-3 h-100'>
          <div class='d-flex justify-content-between align-items-center mb-2'>
            <h6 class='mb-0'>${item.course}</h6>
            ${gradeBadge}
          </div>
          <p class='small mb-1'><strong>Assignment:</strong> ${assignmentTitle}</p>
          <p class='small text-secondary mb-2'>File: ${item.originalFileName}</p>
          <p class='small text-secondary mb-2'>Submitted: ${submittedAt}</p>
          <p class='small mb-2'>${item.feedback || "Feedback pending"}</p>
          <p class='small mb-0'><strong>Scores:</strong> Content ${item.contentScore}, Grammar ${item.grammarScore}, Structure ${item.structureScore}, Originality ${item.originalityScore}</p>
        </div>
      </div>`;
    }).join("");
  } catch (error) {
    wrapper.innerHTML = `<p class='text-danger'>${error.message}</p>`;
  }
}

async function loadCourseOptions() {
  const courseSelect = document.getElementById("course");
  courseSelect.innerHTML = "<option value=''>Select a course</option>";

  try {
    const body = await fetch("/api/public/courses");
    const payload = await body.json();
    const courses = payload.data || [];
    courses.forEach((course) => {
      const option = document.createElement("option");
      option.value = course.code;
      option.textContent = `${course.code} - ${course.title}`;
      courseSelect.appendChild(option);
    });
  } catch (error) {
    courseSelect.innerHTML = "<option value=''>No courses available</option>";
  }
}

document.getElementById("assignmentForm")?.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!currentUserId) {
    await loadCurrentUser();
  }

  document.getElementById("submitError").classList.add("d-none");
  document.getElementById("submitSuccess").classList.add("d-none");

  const course = document.getElementById("course").value;
  const assignmentTitle = document.getElementById("assignmentTitle").value.trim();
  const file = document.getElementById("file").files[0];

  if (!course || !file) {
    document.getElementById("submitError").textContent = "Course and file are required.";
    document.getElementById("submitError").classList.remove("d-none");
    return;
  }

  const formData = new FormData();
  formData.append("studentId", String(currentUserId));
  formData.append("course", course);
  formData.append("assignmentTitle", assignmentTitle);
  formData.append("file", file);

  setButtonLoading("submitBtn", "submitSpinner", true);
  try {
    await authFetch("/api/assignments/submit", {
      method: "POST",
      body: formData
    });

    document.getElementById("submitSuccess").textContent = "Assignment submitted successfully.";
    document.getElementById("submitSuccess").classList.remove("d-none");
    document.getElementById("assignmentForm").reset();
    await loadOverview();
    await loadAssignments();
  } catch (error) {
    document.getElementById("submitError").textContent = error.message;
    document.getElementById("submitError").classList.remove("d-none");
  } finally {
    setButtonLoading("submitBtn", "submitSpinner", false);
  }
});

(async function initStudentDashboard() {
  try {
    await loadCurrentUser();
    await loadOverview();
    await loadCourseOptions();
    await loadAttendance();
    await loadAssignments();
  } catch (error) {
    console.error(error);
  }
})();
