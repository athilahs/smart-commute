# Implementation Plan: Status Alerts Screen

**Branch**: `003-status-alerts-screen` | **Date**: 2025-12-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-status-alerts-screen/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build a Status Alerts screen as the second tab in the bottom navigation, enabling users to configure recurring or one-time alarms that notify them about London Underground tube line statuses at scheduled times. Users can select notification time (time picker), weekdays (weekday selector), and tube lines (tube line picker). The screen displays a list view of all configured alarms (similar to Android's Clock app) with enable/disable toggles. Tapping an alarm opens a bottom sheet for editing. A FAB enables alarm creation (hidden when 10 alarms exist). Empty state shown when no alarms exist. Notifications are silent for "Good Service" status, audible for disruptions. Alarms persist across device reboots using WorkManager/AlarmManager.

## Technical Context

**Language/Version**: Kotlin 2.3.0 with Java 17 target
**Primary Dependencies**: Jetpack Compose (BOM 2025.12.01), Material Design 3, Hilt 2.57.2, Room 2.8.4, Glide Compose 1.0.0-beta08, Kotlin Coroutines 1.10.2, Navigation Compose 2.9.6, WorkManager (version TBD), Retrofit 3.0.0
**Storage**: Room Database for alarm configurations; WorkManager for scheduled alarm triggers
**Testing**: Manual verification only (per constitution - no automated tests)
**Target Platform**: Android 8.0+ (API 26+), targeting Android 14+ (API 34+)
**Project Type**: Mobile (Android) - single-module app with feature-based architecture
**Performance Goals**: Alarms list loads within 500ms; alarm creation/edit completes within 60 seconds; notifications trigger within 30 seconds of scheduled time; 60 fps UI rendering; <200ms interaction response
**Constraints**: Notifications require runtime permission (Android 13+); alarms must persist across device reboots; notifications must handle exact timing requirements; WorkManager/AlarmManager scheduling limitations; TfL API availability at alarm trigger time; 10 alarm maximum limit; offline notification handling (requires cached status or error notification)
**Scale/Scope**: Maximum 10 alarms per user; ~11 London Underground lines; single user (no multi-user); notification scheduling for recurring and one-time alarms; background service integration

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ I. User-First Design
- **Status**: PASS
- **Evidence**: Specification includes 9 prioritized user stories (P1: Grant Notification Permission, Create Basic Status Alert, Manage Multiple Tube Lines Per Alarm, Schedule Recurring Weekly Alarms; P2: Create One-Time Alarms, Enable/Disable Alarms, Edit Existing Alarms; P3: Delete Alarms, Prevent Exceeding Maximum Alarm Limit) with clear acceptance scenarios. Each story has independent test criteria and documented priority rationale.

### ✅ II. Specification-Driven Development
- **Status**: PASS
- **Evidence**: Complete spec.md exists with 9 user stories, 69 functional requirements (FR-001 through FR-069), 10 success criteria, comprehensive edge cases, assumptions, and dependencies. Clarifications session resolved 5 critical ambiguities. Changes documented in Clarifications section.

### ✅ III. No Automated Testing
- **Status**: PASS
- **Evidence**: Technical Context explicitly states "Manual verification only (per constitution - no automated tests)". No test infrastructure planned. Success criteria focus on manual validation and user acceptance.

### ✅ IV. Progressive Planning
- **Status**: PASS
- **Evidence**: Following three-phase approach: (1) Specification completed in spec.md, (2) Implementation Plan (this document) in progress with research, data model, and contract phases defined, (3) Tasks will be generated via /speckit.tasks command.

### ✅ V. Independent User Stories
- **Status**: PASS
- **Evidence**: P1 stories deliver MVP functionality independently. "Grant Notification Permission" (P1) is a prerequisite but delivers immediately testable value. "Create Basic Status Alert" (P1) delivers core alarm functionality. "Manage Multiple Tube Lines Per Alarm" (P1) and "Schedule Recurring Weekly Alarms" (P1) build on core but remain independently implementable. P2/P3 stories add optional enhancements.

### ✅ VI. Simplicity and YAGNI
- **Status**: PASS
- **Evidence**: Reuses existing tube line data models from feature 001-tube-status-screen. Single-module Android app. Essential dependencies only (WorkManager for alarm scheduling, Room for persistence). No premature abstractions. Maximum 10 alarms enforced to prevent over-engineering. No snooze/complex notification actions (spec explicitly removes these).

### ✅ VII. Explicit Over Implicit
- **Status**: PASS
- **Evidence**: All technical decisions explicit (Kotlin 2.3.0, API 26+, specific library versions). Alarm behavior explicitly defined for edge cases (past time handling, missed alarms, conflicting alarms). FR requirements are concrete and unambiguous. Clarifications section documents all resolved ambiguities with clear answers.

### ✅ Android Platform Standards - Architecture
- **Status**: PASS
- **Evidence**: MVVM pattern will be used (ViewModel + Repository). Hilt for dependency injection. Kotlin Flow for reactive streams. Single Activity with Navigation Component for bottom navigation. Room Repository pattern for alarm data access.

### ✅ Android Platform Standards - UI/UX
- **Status**: PASS
- **Evidence**: Jetpack Compose for UI. Material Design 3 components (FAB, Bottom Sheet, TimePicker, Lists). Dark mode support (implicit in Material Design 3 theming). Accessibility required (content descriptions for alarms, TalkBack support). String resources in strings.xml (no hardcoded text).

### ✅ Android Platform Standards - Data & State
- **Status**: PASS
- **Evidence**: ViewModels for UI state management. StateFlow for observable alarm list. Room Database for alarm persistence (FR-064, FR-065). No SharedPreferences usage. WorkManager for background alarm scheduling (guaranteed execution across reboots per FR-053). Offline-first with graceful degradation (error notifications when TfL API unavailable per FR-054, FR-055).

### ✅ Android Platform Standards - Performance
- **Status**: PASS
- **Evidence**: LazyColumn for alarm list (scrollable, efficient). Glide for tube line logo/icon loading (reuses existing integration). Coroutines for async operations (alarm scheduling, TfL API calls). Network calls off main thread (Retrofit + Coroutines). Performance goals explicit in Technical Context (500ms list load, 30s notification trigger tolerance, 60 fps UI).

### ✅ Android Platform Standards - Security
- **Status**: PASS
- **Evidence**: HTTPS only for TfL API (existing from feature 001). TfL API key managed via BuildConfig (existing configuration). R8 obfuscation for release builds (existing). Runtime notification permission checks (FR-001, FR-002, FR-004 for Android 13+). Minimal permissions (internet, notification, SCHEDULE_EXACT_ALARM for Android 12+).

### ✅ Android Platform Standards - Code Organization
- **Status**: PASS
- **Evidence**: Feature-based package structure (com.smartcommute.feature.statusalerts). Follows existing pattern from linestatus and linedetails features. See Project Structure section for concrete paths.

### ✅ Android Platform Standards - Build & Configuration
- **Status**: PASS
- **Evidence**: Min SDK 26, Target SDK 34+ documented in Technical Context. Kotlin 2.3.0 specified. Gradle version catalogs already in use (libs.versions.toml). WorkManager dependency version TBD during research phase. Build variants (debug/release) already configured.

**GATE RESULT**: ✅ **ALL CHECKS PASSED** - Proceed to Phase 0 Research

---

## Phase 0 & Phase 1 Completion Status

**Phase 0: Research** ✅ COMPLETE
- research.md generated with WorkManager/AlarmManager decision
- Notification permissions research completed
- Material Design 3 components research completed
- All NEEDS CLARIFICATION markers resolved

**Phase 1: Design & Contracts** ✅ COMPLETE
- data-model.md generated with domain entities, UI states, and validation rules
- contracts/ directory created with:
  - alarm-entity.md (Room database contract)
  - alarm-scheduler.md (AlarmManager integration contract)
  - alarm-repository.md (Repository layer contract)
- quickstart.md generated with implementation order and integration guide
- Agent context updated (CLAUDE.md)

**Constitution Check Re-evaluation** (Post-Phase 1):

### ✅ I. User-First Design (Re-validated)
- **Status**: PASS
- **Evidence**: Design artifacts (data model, contracts) directly map to user stories. Each domain entity supports specific user scenarios (StatusAlert → User Story 2-8, TubeLine picker → User Story 3).

### ✅ II. Specification-Driven Development (Re-validated)
- **Status**: PASS
- **Evidence**: All design decisions documented with traceability to FR requirements. Contracts include FR references (e.g., alarm-entity.md validates FR-032, FR-044, FR-045).

### ✅ III. No Automated Testing (Re-validated)
- **Status**: PASS
- **Evidence**: Contracts specify "manual verification" testing strategies. No test infrastructure planned in data model or contracts.

### ✅ IV. Progressive Planning (Re-validated)
- **Status**: PASS
- **Evidence**: Phase 0 (research) and Phase 1 (design) completed sequentially. Tasks phase (Phase 2) will follow via /speckit.tasks command.

### ✅ V. Independent User Stories (Re-validated)
- **Status**: PASS
- **Evidence**: Data model supports incremental implementation. StatusAlert entity can be created without TubeLine picker (use hardcoded lines for MVP). Each P1 story independently deliverable.

### ✅ VI. Simplicity and YAGNI (Re-validated)
- **Status**: PASS
- **Evidence**: AlarmManager chosen over WorkManager based on actual requirements (exact timing). No premature abstractions in data model. Room schema uses simple comma-separated strings instead of junction tables for weekdays/lines (YAGNI).

### ✅ VII. Explicit Over Implicit (Re-validated)
- **Status**: PASS
- **Evidence**: All file paths concrete in quickstart.md. Entity fields explicitly typed. Validation rules clearly documented. No placeholders in final deliverables.

### ✅ Android Platform Standards - Architecture (Re-validated)
- **Status**: PASS
- **Evidence**: Repository pattern confirmed in alarm-repository.md. Hilt modules defined in contracts. Kotlin Flow used for observeAllAlarms(). MVVM with ViewModel + StateFlow in data model.

### ✅ Android Platform Standards - UI/UX (Re-validated)
- **Status**: PASS
- **Evidence**: Material Design 3 components confirmed (ModalBottomSheet, TimePicker, FilterChip). Compose implementations in research.md. Accessibility patterns documented (TalkBack support).

### ✅ Android Platform Standards - Data & State (Re-validated)
- **Status**: PASS
- **Evidence**: Room database with DAO contract. StateFlow for UI state. AlarmManager for background scheduling. Offline-first design with error handling (FR-054, FR-055).

### ✅ Android Platform Standards - Performance (Re-validated)
- **Status**: PASS
- **Evidence**: LazyColumn for alarm list in data model. Coroutines for async operations in repository. AlarmManager uses exact timing APIs (setExactAndAllowWhileIdle). Database indexes defined for enabled and createdAt fields.

### ✅ Android Platform Standards - Security (Re-validated)
- **Status**: PASS
- **Evidence**: Runtime permission checks for POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM. PendingIntent uses FLAG_IMMUTABLE. No hardcoded secrets. TfL API key managed via BuildConfig (existing).

### ✅ Android Platform Standards - Code Organization (Re-validated)
- **Status**: PASS
- **Evidence**: Feature-based structure confirmed in quickstart.md. Package paths concrete: com.smartcommute.feature.statusalerts.{ui,data,domain,notification,di}.

### ✅ Android Platform Standards - Build & Configuration (Re-validated)
- **Status**: PASS
- **Evidence**: No new gradle dependencies required (reuses existing Room, Hilt, Compose). Manifest permissions documented in quickstart.md. Kotlin 2.3.0 confirmed in Technical Context.

**POST-DESIGN GATE RESULT**: ✅ **ALL CHECKS PASSED** - Proceed to Phase 2 (Tasks Generation via /speckit.tasks)

## Project Structure

### Documentation (this feature)

```text
specs/003-status-alerts-screen/
├── spec.md              # Feature specification (completed)
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── alarm-entity.md           # Room entity contract for StatusAlert
│   ├── notification-worker.md    # WorkManager worker contract
│   └── alarm-repository.md       # Repository interface contract
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/smartcommute/
├── core/
│   ├── ui/theme/                    # [EXISTING] Material Design 3 theme, colors, typography
│   ├── di/                          # [EXISTING] Hilt modules for app-wide dependencies
│   ├── network/                     # [EXISTING] Retrofit configuration, TfL API service
│   └── navigation/                  # [EXISTING] Bottom navigation configuration
│       └── NavGraph.kt              # [UPDATE] Add StatusAlerts destination
│
├── feature/
│   ├── linestatus/                  # [EXISTING] Feature 001 - tube status screen
│   │   ├── ui/                      # LineStatusScreen composable
│   │   ├── data/                    # LineStatusRepository, TfL API models
│   │   └── domain/                  # Line entity, status enums
│   │
│   ├── linedetails/                 # [EXISTING] Feature 002 - line details screen
│   │   ├── ui/                      # LineDetailsScreen composable
│   │   ├── data/                    # LineDetailsRepository, Room entities
│   │   └── domain/                  # Line details domain models
│   │
│   └── statusalerts/                # [NEW] Feature 003 - status alerts screen
│       ├── ui/
│       │   ├── StatusAlertsScreen.kt          # Main screen with alarm list and FAB
│       │   ├── AlarmListItem.kt               # Individual alarm list item composable
│       │   ├── AlarmBottomSheet.kt            # Alarm configuration bottom sheet
│       │   ├── EmptyStateView.kt              # Empty state when no alarms
│       │   ├── components/
│       │   │   ├── TimePickerDialog.kt        # Time picker component
│       │   │   ├── WeekdaySelector.kt         # Weekday multi-select component
│       │   │   └── TubeLinePicker.kt          # Tube line multi-select component
│       │   └── StatusAlertsViewModel.kt       # ViewModel for UI state and actions
│       │
│       ├── data/
│       │   ├── local/
│       │   │   ├── StatusAlertEntity.kt       # Room entity for alarm storage
│       │   │   ├── StatusAlertDao.kt          # Room DAO for CRUD operations
│       │   │   └── StatusAlertsDatabase.kt    # Room database configuration
│       │   ├── repository/
│       │   │   ├── StatusAlertsRepository.kt  # Repository interface
│       │   │   └── StatusAlertsRepositoryImpl.kt # Repository implementation
│       │   └── worker/
│       │       └── AlarmNotificationWorker.kt # WorkManager worker for notifications
│       │
│       ├── domain/
│       │   ├── model/
│       │   │   └── StatusAlert.kt             # Domain model for alarm
│       │   └── util/
│       │       └── AlarmScheduler.kt          # Alarm scheduling utility
│       │
│       └── notification/
│           └── NotificationManager.kt         # Notification creation and display logic
│
└── ui/
    └── MainActivity.kt                         # [EXISTING] Single Activity with bottom nav
```

**Structure Decision**: Feature-based architecture following existing patterns from features 001 (linestatus) and 002 (linedetails). New feature 003 (statusalerts) will be organized by layer (ui, data, domain, notification) within the feature package. This aligns with Android best practices for feature modules and maintains consistency with existing codebase structure. WorkManager worker placed in data layer as it handles background data fetching and notification triggering.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
