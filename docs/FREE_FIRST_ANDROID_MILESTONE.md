# Aadhini Android — Free-first milestone

## Decision
The first Motorola Android app milestone must not require a paid API key or make paid cloud-model requests.

## Current implementation
- Android UI continues to use the existing Aadhini identity and avatar surface.
- Voice input uses Android's speech-recognition intent; availability and any network use depend on the installed speech service.
- Speech output uses Android TextToSpeech.
- The app injects `OfflineTamilProvider` through the Core `AIProvider` interface.
- The provider offers a small Tamil-first deterministic baseline for greetings, identity, thanks, and a few simple intents.
- This baseline is deliberately not described as a full generative AI model.
- The Android app no longer calls the Termux/Gemini HTTP bridge from its chat path in this milestone.

## Not yet verified
- APK build and installation on Motorola Edge 60 Pro.
- Runtime UI validation, voice recognition language availability, and Tamil TTS voice availability.
- Durable memory across app restarts.
- Local generative model integration.
- Background microphone / wake-word operation.

## Next engineering steps
1. Build and install the Android app; fix build/runtime issues without redesigning the frozen UX.
2. Add Room-backed local conversation and user-controlled memory cards.
3. Evaluate an on-device model runtime and a model that fits the target phone; keep it optional and fully offline.
4. Add explicit user-controlled voice activation after the baseline is stable.
5. Keep cloud providers disabled by default and never introduce paid API usage without explicit approval.

## Architecture constraints
Preserve the three-module monorepo:
- `aadhini-core`: identity, orchestration, intent, decision, memory contracts.
- `aadhini-platform`: Android device services and provider adapters.
- `aadhini-android`: UI, home, conversation, avatar, and device integration.

One Aadhini, one consistent identity, Tamil-first communication, privacy and user control.
