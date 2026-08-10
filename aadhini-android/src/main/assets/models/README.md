# Aadhini 3D Avatar Model

The production avatar will be supplied as:

`aadhini_avatar.glb`

Place it at:

`aadhini-android/src/main/assets/models/aadhini_avatar.glb`

Requirements for the first model:

- glTF 2.0 / GLB
- Android/mobile-friendly texture sizes
- A neutral idle pose
- Skeletal animation support preferred
- Named animation clips for at least idle and speaking
- Facial morph targets / blend shapes preferred for blink and lip-sync

The Filament renderer currently treats this file as optional. If it is absent,
the existing WebView avatar remains the safe fallback.
