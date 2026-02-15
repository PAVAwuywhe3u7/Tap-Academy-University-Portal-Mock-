if (!requireRole(["FACULTY", "ADMIN"])) {
  throw new Error("Unauthorized");
}

attachLogout();

document.getElementById("facultyName").textContent = authStore.name || "Faculty";

const facultySections = ["overviewSection", "attendanceSection", "assignmentSection", "reportsSection"];
activateSidebar("facultyNav", (target) => {
  facultySections.forEach((id) => document.getElementById(id).classList.add("d-none"));
  document.getElementById(target).classList.remove("d-none");
});

async function loadDashboardOverview() {
  const body = await authFetch("/api/faculty/dashboard");
  document.getElementById("totalCourses").textContent = body.data.totalCourses;
  document.getElementById("totalStudents").textContent = body.data.totalStudents;
  document.getElementById("pendingEvaluations").textContent = body.data.pendingEvaluations;
}

async function loadCourseSelects() {
  const response = await fetch("/api/public/courses");
  const payload = await response.json();
  const courses = payload.data || [];

  const selectors = [
    document.getElementById("className"),
    document.getElementById("assignmentCourse"),
    document.getElementById("reportClass")
  ];

  selectors.forEach((select) => {
    if (!select) {
      return;
    }
    const firstOption = select.id === "assignmentCourse"
      ? "<option value=''>Select course for review</option>"
      : "<option value=''>Select class</option>";
    select.innerHTML = firstOption;
    courses.forEach((course) => {
      const option = document.createElement("option");
      option.value = course.code;
      option.textContent = `${course.code} - ${course.title}`;
      select.appendChild(option);
    });
  });
}

let loadedStudents = [];

document.getElementById("attendanceForm")?.addEventListener("submit", async (event) => {
  event.preventDefault();
  const className = document.getElementById("className").value;
  const date = document.getElementById("attendanceDate").value;

  const success = document.getElementById("attendanceSuccess");
  const error = document.getElementById("attendanceError");
  success.classList.add("d-none");
  error.classList.add("d-none");

  if (!className || !date) {
    error.textContent = "Class and date are required.";
    error.classList.remove("d-none");
    return;
  }

  try {
    const studentsBody = await authFetch(`/api/attendance/faculty/students?className=${encodeURIComponent(className)}`);
    const existingBody = await authFetch(`/api/attendance/filter?className=${encodeURIComponent(className)}&date=${date}`);

    loadedStudents = studentsBody.data || [];
    const existingMap = new Map((existingBody.data || []).map((item) => [item.studentId, item.status]));

    const tbody = document.getElementById("studentsTableBody");
    tbody.innerHTML = loadedStudents.map((student) => {
      const selectedStatus = existingMap.get(student.id) || "PRESENT";
      return `
      <tr>
        <td>${student.name}</td>
        <td>${student.email}</td>
        <td>
          <select class="form-select form-select-sm attendance-status" data-student-id="${student.id}">
            <option value="PRESENT" ${selectedStatus === "PRESENT" ? "selected" : ""}>Present</option>
            <option value="ABSENT" ${selectedStatus === "ABSENT" ? "selected" : ""}>Absent</option>
          </select>
        </td>
      </tr>`;
    }).join("");

    document.getElementById("studentsTableWrap").classList.remove("d-none");
  } catch (err) {
    error.textContent = err.message;
    error.classList.remove("d-none");
  }
});

document.getElementById("saveAttendanceBtn")?.addEventListener("click", async () => {
  const className = document.getElementById("className").value;
  const date = document.getElementById("attendanceDate").value;

  const success = document.getElementById("attendanceSuccess");
  const error = document.getElementById("attendanceError");
  success.classList.add("d-none");
  error.classList.add("d-none");

  try {
    const records = Array.from(document.querySelectorAll(".attendance-status")).map((select) => ({
      studentId: Number(select.getAttribute("data-student-id")),
      status: select.value
    }));

    await authFetch("/api/attendance/mark-batch", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ className, date, records })
    });

    success.textContent = `Attendance saved for ${records.length} students.`;
    success.classList.remove("d-none");
  } catch (err) {
    error.textContent = err.message;
    error.classList.remove("d-none");
  }
});

