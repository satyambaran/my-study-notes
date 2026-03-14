// Auto PiP - Background Service Worker
chrome.runtime.onInstalled.addListener(() => {
  chrome.storage.sync.set({ autopipEnabled: true });
});
