const updateMessages = [
  "Admissions portal opens for Fall 2026 on March 1.",
  "Faculty attendance review dashboard updated this week.",
  "AI assignment feedback now includes originality insights."
];

let updateIndex = 0;
const updateMessageEl = document.getElementById("liveUpdateMessage");
const updateTimeEl = document.getElementById("liveUpdateTime");

function refreshLiveUpdate() {
  if (!updateMessageEl || !updateTimeEl) {
    return;
  }

  updateMessageEl.textContent = updateMessages[updateIndex];
  updateTimeEl.textContent = new Date().toLocaleString();
  updateIndex = (updateIndex + 1) % updateMessages.length;
}

refreshLiveUpdate();
setInterval(refreshLiveUpdate, 7000);
