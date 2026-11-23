# Camilia Android runtime model

The current Three.js WebView avatar loader expects:

`aadhini-android/src/main/assets/models/camilia.glb`

The GLB is not present yet. Do not rename the previous placeholder `aadhini_avatar.glb` to this path unless it is actually the validated Camilia export.

Requirements before adding the model:

- glTF 2.0 / GLB
- Full Camilia geometry, materials, and embedded textures
- Original skeleton and skin weights preserved
- Facial morph targets/blendshapes retained and tested
- Mobile-appropriate geometry and texture sizes
- Correct scale, orientation, and neutral pose
- Animation clips can be added later; the source FBX was previously inspected as having no embedded animation clips

The source FBX is approximately 383 MiB and must not be loaded directly by the Android app or committed to this assets directory. Follow `docs/avatars/CAMILIA_ANDROID_ASSET_PIPELINE.md` for conversion and validation.
