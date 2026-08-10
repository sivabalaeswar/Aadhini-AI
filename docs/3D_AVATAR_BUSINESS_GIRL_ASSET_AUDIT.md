# Aadhini 3D Avatar — Business Girl Asset Audit

## Source assets inspected

- `business girl Final.blend` — Blender 4.0 file, ~244 MB.
- `business girl .obj` + `.mtl` — OBJ export, ~106k vertices / ~92k faces, 20 objects and 41 materials.
- `business_girl_texture.zip` — upload is incomplete/corrupt and cannot currently be extracted.

## Blender source findings

The Blender file contains substantially more character data than the OBJ export. Static inspection of the `.blend` confirms:

- Blender 4.0 file header.
- Armature/rig-related data and pose/animation UI data.
- Auto-Rig Pro integration data.
- Faceit facial-rig / shape-key tooling data.
- `faceit_armature`, `faceit_body_armature`, and `faceit_control_armature` references.
- Eye, teeth, tongue, eyelash and body character components, including `CC_Base_Eye`, `CC_Base_Teeth`, `CC_Base_Tongue`, and `CC_Base_Body`.
- Facial/eye animation capability indicators such as `can_animate_eye_rotation`.
- Shape-key related data is present in the file; the exact number and names of production-ready facial shapes still require Blender-side inspection.
- Original source/import paths reference an FBX character source and associated texture assets.

## Decision

Use the **Blender source as the master asset**, not the OBJ export. The OBJ is useful as a fallback/static geometry source but should not be the primary Aadhini pipeline asset because it cannot preserve the full Blender rig/animation/shape-key workflow.

## Aadhini customization target

Keep the technical character foundation where possible:

- skeleton/armature
- facial rig / shape-key infrastructure
- eye and mouth components
- existing animation data
- material separation

Customize toward the locked Aadhini visual direction:

- mature realistic appearance
- natural fuller adult body silhouette
- selected hairstyle direction
- Aadhini clothing/styling
- facial expression set for realtime interaction

Do **not** simply scale the whole body uniformly. Proportions should be edited at the mesh/body-region level and then revalidated against the existing rig.

## Mobile optimization target

Before Android export:

1. Remove unnecessary scene objects.
2. Reduce geometry while preserving facial deformation quality.
3. Preserve facial shape keys/rig functionality.
4. Reduce textures toward a mobile-appropriate resolution.
5. Pack/resolve texture references correctly.
6. Export GLB/glTF 2.0.
7. Validate animation, morph targets and materials in a standalone viewer.
8. Integrate the validated GLB with the existing Filament renderer.

## Current blocker

The uploaded texture archive is incomplete/corrupt, so texture completeness cannot yet be verified. The Blender file contains texture path references, but a reliable final mobile asset should use verified, locally available texture files.

## Next execution step

Open the Blender source in Blender 4.x and inspect/export:

- armature names
- animation actions
- shape-key names/counts
- facial rig configuration
- texture dependencies
- object/mesh statistics

Only after that inspection should the asset be modified and exported as the Aadhini prototype GLB.