document.getElementById("assignmentFilterForm")?.addEventListener("submit", async (event) => {
  event.preventDefault();
  const course = document.getElementById("assignmentCourse").value;
  const wrapper = document.getElementById("assignmentReviewList");

  if (!course) {
    wrapper.innerHTML = "<p class='text-secondary'>Select a course to load assignments.</p>";
    return;
  }

  wrapper.innerHTML = "<div class='loading-spinner'><span class='spinner-border text-primary'></span>Loading assignments...</div>";

  try {
    const body = await authFetch(`/api/assignments/course/${encodeURIComponent(course)}`);
    const assignments = body.data || [];

    if (assignments.length === 0) {
      wrapper.innerHTML = "<p class='text-secondary'>No assignments submitted for this course.</p>";
      return;
    }

    wrapper.innerHTML = assignments.map((item) => {
      const grade = item.grade || "";
      const assignmentTitle = item.assignmentTitle || "General Submission";
      return `
      <div class='col-md-6'>
        <div class='card-feedback p-3 h-100'>
          <h6>${item.studentName} - ${item.course}</h6>
          <p class='small mb-1'><strong>Assignment:</strong> ${assignmentTitle}</p>
          <p class='small text-secondary mb-2'>File: ${item.originalFileName}</p>
          <p class='small mb-2'>${item.feedback || "No feedback yet"}</p>
          <div class='mb-2'><label class='small'>Grade (A/B/C)</label><input class='form-control form-control-sm grade-input' data-id='${item.id}' value='${grade}' maxlength='1'></div>
          <div class='mb-2'><label class='small'>Feedback</label><textarea class='form-control form-control-sm feedback-input' data-id='${item.id}' rows='2'>${item.feedback || ""}</textarea></div>
          <button class='btn btn-sm btn-accent save-grade-btn' data-id='${item.id}'>Save Grade</button>
        </div>
      </div>`;
    }).join("");

    bindGradeButtons();
  } catch (err) {
    wrapper.innerHTML = `<p class='text-danger'>${err.message}</p>`;
  }
});

function bindGradeButtons() {
  document.querySelectorAll(".save-grade-btn").forEach((button) => {
    button.addEventListener("click", async () => {
      const id = button.getAttribute("data-id");
      const gradeInput = document.querySelector(`.grade-input[data-id='${id}']`);
      const feedbackInput = document.querySelector(`.feedback-input[data-id='${id}']`);

      const gradeValue = gradeInput.value.trim().toUpperCase();
      if (!["A", "B", "C"].includes(gradeValue)) {
        alert("Grade must be A, B, or C");
        return;
      }

      try {
        await authFetch(`/api/assignments/${id}/grade`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ grade: gradeValue, feedback: feedbackInput.value.trim() })
        });
        alert("Grade updated successfully");
      } catch (err) {
        alert(err.message);
      }
    });
  });
}

document.getElementById("reportForm")?.addEventListener("submit", async (event) => {
  event.preventDefault();
  const className = document.getElementById("reportClass").value;
  const startDate = document.getElementById("reportStartDate").value;
  const endDate = document.getElementById("reportEndDate").value;

  if (!startDate || !endDate) {
    return;
  }

  try {
    const params = new URLSearchParams();
    if (className) {
      params.append("className", className);
    }
    params.append("startDate", startDate);
    params.append("endDate", endDate);
    const body = await authFetch(`/api/attendance/report?${params.toString()}`);
    const rows = body.data || [];
    const tableBody = document.getElementById("reportBody");
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
      tableBody.innerHTML = "<tr><td colspan='5' class='text-center text-secondary'>No records for selected range.</td></tr>";
    }
  } catch (err) {
    document.getElementById("reportBody").innerHTML = `<tr><td colspan='5' class='text-danger text-center'>${err.message}</td></tr>`;
  }
});

(async function initFacultyDashboard() {
  const today = new Date();
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
  document.getElementById("attendanceDate").value = today.toISOString().split("T")[0];
  document.getElementById("reportStartDate").value = firstDay.toISOString().split("T")[0];
  document.getElementById("reportEndDate").value = today.toISOString().split("T")[0];

  try {
    await loadDashboardOverview();
    await loadCourseSelects();
  } catch (err) {
    console.error(err);
  }
})();
