# Camilia Android Asset Pipeline

Status: integration scaffold committed; final model asset not yet converted or bundled.

## Source of truth

- Original source: `fbx file.fbx` (Dropbox: `/AadhiniAnimation/fbx_file/fbx file/fbx file.fbx`)
- Source size: 401,282,128 bytes (~383 MiB)
- Recorded SHA-256: `94c44786c5057c9850af3449057c573ab6d0faabc5c1aad11113a78f64017fba`
- Previous FBX inspection reported 278 meshes, 313 bones, 941 morph targets, 21 materials, and 32 textures.
- Previous inspection reported zero embedded animation clips. Do not assume body animations are included.
- The OBJ export is not the source of truth because it may not retain the original skeleton and facial morph targets.

## Runtime target

Place the validated output at:

`aadhini-android/src/main/assets/models/camilia.glb`

The Android WebView page now attempts to load that exact asset with Three.js `GLTFLoader`. On successful load it hides the procedural preview, frames the model to a 3.25-unit height, and creates an `AnimationMixer` when clips exist. If the file is absent or invalid, it leaves the procedural preview visible and reports that the GLB is missing.

## Conversion requirements

Use a converter that can read the original FBX and export glTF 2.0/GLB while retaining:

1. Skinning and the humanoid skeleton.
2. All required facial blendshapes/morph targets and their names.
3. Materials, texture images, UVs, and normal maps.
4. Correct scale, orientation, and bind pose.

Do not use the OBJ → Mixamo → GLB path for the production Camilia asset. It risks replacing the original rig and losing facial morphs. Mixamo or other motion libraries may be used later only for body clips after retargeting compatibility is verified.

## Validation gate before APK bundling

- Re-open the exported GLB in a glTF viewer and verify the complete character.
- Compare mesh count/visual coverage against the FBX.
- Verify skeleton and skin weights.
- Verify facial morph target names and test at least blink, jaw-open, smile, and brow movement.
- Verify materials/textures are embedded and not external file references.
- Verify file size and memory use are reasonable for Android.
- Verify at least one animation clip only if one is intentionally added; the source FBX was previously inspected as having no animation clips.
- Test load time, frame rate, and thermal behaviour on the Motorola Edge 60 Pro.

## Important limitation

The 383 MiB FBX is not bundled into the APK and must not be loaded directly at runtime. A previous Assimp export attempt did not complete successfully, so do not treat any prior output as a valid GLB. No final `camilia.glb` has been produced or verified yet.
