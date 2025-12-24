# Tasks: Tube Status Screen

**Input**: Design documents from `/specs/001-tube-status-screen/`
**Prerequisites**: plan.md (completed), spec.md (completed), research.md (completed), data-model.md (completed), contracts/ (completed)

**Tests**: Per constitution, NO automated tests will be created. Manual verification only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2)
- Include exact file paths in descriptions

## Path Conventions

- **Android app**: `app/src/main/java/com/smartcommute/` for Kotlin source files
- **Resources**: `app/src/main/res/` for drawable, values, etc.
- Paths shown below use absolute structure from plan.md

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create Android project structure per quickstart.md Step 1.1
- [X] T002 Configure Gradle version catalog in gradle/libs.versions.toml per quickstart.md Step 1.2
- [X] T003 Configure app-level build.gradle.kts with dependencies and BuildConfig per quickstart.md Step 1.3
- [X] T004 Create local.properties with TfL API key per quickstart.md Step 1.4
- [X] T005 Create SmartCommuteApplication.kt with @HiltAndroidApp annotation in app/src/main/java/com/smartcommute/SmartCommuteApplication.kt
- [X] T006 Update AndroidManifest.xml with application class, permissions, and MainActivity per quickstart.md Step 2.2

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T007 [P] Create NetworkResult sealed class for API response wrapping in app/src/main/java/com/smartcommute/core/network/NetworkResult.kt
- [ ] T008 [P] Create StatusType enum with all status categories in app/src/main/java/com/smartcommute/feature/linestatus/domain/model/ServiceStatus.kt
- [ ] T009 [P] Create ServiceStatus data class with type, description, severity in app/src/main/java/com/smartcommute/feature/linestatus/domain/model/ServiceStatus.kt
- [ ] T010 [P] Create UndergroundLine domain model with id, name, status, modeName in app/src/main/java/com/smartcommute/feature/linestatus/domain/model/UndergroundLine.kt
- [ ] T011 [P] Create LineStatusDto data class for TfL API response in app/src/main/java/com/smartcommute/feature/linestatus/data/remote/dto/LineStatusDto.kt
- [ ] T012 [P] Create LineStatusResponseDto data class for TfL API response in app/src/main/java/com/smartcommute/feature/linestatus/data/remote/dto/LineStatusResponseDto.kt
- [ ] T013 [P] Create LineStatusEntity with Room annotations for local caching in app/src/main/java/com/smartcommute/feature/linestatus/data/local/entity/LineStatusEntity.kt
- [ ] T014 [P] Create LineStatusDao interface with @Query methods in app/src/main/java/com/smartcommute/feature/linestatus/data/local/dao/LineStatusDao.kt
- [ ] T015 Create LineStatusDatabase abstract class with Room configuration in app/src/main/java/com/smartcommute/feature/linestatus/data/local/LineStatusDatabase.kt
- [ ] T016 [P] Create TflApiService Retrofit interface with getLineStatus endpoint per contracts/tfl-api.md in app/src/main/java/com/smartcommute/feature/linestatus/data/remote/TflApiService.kt
- [ ] T017 [P] Create LineStatusMapper object with DTO↔Domain↔Entity conversions per data-model.md in app/src/main/java/com/smartcommute/feature/linestatus/data/remote/mapper/LineStatusMapper.kt
- [ ] T018 Create NetworkModule with Retrofit, OkHttp, retry interceptor per contracts/tfl-api.md in app/src/main/java/com/smartcommute/core/di/NetworkModule.kt
- [ ] T019 Create DatabaseModule with Room database and DAO provision in app/src/main/java/com/smartcommute/core/di/DatabaseModule.kt
- [ ] T020 [P] Create LineStatusRepository interface defining fetch operations in app/src/main/java/com/smartcommute/feature/linestatus/domain/repository/LineStatusRepository.kt
- [ ] T021 Create LineStatusRepositoryImpl with TfL API integration, caching, offline handling per contracts/tfl-api.md in app/src/main/java/com/smartcommute/feature/linestatus/data/LineStatusRepositoryImpl.kt
- [ ] T022 Create AppModule binding LineStatusRepository to implementation in app/src/main/java/com/smartcommute/core/di/AppModule.kt
- [ ] T023 [P] Create Material 3 Color.kt with tube line brand colors in app/src/main/java/com/smartcommute/core/ui/theme/Color.kt
- [ ] T024 [P] Create Type.kt with Material 3 typography definitions in app/src/main/java/com/smartcommute/core/ui/theme/Type.kt
- [ ] T025 Create Theme.kt with Material 3 theme including dark mode support in app/src/main/java/com/smartcommute/core/ui/theme/Theme.kt

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - View Current Line Statuses (Priority: P1) 🎯 MVP

