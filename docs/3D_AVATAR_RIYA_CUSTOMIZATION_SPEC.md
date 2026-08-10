# Aadhini 3D Avatar — Riya Customization Specification

## Purpose

Use the selected realistic Riya character as the technical base for the first Aadhini 3D prototype. The goal is to preserve the character's existing rig, facial animation infrastructure, materials and animation compatibility while changing the visual silhouette toward the chosen Aadhini direction.

This is a prototype specification, not a claim that the Riya asset has already been imported or modified.

## Base Asset Requirements

- Realistic adult female character
- Existing humanoid skeleton/rig retained
- Existing facial blendshapes/morph targets retained where possible
- PBR materials retained where practical
- Export target: glTF 2.0 / GLB
- Android renderer: Google Filament

## Visual Direction

The customized character should have:

- A mature adult facial appearance
- Long, dark hairstyle inspired by the locked Aadhini visual reference
- Natural adult proportions
- Fuller/natural body silhouette rather than artificial uniform scaling
- Natural skin, hair and eye materials
- A polished, realistic presentation suitable for a personal AI companion

The locked Aadhini reference image is the visual direction. Do not use it as a requirement to reproduce a real person's identity.

## Body Customization

Do NOT simply scale the entire Riya body.

Modify proportions through anatomically coherent mesh editing:

1. Torso volume
2. Shoulder width
3. Waist transition
4. Hip proportion
5. Arm and leg proportion as required
6. Overall silhouette

Preserve skeleton compatibility after mesh edits. Re-weight or correct deformation only where required.

## Face / Hair

- Preserve the Riya facial rig and topology where possible.
- Adjust hairstyle toward the locked Aadhini visual direction.
- Preserve expressive facial deformation.
- Do not destroy existing ARKit-compatible facial blendshapes if present.
- If facial topology changes substantially, rebuild/transfer required morph targets.

## Facial Animation Targets

Priority order:

1. Blink
2. Eye look direction
3. Jaw open
4. Mouth close/open
5. Smile
6. Brow movement
7. Viseme set for speech
8. Emotional expressions

Target states:

- IDLE
- LISTENING
- THINKING
- SPEAKING
- HAPPY
- CONFUSED
- SURPRISED
- SAD
- ERROR

## Mobile Optimization

Initial target after customization:

- GLB/glTF 2.0
- Approximately 30k–50k triangles for the visible character where quality permits
- 2K textures as the default target
- Avoid unnecessary duplicate materials
- Preserve facial morphs required for expression and lip-sync
- Keep animation data compact

These are engineering targets, not hard limits. Visual quality and facial animation take priority over an arbitrary triangle count.

## Integration Pipeline

```text
Riya technical base
    ↓
Mesh / proportion customization
    ↓
Hair + material customization
    ↓
Rig / weight validation
    ↓
Facial morph validation
    ↓
Animation validation
    ↓
GLB export
    ↓
FilamentAvatarRenderer
    ↓
AvatarState controller
    ↓
Aadhini Core
    ↓
TTS + lip-sync
```

## Acceptance Criteria

The first prototype is accepted when:

- GLB loads in Filament without runtime failure.
- Character renders correctly on the target Android device.
- Idle animation works.
- Listening and thinking states can be triggered.
- Speaking state drives mouth animation.
- TTS completion returns the avatar to IDLE.
- Facial expressions can be triggered independently of the AI provider.
- The customized silhouette remains natural after rig deformation.

## Important Constraint

The actual Riya asset file has not yet been imported into the repository. The repository currently contains the renderer foundation only. Asset acquisition/import and Blender/Unreal processing must happen before the mesh customization can be performed.
