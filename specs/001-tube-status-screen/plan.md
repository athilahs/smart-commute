# Implementation Plan: Tube Status Screen

**Branch**: `001-tube-status-screen` | **Date**: 2025-12-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-tube-status-screen/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build the foundation of a London Underground status monitoring Android app with a scrollable list screen displaying all tube lines with their logos, names, and real-time service statuses (Good Service, Minor Delays, Major Delays, Severe Delays, Closure, Service Disruption). Data is fetched from the Transport for London (TfL) Official API with local caching for offline viewing. The screen includes a bottom navigation bar to enable future feature scalability. Manual refresh via pull-to-refresh and refresh button, with graceful offline/error handling.

## Technical Context

**Language/Version**: Kotlin 1.9+
**Primary Dependencies**: Jetpack Compose, Hilt, Retrofit, Room, Kotlin Coroutines, Kotlin Flow, Glide, Material Design 3
**Storage**: Room Database for local caching of line status data
**Testing**: Manual verification only (per constitution - no automated tests)
**Target Platform**: Android 8.0+ (API 26+), targeting Android 14 (API 34)
**Project Type**: Mobile (Android) - single-module app for MVP
**Performance Goals**: App loads and displays cached data within 3 seconds; fresh data fetch within 3 seconds; 60 fps UI rendering; <200ms interaction response
**Constraints**: Offline-capable (cached data), manual refresh only (no automatic polling), TfL API rate limits (free tier), must work on 5-10+ inch screens
**Scale/Scope**: ~11 London Underground lines, single user (no multi-user), MVP single feature

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ I. User-First Design
- **Status**: PASS
- **Evidence**: Specification includes 2 prioritized user stories (P1: View Current Line Statuses, P2: Navigate Using Bottom Navigation) with clear acceptance scenarios and independent test criteria

### ✅ II. Specification-Driven Development
- **Status**: PASS
- **Evidence**: Complete spec.md exists with user scenarios, 23 functional requirements, success criteria, edge cases, assumptions, and out-of-scope declarations. Clarifications session resolved 5 critical ambiguities.

### ✅ III. No Automated Testing
- **Status**: PASS
- **Evidence**: Technical Context explicitly states "Manual verification only (per constitution - no automated tests)". No test infrastructure planned.

### ✅ IV. Progressive Planning
- **Status**: PASS
- **Evidence**: Following three-phase approach: (1) Specification completed, (2) Implementation Plan (this document) in progress, (3) Tasks will be generated via /speckit.tasks

### ✅ V. Independent User Stories
- **Status**: PASS
- **Evidence**: P1 story (View Current Line Statuses) delivers standalone MVP value. P2 story (Navigate Using Bottom Navigation) adds navigation infrastructure independently. Each can be implemented and validated separately.

### ✅ VI. Simplicity and YAGNI
- **Status**: PASS
- **Evidence**: Single-module Android app with essential dependencies only. No premature abstractions planned. Manual refresh only (no automatic polling). Single screen for MVP.

### ✅ VII. Explicit Over Implicit
- **Status**: PASS
- **Evidence**: All technical decisions explicit (Kotlin 1.9+, API 26+, specific libraries). File paths will be concrete in data model and task phases. No ambiguous requirements remain after clarification.

### ✅ Android Platform Standards - Architecture
- **Status**: PASS
- **Evidence**: MVVM with ViewModel + Repository pattern planned. Hilt for DI. Kotlin Flow for reactive streams. Single Activity with Navigation Component (bottom nav).

### ✅ Android Platform Standards - UI/UX
- **Status**: PASS
- **Evidence**: Jetpack Compose for UI. Material Design 3 components. Dark mode support required (FR in spec). Accessibility (content descriptions, TalkBack). Localization (string.xml resources).

### ✅ Android Platform Standards - Data & State
- **Status**: PASS
- **Evidence**: ViewModels for UI state. StateFlow for observables. Room for local persistence. Offline-first with caching (FR-017). No SharedPreferences (will use DataStore if needed).

### ✅ Android Platform Standards - Performance
- **Status**: PASS
- **Evidence**: LazyColumn for scrollable list. Glide for line logo loading. Coroutines for async operations. Network calls off main thread (Retrofit + coroutines). Performance goals explicit (3s load, 60 fps).

