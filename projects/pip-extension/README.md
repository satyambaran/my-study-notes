# Auto PiP — Picture-in-Picture on Tab Switch

Automatically plays videos in a floating Picture-in-Picture window whenever you switch to another tab. Switch back and it returns to normal.

Works on YouTube, Twitch, Vimeo, and most sites with HTML5 video — including pages running the **Teleparty** extension.

---

## Features

- Auto-enters PiP when you leave a tab with a playing video
- Auto-exits PiP when you return to the tab
- Toggle on/off via the extension icon popup
- Works repeatedly without needing to re-click the page
- Compatible with Teleparty (watch parties)

---

## Install Locally (Brave / Chrome)

1. Download or clone this repository
2. Open `brave://extensions` (or `chrome://extensions`)
3. Enable **Developer mode** (toggle in the top-right corner)
4. Click **Load unpacked**
5. Select the `pip-extension` folder
6. The Auto PiP icon appears in your toolbar

> After loading, **reload any existing tabs** you want it to work on (Ctrl+R).

---

## Usage

### Basic
1. Open any page with a video (e.g. YouTube)
2. Start playing the video
3. **Click once on the page** (required by the browser the very first time)
4. Switch to another tab — the video floats in a PiP window
5. Switch back — PiP closes and video plays normally in the tab

### Toggle on/off
Click the **Auto PiP icon** in the toolbar to open the popup and flip the switch.

### Keyboard shortcuts that trigger PiP
| Action | Result |
|---|---|
| `Ctrl+T` (new tab) | PiP starts on the video tab |
| Click another tab | PiP starts |
| `Ctrl+W` (close current tab) | PiP exits on the tab you return to |
| `Alt+Tab` to another app | PiP starts |

---

## How It Works

The extension runs a script in the page's **main JavaScript world** at page load — before any other extension (like Teleparty) can interfere. It:

1. **Saves** the browser's real `document.hidden` getter before any script can override it
2. **Patches** `stopImmediatePropagation` so no script can suppress `visibilitychange` events
3. **Registers** a `visibilitychange` listener in capture phase — fires first, every time

When the `visibilitychange` event fires (tab hidden), the browser grants PiP permission automatically — no repeated user gesture needed.

---

## Troubleshooting

**PiP doesn't start on the first try**
→ Click once anywhere on the video page, then switch tabs. The browser requires one interaction per page load.

**Nothing happens at all**
→ Reload the extension (`brave://extensions` → refresh icon) and hard-reload the video tab (`Ctrl+Shift+R`).

**Works on YouTube but not another site**
→ Open DevTools (`F12`) → Console → filter by `Auto PiP`. Share any red warning messages.

**Netflix / Disney+ doesn't work**
→ These services block PiP via DRM or `disablePictureInPicture` on the video element. This is a platform restriction and cannot be bypassed.

---

## Publish to Chrome Web Store (free except one-time fee)

1. Zip the extension folder (exclude `.git` and `.claude` folders):
   ```bash
   cd pip-extension
   zip -r auto-pip.zip . --exclude "*.git*" --exclude ".claude*"
   ```
2. Go to [chrome.google.com/webstore/devconsole](https://chrome.google.com/webstore/devconsole)
3. Sign in and pay the **one-time $5** developer registration fee
4. Click **New Item** → upload `auto-pip.zip`
5. Fill in description, add screenshots, set category to **Productivity**
6. Submit — review takes 1–3 business days

Once published, it works in both Chrome and Brave (Brave supports all Chrome Web Store extensions).

---

## File Structure

```
pip-extension/
├── manifest.json    # Extension config (Manifest V3)
├── pip_main.js      # PiP logic — runs in MAIN world before page scripts
├── content.js       # Popup toggle bridge (isolated world)
├── background.js    # Service worker — sets defaults on install
├── popup.html       # Toggle UI
├── popup.js         # Toggle logic
└── icons/
    ├── icon16.png
    ├── icon48.png
    └── icon128.png
```
