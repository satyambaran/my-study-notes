console.log('[PDF Dark Mode] Background script loaded');

// Listen for the keyboard command
browser.commands.onCommand.addListener((command) => {
  console.log('[PDF Dark Mode] Command received:', command);
  if (command === "toggle-dark-mode") {
    sendToggleMessage();
  }
});

// Listen for toolbar button click
browser.browserAction.onClicked.addListener(() => {
  console.log('[PDF Dark Mode] Toolbar button clicked');
  sendToggleMessage();
});

function sendToggleMessage() {
  console.log('[PDF Dark Mode] Sending toggle message to active tab');
  
  browser.tabs.query({active: true, currentWindow: true}).then((tabs) => {
    const tab = tabs[0];
    console.log('[PDF Dark Mode] Active tab:', tab.url);
    
    // Send message to content script
    browser.tabs.sendMessage(tab.id, {action: "toggle"}).then(() => {
      console.log('[PDF Dark Mode] Message sent successfully');
    }).catch(err => {
      console.error('[PDF Dark Mode] Failed to send message:', err);
    });
  });
}

// accounts-static.cdn.mozilla.net
// accounts.firefox.com
// addons.cdn.mozilla.net
// addons.mozilla.org
// api.accounts.firefox.com
// content.cdn.mozilla.net
// discovery.addons.mozilla.org
// oauth.accounts.firefox.com
// profile.accounts.firefox.com
// support.mozilla.org
// sync.services.mozilla.com

// accounts-static.cdn.mozilla.net,accounts.firefox.com,addons.cdn.mozilla.net,addons.mozilla.org,api.accounts.firefox.com,content.cdn.mozilla.net,discovery.addons.mozilla.org,oauth.accounts.firefox.com,profile.accounts.firefox.com,support.mozilla.org,sync.services.mozilla.com