### ✅ Android Platform Standards - Security
- **Status**: PASS
- **Evidence**: HTTPS only for TfL API. TfL API key managed via BuildConfig (not hardcoded). R8 obfuscation for release builds. Internet permission required (minimal permissions).

### ✅ Android Platform Standards - Code Organization
- **Status**: PASS
- **Evidence**: Feature-based package structure planned (see Project Structure below). Single module for MVP avoids premature modularization.

### ✅ Android Platform Standards - Build & Configuration
- **Status**: PASS
- **Evidence**: Min SDK 26, Target SDK 34 documented. Kotlin 1.9+ specified. Gradle version catalogs will be used for dependency management. Build variants (debug/release) configured.

**GATE RESULT**: ✅ **ALL CHECKS PASSED** - Proceed to Phase 0 Research

## Project Structure

### Documentation (this feature)

```text
specs/001-tube-status-screen/
├── spec.md              # Feature specification (completed)
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── tfl-api.md      # TfL API integration contract
├── checklists/
│   └── requirements.md  # Specification quality checklist (completed)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/
├── build.gradle.kts
├── proguard-rules.pro
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── java/com/smartcommute/
    │   │   ├── SmartCommuteApplication.kt
    │   │   ├── MainActivity.kt
    │   │   ├── feature/
    │   │   │   └── linestatus/
    │   │   │       ├── ui/
    │   │   │       │   ├── LineStatusScreen.kt
    │   │   │       │   ├── LineStatusViewModel.kt
    │   │   │       │   ├── LineStatusUiState.kt
    │   │   │       │   └── components/
    │   │   │       │       ├── LineStatusItem.kt
    │   │   │       │       ├── StatusIndicator.kt
    │   │   │       │       └── LoadingStateOverlay.kt
    │   │   │       ├── domain/
    │   │   │       │   ├── model/
    │   │   │       │   │   ├── UndergroundLine.kt
    │   │   │       │   │   └── ServiceStatus.kt
    │   │   │       │   └── repository/
    │   │   │       │       └── LineStatusRepository.kt
    │   │   │       └── data/
    │   │   │           ├── remote/
    │   │   │           │   ├── TflApiService.kt
    │   │   │           │   ├── dto/
    │   │   │           │   │   ├── LineStatusResponseDto.kt
    │   │   │           │   │   └── LineStatusDto.kt
    │   │   │           │   └── mapper/
    │   │   │           │       └── LineStatusMapper.kt
    │   │   │           └── local/
    │   │   │               ├── LineStatusDatabase.kt
    │   │   │               ├── dao/
    │   │   │               │   └── LineStatusDao.kt
    │   │   │               └── entity/
    │   │   │                   └── LineStatusEntity.kt
    │   │   ├── core/
    │   │   │   ├── navigation/
    │   │   │   │   ├── AppNavigation.kt
    │   │   │   │   └── NavigationScreen.kt
    │   │   │   ├── di/
    │   │   │   │   ├── AppModule.kt
    │   │   │   │   ├── NetworkModule.kt
    │   │   │   │   └── DatabaseModule.kt
    │   │   │   ├── network/
    │   │   │   │   └── NetworkResult.kt
    │   │   │   └── ui/
    │   │   │       └── theme/
    │   │   │           ├── Color.kt
    │   │   │           ├── Theme.kt
    │   │   │           └── Type.kt
    │   │   └── ui/
    │   │       └── MainScreen.kt
    │   └── res/
    │       ├── drawable/
    │       │   └── [tube line logos]
    │       ├── values/
    │       │   ├── strings.xml
    │       │   ├── colors.xml
    │       │   └── themes.xml
    │       └── values-night/
    │           └── themes.xml
    └── debug/
        └── [debug-specific resources]

build.gradle.kts (project root)
settings.gradle.kts
gradle/
└── libs.versions.toml
```

**Structure Decision**: Android single-module app architecture with feature-based package organization. The `linestatus` feature is organized with UI (Compose screens, ViewModels), domain (models, repository interface), and data (API client, Room database) layers following clean architecture principles within the feature package. Core package contains shared infrastructure (navigation, DI, networking utilities, theme). This structure supports future feature additions as sibling packages under `feature/`.

## Complexity Tracking

No constitution violations - section not applicable.
