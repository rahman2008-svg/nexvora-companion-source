# NexVora Companion

**Your Private AI. Always With You.**

Offline-first Android AI assistant running on-device models (Google AI Edge / LiteRT-LM).
Developed by Prince AR Abdur Rahman · Published by NexVora Lab's Ofc · © 2026 NexVora Lab's Ofc. All Rights Reserved.

- Application ID: `com.nexvora.companion` · Version 1.0.0 (versionCode 1)
- Kotlin + Jetpack Compose, minSdk 31, compileSdk 37 (inherited from the upstream project; its dependencies require it — not lowered to 36)

## Build
Push to GitHub and run the **Build NexVora Companion (Debug APK)** workflow (Actions tab); the APK is uploaded as an artifact.
Locally: `cd Android/src && ./gradlew :app:assembleDebug` (JDK 21, Android SDK platform 37).

## Status
See `NEXVORA_CHANGES.md` for what is implemented, what is not, and the continuation point. This code has not been compiled yet.

## Attribution
Based on [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery), © Google LLC, Apache License 2.0 (see `LICENSE`).
Third-party library licenses are available in the app under Settings.
