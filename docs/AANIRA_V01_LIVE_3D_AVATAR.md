# AaNira v01 — First Live 3D Avatar Milestone

This branch starts the new combined prototype under the working name **AaNira v01**.

## What is implemented

- A dedicated, procedural Three.js 3D adult female avatar scene in `aadhini-android/src/main/assets/aanira_v01_live3d.html`.
- Real-time WebGL rendering inside the existing Android WebView.
- Animated idle float, eye gaze, periodic blinking, head movement, and expression states.
- Text-first expression selection in `MainActivity.kt` for neutral, happy, thinking, and romantic/shy prompts.
- Romantic state changes the outfit palette and facial blush; a final character model, wardrobe system, detailed facial rig, and kiss animation are not yet implemented.

## Important implementation note

The prototype currently loads Three.js 0.160.0 from jsDelivr, so the first render requires an internet connection. For an offline/release build, bundle the Three.js runtime locally after validating the prototype.

The current character is a procedural 3D blockout made from meshes. It is not yet a production-quality rigged GLB/VRM character. The next visual milestone is to replace the blockout with a properly authored semi-realistic adult character model with facial blendshapes and outfit variants.

## Validation status

Source files have been committed, but no Android APK build or Motorola Edge 60 Pro runtime test has been performed yet. Do not describe this milestone as device-verified until that test passes.