**Goal**: Display scrollable list of all London Underground lines with logos, names, and real-time statuses from TfL API, with offline caching

**Independent Test**: Launch app and verify all 11 tube lines display with correct logos, names, and statuses. Test offline mode by disabling network - cached data should display with "No connection" banner.

### Implementation for User Story 1

- [ ] T026 [P] [US1] Create LineStatusUiState sealed class with Loading, Success, Error states per data-model.md in app/src/main/java/com/smartcommute/feature/linestatus/ui/LineStatusUiState.kt
- [ ] T027 [US1] Create LineStatusViewModel with StateFlow<LineStatusUiState>, inject LineStatusRepository, implement fetchStatus and refreshStatus functions in app/src/main/java/com/smartcommute/feature/linestatus/ui/LineStatusViewModel.kt
- [ ] T028 [P] [US1] Create StatusIndicator composable showing colored badge/icon per status type (FR-011) in app/src/main/java/com/smartcommute/feature/linestatus/ui/components/StatusIndicator.kt
- [ ] T029 [P] [US1] Create LineStatusItem composable with line logo, name, status subtitle, and StatusIndicator (FR-002, FR-003, FR-004) in app/src/main/java/com/smartcommute/feature/linestatus/ui/components/LineStatusItem.kt
- [ ] T030 [P] [US1] Create LoadingStateOverlay composable for full-screen spinner (FR-013) in app/src/main/java/com/smartcommute/feature/linestatus/ui/components/LoadingStateOverlay.kt
- [ ] T031 [US1] Create LineStatusScreen composable with Scaffold, TopAppBar with refresh button (FR-025), LazyColumn of LineStatusItems (FR-001, FR-010), pull-to-refresh (FR-024), state handling (Loading/Success/Error), offline banner (FR-019), API error banner (FR-021), "Last updated" indicator (FR-018) in app/src/main/java/com/smartcommute/feature/linestatus/ui/LineStatusScreen.kt
- [ ] T032 [P] [US1] Add tube line logo vector drawables (11 lines) to app/src/main/res/drawable/ per quickstart.md Step 5 (ic_line_bakerloo.xml, ic_line_central.xml, ic_line_circle.xml, ic_line_district.xml, ic_line_hammersmith_city.xml, ic_line_jubilee.xml, ic_line_metropolitan.xml, ic_line_northern.xml, ic_line_piccadilly.xml, ic_line_victoria.xml, ic_line_waterloo_city.xml)
- [ ] T033 [P] [US1] Create string resources for status types, banners, errors in app/src/main/res/values/strings.xml per quickstart.md Step 6
- [ ] T034 [P] [US1] Create colors.xml with status indicator colors in app/src/main/res/values/colors.xml
- [ ] T035 [P] [US1] Create themes.xml for light mode in app/src/main/res/values/themes.xml
- [ ] T036 [P] [US1] Create themes.xml for dark mode in app/src/main/res/values-night/themes.xml
- [ ] T037 [P] [US1] Add content descriptions to LineStatusItem and StatusIndicator for accessibility (FR - SC-006) per quickstart.md accessibility implementation
- [ ] T038 [US1] Update MainActivity to set LineStatusScreen as content with @HiltAndroidApp, SmartCommuteTheme wrapper in app/src/main/java/com/smartcommute/MainActivity.kt

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently. Run manual verification scenarios from quickstart.md Step 9 (tests 1, 2, 3, 4, 5, 6, 7).

---

## Phase 4: User Story 2 - Navigate Using Bottom Navigation (Priority: P2)

**Goal**: Add bottom navigation bar with Line Status screen as a tab to enable future feature scalability

**Independent Test**: Open app and verify bottom navigation bar is visible with a selected "Status" tab. Tap the tab while already on status screen - should remain on screen with tab highlighted.

### Implementation for User Story 2

- [ ] T039 [P] [US2] Create NavigationScreen sealed class with LineStatus route in app/src/main/java/com/smartcommute/core/navigation/NavigationScreen.kt
- [ ] T040 [US2] Create AppNavigation composable with NavHost, handle LineStatus destination in app/src/main/java/com/smartcommute/core/navigation/AppNavigation.kt
- [ ] T041 [US2] Create MainScreen composable with Scaffold containing BottomNavigation with single "Status" tab, NavHost for content (FR-008, FR-009) in app/src/main/java/com/smartcommute/ui/MainScreen.kt
- [ ] T042 [US2] Update MainActivity to use MainScreen instead of LineStatusScreen directly, preserving theme wrapper in app/src/main/java/com/smartcommute/MainActivity.kt
- [ ] T043 [P] [US2] Add bottom navigation item icon and label strings to app/src/main/res/values/strings.xml
- [ ] T044 [US2] Verify state persistence when navigating to/from status screen via bottom nav (FR-012) - manual verification only

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently. Run manual verification for US2 from quickstart.md Step 9.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and final release preparation

