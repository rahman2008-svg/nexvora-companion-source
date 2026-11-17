# NexVora Companion – status and continuation point
Based on Google AI Edge Gallery (Apache-2.0, © Google LLC). Original license headers retained.
**NOTHING HAS BEEN COMPILED OR RUN.** No network/Gradle in the authoring environment. Expect compile errors on first build.

## Implemented in source
- applicationId com.nexvora.companion, v1.0.0 / versionCode 1, app name NexVora Companion
- About Developer / Company / Credits (ui/home/AboutDeveloperDialog.kt); WhatsApp links use +880 with the leading 0 removed
- Design tokens + components (ui/theme/NexVoraDesign.kt); NexVora dark scheme, dark default, dark splash background
- Adaptive launcher icon (gradient N), monochrome layer
- Room database v1 (nexvora/data): memories, notes, saved prompts, documents (metadata; files in app-private storage), summaries
- Preferences DataStore: onboarding flag, memory enabled, user name (nexvora/data/NexVoraPrefs.kt)
- Six-page onboarding with Skip/Get Started, saved in DataStore (nexvora/ui/Onboarding.kt)
- Five-tab shell (Home, AI Chat, Assistant, Library, Settings) replacing the old home route (nexvora/ui/NexVoraShell.kt, GalleryNavGraph.kt). Chat/Voice/Image/Prompt Lab/Agent open the EXISTING Gallery task screens (real on-device inference)
- Home dashboard: AI readiness/model/storage card, six quick-action cards, recent documents, download shortcut
- Personal Memory: add/edit/delete/search/clear, enable switch (Room)
- Library: memories, notes, prompts, documents (import PDF/text, text preview, delete), summaries (manual add), search, share
- Settings tab: existing settings dialog, model manager, memory switch, replay onboarding, clear local data, About
- GitHub Actions debug APK workflow, README

## NOT implemented (do not assume)
1. Conversation/message persistence, rename/search chat history (existing chat screens do not save history)
2. Memory retrieval into chat prompts (memories are stored but not yet injected)
3. Document summarization/QA with the model, PDF text extraction, chunking
4. Dedicated AI Tools screens (Prompt Lab is used as the tools entry)
5. Custom animated splash screen; Room migrations beyond v1; sort/filter options in Library
6. All testing (build, device, model, offline, low-memory)

## Next steps
1. Push to GitHub, run workflow, fix compile errors from the build-log artifact (likely in new nexvora/ files and the Room/KSP setup).
2. Add conversation persistence by hooking the LLM chat view model; inject memories into prompts.
3. Add document summarizer (PDF text extraction library + chunked inference through the existing LLM helper).
