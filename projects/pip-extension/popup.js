const toggle = document.getElementById("togglePip");
const badge = document.getElementById("statusBadge");

function updateBadge(enabled) {
  if (enabled) {
    badge.textContent = "Auto-PiP is ON";
    badge.className = "status-badge";
  } else {
    badge.textContent = "Auto-PiP is OFF";
    badge.className = "status-badge off";
  }
}

// Load saved preference
chrome.storage.sync.get({ autopipEnabled: true }, (data) => {
  toggle.checked = data.autopipEnabled;
  updateBadge(data.autopipEnabled);
});

// Save preference and notify all tabs on toggle
toggle.addEventListener("change", () => {
  const enabled = toggle.checked;
  chrome.storage.sync.set({ autopipEnabled: enabled });
  updateBadge(enabled);

  // Notify all existing content scripts
  chrome.tabs.query({}, (tabs) => {
    tabs.forEach((tab) => {
      chrome.tabs.sendMessage(tab.id, { type: "SET_PIP_ENABLED", value: enabled }).catch(() => {
        // Ignore errors for tabs that don't have the content script
      });
    });
  });
});