- [ ] T045 [P] Configure ProGuard rules for Retrofit, Gson, Glide, Room in app/proguard-rules.pro per quickstart.md Step 7
- [ ] T046 [P] Add comprehensive code comments for non-obvious business logic (status mapping, offline retry logic)
- [ ] T047 Verify performance goals: 3s load time, 60fps rendering, <200ms interactions per plan.md Technical Context
- [ ] T048 Verify screen size compatibility: Test on 5-inch and 10-inch+ devices per FR-016 and SC-003
- [ ] T049 Run manual verification for all scenarios in quickstart.md Step 9 (scenarios 1-7)
- [ ] T050 Verify accessibility with TalkBack enabled per SC-006
- [ ] T051 Verify dark mode theme switching per Android Platform Standards
- [ ] T052 Verify screen rotation state persistence per Edge Cases

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational phase completion
- **User Story 2 (Phase 4)**: Depends on Foundational phase completion AND User Story 1 completion (uses LineStatusScreen)
- **Polish (Phase 5)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) AND depends on User Story 1 completion - integrates LineStatusScreen into navigation

### Within Each User Story

- Domain models before repository
- Repository before ViewModel
- ViewModel before UI composables
- UI components before main screen
- Resources (drawables, strings) can be done in parallel with code
- Accessibility additions after UI components exist

### Parallel Opportunities

- **All Setup tasks (T001-T006)**: Can be done in any order
- **Foundational phase marked [P]** (T007-T025): Many can run in parallel (different files, no dependencies)
  - T007-T012 (models and DTOs) can all be done in parallel
  - T013-T014 (database) can be done in parallel
  - T016-T017 (API service and mapper) can be done in parallel
  - T023-T024 (theme files) can be done in parallel
- **User Story 1 marked [P]** (T026-T038): Several can run in parallel
  - T028-T030 (UI components) can be done in parallel
  - T032-T036 (resources) can be done in parallel
- **User Story 2 marked [P]** (T039-T044): T039 and T043 can be done in parallel
- **Polish phase marked [P]** (T045-T046): Can be done in parallel

---

## Parallel Example: Foundational Phase

```bash
# Launch all domain models in parallel (T008, T009, T010):
# - StatusType enum and ServiceStatus data class
# - UndergroundLine domain model
# - All DTOs (T011, T012)

# Launch all database components in parallel (T013, T014):
# - LineStatusEntity with Room annotations
# - LineStatusDao interface

# Launch API integration in parallel (T016, T017):
# - TflApiService Retrofit interface
# - LineStatusMapper

# Launch theme files in parallel (T023, T024):
# - Color.kt
# - Type.kt
```

---

## Parallel Example: User Story 1

```bash
# Launch all UI components together (T028, T029, T030):
# - StatusIndicator composable
# - LineStatusItem composable
# - LoadingStateOverlay composable

# Launch all resource files together (T032, T033, T034, T035, T036):
# - Tube line logo drawables (11 files)
# - String resources
# - Colors XML
# - Light and dark themes
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T006)
2. Complete Phase 2: Foundational (T007-T025) - CRITICAL blocking phase
3. Complete Phase 3: User Story 1 (T026-T038)
4. **STOP and VALIDATE**: Run all manual verification scenarios for User Story 1
5. Deploy/demo if ready (functional tube status viewer with offline support)

**At this checkpoint, you have a working MVP** that displays tube line statuses with offline caching, pull-to-refresh, and error handling. This delivers the core value proposition of the app.

### Incremental Delivery

1. Complete Setup + Foundational (T001-T025) → Foundation ready
2. Add User Story 1 (T026-T038) → Test independently → **Deploy/Demo (MVP!)**
3. Add User Story 2 (T039-T044) → Test independently → Deploy/Demo (navigation foundation)
4. Add Polish (T045-T052) → Final validation → Production release

Each story adds value without breaking previous stories.

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together (T001-T025)
2. Once Foundational is done:
   - **Developer A**: User Story 1 (T026-T038)
   - **Developer B**: Can start User Story 2 preparation (T039, T043) but must wait for T038 to integrate
3. After User Story 1 complete:
   - **Developer B**: Complete User Story 2 integration (T040-T042, T044)
4. Team collaborates on Polish (T045-T052)

---

## Notes

- **[P] tasks**: Different files, no dependencies - safe to run in parallel
- **[Story] label**: Maps task to specific user story for traceability
- Each user story should be independently completable and testable
- **NO test tasks**: Per constitution, no automated tests. Manual verification only (see quickstart.md Step 9)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- File paths are absolute from repository root
- All tasks follow checklist format: `- [ ] [ID] [P?] [Story?] Description with path`
- Exact paths match plan.md Project Structure section
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
