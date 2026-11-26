# Aadhini 3D model asset

The Android WebView and Filament renderers use this asset path:

`aadhini-android/src/main/assets/models/aadhini.glb`

The APK workflow downloads the validated GLB from the AadhiniAnimation Dropbox folder into this path before Gradle builds the debug APK.

Asset validation:
- glTF 2.0 binary container
- 10 meshes, 955 nodes, 1 skin
- 50 embedded images and 59 texture references
- 0 animation clips (unwanted source clip removed)
- 1,829 morph-target entries reported by source GLB inspection
- Required extension: `KHR_draco_mesh_compression`

The WebView loader registers Three.js DRACOLoader before loading the model. Draco decoder assets are fetched from Google's versioned decoder CDN, so the current WebView avatar requires network access for the Three.js modules and decoder. The GLB itself is bundled in the APK.

Keep original materials, textures, skinning, and morph targets intact. Do not add unrelated animation clips. Drive expressions and speech from Aadhini's own state/morph controls.
