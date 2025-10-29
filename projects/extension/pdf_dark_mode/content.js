console.log('[PDF Dark Mode] Content script loaded on:', window.location.href);

// Listen for messages from background script
browser.runtime.onMessage.addListener((message) => {
  console.log('[PDF Dark Mode] Received message:', message);
  if (message.command === "toggle-dark-mode") {
    toggleDarkMode();
  }
});

// Function to toggle dark mode
function toggleDarkMode() {
  console.log('[PDF Dark Mode] toggleDarkMode() called');
  
  // Target the PDF viewer container
  const viewerContainer = document.getElementById('viewerContainer');
  console.log('[PDF Dark Mode] viewerContainer found:', viewerContainer);
  
  if (viewerContainer) {
    const currentFilter = viewerContainer.style.filter;
    console.log('[PDF Dark Mode] Current filter:', currentFilter);
    
    if (currentFilter === 'invert(1) grayscale(1)') {
      viewerContainer.style.filter = '';
      console.log('[PDF Dark Mode] Filter removed');
    } else {
      viewerContainer.style.filter = 'invert(1) grayscale(1)';
      console.log('[PDF Dark Mode] Filter applied');
    }
  } else {
    console.log('[PDF Dark Mode] viewerContainer not found, using fallback');
    // Fallback for non-PDF pages
    if (document.body.style.filter === 'invert(1) grayscale(1)') {
      document.body.style.filter = '';
      console.log('[PDF Dark Mode] Fallback: Filter removed from body');
    } else {
      document.body.style.filter = 'invert(1) grayscale(1)';
      console.log('[PDF Dark Mode] Fallback: Filter applied to body');
    }
  }
}

// Also listen for keyboard shortcut directly in page
document.addEventListener('keydown', function(e) {
  // Ctrl+Q (or Cmd+Q on Mac)
  if ((e.ctrlKey || e.metaKey) && e.key === 'q') {
    console.log('[PDF Dark Mode] Keyboard shortcut detected: Ctrl+Q');
    e.preventDefault();
    toggleDarkMode();
  }
});

console.log('[PDF Dark Mode] Event listeners registered');

/*
about:config

pdfjs.enableAltText	true	
pdfjs.enableAltTextForEnglish	true	
pdfjs.enabledCache.state	true	
pdfjs.forcePageColors	true	
pdfjs.migrationVersion	2	
pdfjs.pageColorsBackground	#2A2A2E	
pdfjs.pageColorsForeground	#ffffff
*/