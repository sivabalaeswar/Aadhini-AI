# Aadhini 3D model asset

The Android WebView and Filament renderers use the same asset path:

`aadhini-android/src/main/assets/models/aadhini.glb`

The GLB is not bundled in this source commit. Add the validated model at this exact path before expecting the real character to appear in the APK.

Asset validation completed on the prepared GLB:
- glTF 2.0 binary container
- 10 meshes, 955 nodes, 1 skin
- 50 embedded images and 59 texture references
- 0 animation clips (unwanted source clip removed)
- 1,829 morph-target entries reported by the source GLB inspection
- Required extension: `KHR_draco_mesh_compression`

Important runtime check:
- The GLB requires Draco mesh decoding.
- Confirm the target renderer supports Draco before shipping this model. The current Filament renderer must be tested on-device; do not assume successful decoding from a successful APK build.
- Keep original materials, textures, skinning, and morph targets intact.
- Do not add unrelated animation clips. Drive expressions and speech from Aadhini's own state/morph controls.

The source FBX and working files do not belong in the Android assets directory. Only the optimized, validated GLB should be bundled.
