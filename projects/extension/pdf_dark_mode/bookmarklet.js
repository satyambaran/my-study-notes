// This is the exact bookmarklet code that works
console.log('[PDF Dark Mode] Bookmarklet script loaded on:', window.location.href);

// Listen for messages from background script
browser.runtime.onMessage.addListener((message) => {
  console.log('[PDF Dark Mode] Message received:', message);
  if (message.action === "toggle") {
    toggleDarkMode();
  }
});

// The bookmarklet function that we know works
function toggleDarkMode() {
  console.log('[PDF Dark Mode] Executing bookmarklet code');
  
  var el = typeof viewer !== 'undefined' ? viewer : document.body;
  
  // Also try viewerContainer for Firefox PDF viewer
  if (document.getElementById('viewerContainer')) {
    el = document.getElementById('viewerContainer');
    console.log('[PDF Dark Mode] Using viewerContainer');
  }
  
  console.log('[PDF Dark Mode] Target element:', el);
  console.log('[PDF Dark Mode] Current filter:', el.style.filter);
  
  if (el.style.filter === 'invert(1) grayscale(1)') {
    el.style.filter = '';
    console.log('[PDF Dark Mode] Filter removed - Normal mode');
  } else {
    el.style.filter = 'invert(1) grayscale(1)';
    console.log('[PDF Dark Mode] Filter applied - Dark mode');
  }
}

// Listen for keyboard shortcut
document.addEventListener('keydown', function(e) {
  // Ctrl+Q (or Cmd+Q on Mac)
  if ((e.ctrlKey || e.metaKey) && e.key === 'q') {
    e.preventDefault();
    console.log('[PDF Dark Mode] Keyboard shortcut detected');
    toggleDarkMode();
  }
});

console.log('[PDF Dark Mode] Event listeners registered');