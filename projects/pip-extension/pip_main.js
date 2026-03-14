// Auto PiP - MAIN world script (document_start)
// Runs before ANY page script (including Teleparty).
// Three key tricks that make this work despite Teleparty's interference:
//
//   1. Save Document.prototype.hidden getter NOW — before Teleparty can
//      override document.hidden on the instance to always return false.
//
//   2. Patch Event.prototype.stopImmediatePropagation to be a no-op for
//      visibilitychange events — so even if Teleparty's capture handler
//      calls it, our handler still fires.
//
//   3. Register visibilitychange in capture phase → browser grants PiP
//      permission without needing a user gesture (special exception for
//      documents becoming hidden).

(function () {
  // ── Trick 1: Save the real hidden getter ──────────────────────────────
  const _realHiddenDesc = Object.getOwnPropertyDescriptor(Document.prototype, "hidden");
  const isReallyHidden = _realHiddenDesc
    ? () => _realHiddenDesc.get.call(document)
    : () => document.hidden;

  // ── Trick 2: Block stopImmediatePropagation on visibilitychange ────────
  const _origSIAP = Event.prototype.stopImmediatePropagation;
  Event.prototype.stopImmediatePropagation = function () {
    if (this.type === "visibilitychange") return; // no-op — let our handler fire
    _origSIAP.call(this);
  };

  // ── State ──────────────────────────────────────────────────────────────
  let autoPipEnabled = true;

  // Receive preference updates from the isolated-world content script
  // (CustomEvents on window are shared across worlds)
  window.addEventListener("__autopip_set", (e) => {
    autoPipEnabled = e.detail;
    console.log("[Auto PiP MAIN] autoPipEnabled =", autoPipEnabled);
  });

  // ── Helpers ────────────────────────────────────────────────────────────
  function getBestVideo() {
    const all = Array.from(document.querySelectorAll("video"));
    const candidates = all.filter((v) => !v.ended && v.readyState >= 1 && v.duration > 0);
    if (!candidates.length) return null;
    return candidates.reduce((best, v) =>
      v.offsetWidth * v.offsetHeight > best.offsetWidth * best.offsetHeight ? v : best
    );
  }

  // ── Core handler ───────────────────────────────────────────────────────
  // Capture phase + registered at document_start = fires first, always.
  window.addEventListener(
    "visibilitychange",
    () => {
      const hidden = isReallyHidden();
      console.log("[Auto PiP MAIN] visibilitychange →", hidden ? "HIDDEN" : "VISIBLE");

      if (!autoPipEnabled || !document.pictureInPictureEnabled) return;

      if (hidden) {
        const video = getBestVideo();
        if (!video) { console.log("[Auto PiP MAIN] No video found"); return; }
        if (document.pictureInPictureElement === video) return;

        video
          .requestPictureInPicture()
          .then(() => console.log("[Auto PiP MAIN] PiP started!"))
          .catch((e) => console.warn("[Auto PiP MAIN] PiP failed:", e.name, "-", e.message));
      } else {
        if (document.pictureInPictureElement) {
          document.exitPictureInPicture().catch(() => {});
          console.log("[Auto PiP MAIN] Exited PiP");
        }
      }
    },
    true // capture phase
  );
})();
