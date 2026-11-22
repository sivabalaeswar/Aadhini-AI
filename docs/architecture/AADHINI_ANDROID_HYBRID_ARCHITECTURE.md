# Aadhini Android Hybrid Architecture

Status: Initial implementation plan
Base: `prototype/aanira-v02-fullscreen-avatar`
Decision: Android APK shell + embedded lightweight 3D companion renderer.

## Non-negotiable project boundaries

- Preserve the existing Aadhini identity, UX decisions, Tamil-first behaviour, and canonical design handoff.
- Preserve the existing Aadhini Brain and backend contract. The current local bridge endpoint remains `http://127.0.0.1:8765/chat` unless a separately approved change is made.
- Do not replace Aadhini's brain, memory, prompts, or voice design with Amni's application-level AI/voice stack.
- Do not modify `main`, the v01 prototype, or the existing v02 branch.
- Treat Camilia as the intended avatar appearance. The supplied FBX is a source asset; it is not yet a mobile-ready runtime asset.
- Reuse only the required animation/runtime techniques from Amni after reviewing its AGPL-3.0 obligations. Do not copy the complete Amni application.

## Target runtime

1. Android APK is the installable application shell and owns Android lifecycle, permissions, audio focus, notifications, and native integrations.
2. A WebView-hosted Three.js companion surface is the first 3D implementation target. Keep the renderer isolated behind a small JavaScript interface so it can be replaced later if profiling proves a native renderer is necessary.
3. The 3D surface loads an optimized GLB avatar and a curated animation set. It must not load the original 383 MiB FBX at runtime.
4. The Aadhini Brain remains a separate service. The companion receives assistant state/events and emits user interaction events through a documented bridge.
5. Push-to-talk is the reliable voice baseline. Wake-word listening is optional and must respect Android microphone foreground-service and battery restrictions.

## Runtime performance rules

- Start with a low-cost scene, one avatar, one key light, and capped device pixel ratio.
- Load the avatar only after the shell and controls are responsive.
- Use compressed textures and geometry only after validating visual quality and Android WebView support.
- Load animation clips on demand or use a small curated set; do not retarget the full animation library at startup.
- Pause rendering when the app is backgrounded or the companion is not visible.
- Provide a static/loading fallback and recoverable error UI; never leave an infinite spinner.
- Profile on the actual Motorola Edge 60 Pro. WebGPU availability alone is not proof that a particular renderer/model fits memory or thermal limits. Keep a WebGL-compatible fallback where practical.

## Implementation phases

### Phase 0 — Baseline and safety
- Record the current v02 app entry point, build system, backend URL/configuration, and existing interaction flow.
- Create a clean Android-hybrid branch (this branch) without changing existing prototypes.
- Add a lightweight diagnostics screen for renderer availability, load duration, and recoverable errors.

### Phase 1 — Android shell proof
- Build/install a minimal APK shell.
- Open a local bundled companion page in WebView.
- Verify lifecycle handling, touch input, and bridge communication.
- Do not integrate the large Camilia model yet.

### Phase 2 — 3D renderer proof
- Load a simple low-poly test avatar first.
- Measure startup time, frame rate, memory pressure, device temperature, and crash behaviour.
- Test both WebGL and WebGPU paths where supported; select based on measured stability, not feature detection alone.

### Phase 3 — Camilia asset pipeline
- Inspect the source FBX and preserve required face blendshapes/rig data.
- Convert and optimize on a desktop/laptop workflow when available; keep the phone as the target test device.
- Produce a mobile GLB with validated skeleton, facial morph targets, textures, and a small set of animations.
- Never discard facial controls or animation data without an explicit validation pass.

### Phase 4 — Aadhini integration
- Connect the renderer to the existing Aadhini Brain/backend without changing its contract.
- Map assistant states to idle/listening/thinking/speaking animations.
- Add blink, expressions, jaw/lip-sync, gestures, and user-triggered actions incrementally.

## Initial acceptance criteria

- APK installs and opens on the Motorola Edge 60 Pro.
- Companion shell appears without an infinite loading state.
- A simple avatar renders reliably for at least 10 minutes without browser/app termination.
- Touch controls and push-to-talk remain responsive while the avatar is visible.
- Backgrounding and returning to the app does not leave a runaway render loop.
- Aadhini backend requests still use the existing contract.
- Camilia is not considered integrated until skeleton, face controls, textures, and animation quality are verified.

## Current known risks / unknowns

- The Amni live demo previously stalled or closed Chrome on this device even though WebGL and WebGPU capability tests succeeded. The exact failure cause is not confirmed.
- Camilia FBX is approximately 383 MiB and has a complex scene (278 meshes, 313 bones, 941 reported morph targets, 21 materials, 32 textures). It is not suitable for direct mobile runtime loading.
- A previous Assimp export attempt did not complete successfully. Do not repeat the same conversion blindly or assume a valid GLB exists.
- Amni Companion is AGPL-3.0. Review license obligations before distributing a modified derivative or offering it as a network service.

## Next concrete task

Inspect the actual v02 prototype entry point and build setup, then implement only the smallest Android/WebView shell proof on this branch. Do not change the existing v02 source until its structure and current state have been inspected.


## Repository inspection update — 2026-11-23

The v02 branch already contains an Android application module and a WebView-hosted Three.js procedural avatar. Therefore, the first milestone is not a new WebView shell; it is hardening and measuring the existing implementation.

Verified existing files:
- `aadhini-android/src/main/kotlin/ai/aadhini/android/ui/MainActivity.kt`
- `aadhini-android/src/main/assets/aanira_v01_live3d.html`
- `aadhini-android/build.gradle.kts`
- `.github/workflows/aanira-v01-android-build.yml`

Changes made on this isolated branch:
- WebView now reports a renderer-process termination in the app status instead of silently failing.
- WebView is paused/resumed with the Android Activity lifecycle and stopped/destroyed during Activity teardown.
- Zoom controls are disabled for the immersive companion surface.
- Three.js renderer pixel ratio is capped at 1.25 to reduce fill-rate and memory pressure on mobile.
- The animation loop pauses while the page is hidden and resumes when visible.
- WebGL context-loss and context-restoration events are surfaced in the status label.
- A branch-specific GitHub Actions workflow was added to build and upload a debug APK.

Not yet verified:
- No successful CI run or APK artifact has been observed yet.
- Runtime stability, frame rate, and thermal behaviour still require installation and testing on the Motorola Edge 60 Pro.
- The current avatar remains the existing procedural prototype, not the final Camilia model.
