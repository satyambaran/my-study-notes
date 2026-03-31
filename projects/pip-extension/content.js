// Auto PiP - Isolated world content script
// Only handles chrome API access and popup toggle.
// PiP logic lives in pip_main.js (MAIN world).

(function () {
  function sendPref(value) {
    // CustomEvent on window is visible to MAIN world (pip_main.js)
    window.dispatchEvent(new CustomEvent("__autopip_set", { detail: value }));
  }

  chrome.storage.sync.get({ autopipEnabled: true }, (data) => {
    sendPref(data.autopipEnabled);
  });

  chrome.runtime.onMessage.addListener((msg) => {
    if (msg.type === "SET_PIP_ENABLED") sendPref(msg.value);
  });
})();
