# Tasks: Line Details Screen

**Input**: Design documents from `/specs/002-line-details-screen/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/, research.md, quickstart.md

**Tests**: NO automated tests per project constitution III. Manual verification only.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Android project**: `app/src/main/java/com/smartcommute/`
- **Resources**: `app/src/main/res/`
- All paths are relative to repository root

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database migration and shared infrastructure updates

**Database Schema Context**:
- **v1 (current)**: Single `line_status` table with basic tube line info (id, name, status, severity)
- **v2 (target)**: Renamed `tube_lines` table (adds headerImageRes, cacheExpiry) + new tables for `disruptions`, `closures`, `crowding` with foreign key relationships

- [x] T001 Update LineStatusDatabase to version 2 and rename line_status table to tube_lines in app/src/main/java/com/smartcommute/feature/linestatus/data/local/LineStatusDatabase.kt
- [x] T002 [P] Create MIGRATION_1_2 in app/src/main/java/com/smartcommute/core/di/DatabaseModule.kt
- [x] T003 [P] Rename LineStatusEntity to TubeLineEntity and update table name in app/src/main/java/com/smartcommute/feature/linestatus/data/local/entity/LineStatusEntity.kt
- [x] T004 [P] Add headerImageRes and cacheExpiry columns to TubeLineEntity
- [x] T005 Rename LineStatusDao to TubeLineDao and update queries to use tube_lines table in app/src/main/java/com/smartcommute/feature/linestatus/data/local/dao/LineStatusDao.kt
- [x] T006 Update DatabaseModule to provide migration and new DAO references in app/src/main/java/com/smartcommute/core/di/DatabaseModule.kt
- [x] T007 [P] Update LineStatusRepositoryImpl to use renamed TubeLineDao in app/src/main/java/com/smartcommute/feature/linestatus/data/repository/LineStatusRepositoryImpl.kt

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core entities and DAOs that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T008 [P] Create DisruptionEntity in app/src/main/java/com/smartcommute/feature/linedetails/data/local/entity/DisruptionEntity.kt
- [x] T009 [P] Create ClosureEntity in app/src/main/java/com/smartcommute/feature/linedetails/data/local/entity/ClosureEntity.kt
- [x] T010 [P] Create CrowdingEntity in app/src/main/java/com/smartcommute/feature/linedetails/data/local/entity/CrowdingEntity.kt
- [x] T011 Create LineDetailsDao with queries for disruptions, closures, and crowding in app/src/main/java/com/smartcommute/feature/linedetails/data/local/dao/LineDetailsDao.kt
- [x] T012 Add LineDetailsDao to LineStatusDatabase in app/src/main/java/com/smartcommute/feature/linestatus/data/local/LineStatusDatabase.kt
- [x] T013 [P] Create Disruption domain model in app/src/main/java/com/smartcommute/feature/linedetails/domain/model/Disruption.kt
- [x] T014 [P] Create Closure domain model in app/src/main/java/com/smartcommute/feature/linedetails/domain/model/Closure.kt
- [x] T015 [P] Create Crowding domain model and CrowdingLevel enum in app/src/main/java/com/smartcommute/feature/linedetails/domain/model/Crowding.kt
- [x] T016 [P] Create StatusSeverity enum if not exists in app/src/main/java/com/smartcommute/feature/linedetails/domain/model/StatusSeverity.kt
- [x] T017 [P] Create UndergroundLineDetails domain model in app/src/main/java/com/smartcommute/feature/linedetails/domain/model/UndergroundLineDetails.kt

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - View Basic Line Information (Priority: P1) 🎯 MVP

**Goal**: Display line details screen with header image, line icon, name, and status with smooth shared element transitions

**Independent Test**: Tap any line from tube status screen and verify line details screen displays with header image showing station, line icon in bottom-right, line name and status overlaid on image with readable contrast. Verify screen loads in <2 seconds.

### Implementation for User Story 1

- [x] T018 [P] [US1] Create LineDetailsRepository interface in app/src/main/java/com/smartcommute/feature/linedetails/domain/repository/LineDetailsRepository.kt
- [x] T019 [P] [US1] Create TubeLineMapper with toBasicDomain and toDetailedDomain functions in app/src/main/java/com/smartcommute/feature/linedetails/data/mapper/TubeLineMapper.kt
- [x] T020 [US1] Create LineDetailsRepositoryImpl with offline-only data access in app/src/main/java/com/smartcommute/feature/linedetails/data/repository/LineDetailsRepositoryImpl.kt
- [x] T021 [P] [US1] Create LineDetailsModule to bind repository in app/src/main/java/com/smartcommute/core/di/LineDetailsModule.kt
- [x] T022 [P] [US1] Create LineDetailsUiState sealed interface in app/src/main/java/com/smartcommute/feature/linedetails/ui/LineDetailsUiState.kt
- [x] T023 [US1] Create LineDetailsViewModel with repository integration in app/src/main/java/com/smartcommute/feature/linedetails/ui/LineDetailsViewModel.kt
- [x] T024 [P] [US1] Add LineDetails navigation route to NavigationScreen in app/src/main/java/com/smartcommute/core/navigation/NavigationScreen.kt
- [x] T025 [US1] Add LineDetailsScreen composable to AppNavigation in app/src/main/java/com/smartcommute/core/navigation/AppNavigation.kt
- [x] T026 [P] [US1] Create LineDetailsHeader composable with hero image and gradient scrim in app/src/main/java/com/smartcommute/feature/linedetails/ui/components/LineDetailsHeader.kt
- [x] T027 [P] [US1] Create LoadingState composable in app/src/main/java/com/smartcommute/feature/linedetails/ui/components/LoadingState.kt
- [x] T028 [P] [US1] Create ErrorState composable with back button in app/src/main/java/com/smartcommute/feature/linedetails/ui/components/ErrorState.kt
- [x] T029 [US1] Create LineDetailsScreen main composable with Scaffold and TopAppBar in app/src/main/java/com/smartcommute/feature/linedetails/ui/LineDetailsScreen.kt
- [x] T030 [US1] Update LineStatusScreen to add onLineClick navigation parameter in app/src/main/java/com/smartcommute/feature/linestatus/ui/LineStatusScreen.kt
- [ ] T031 [P] [US1] Generate and add 11 high-resolution station header images (1440x960px WebP, anonymized crowds) to app/src/main/res/drawable-nodpi/
- [x] T032 [P] [US1] Add string resources for line details screen in app/src/main/res/values/strings.xml
- [x] T033 [US1] Implement BoxWithConstraints for responsive 1/3 height image calculation in LineDetailsHeader
- [x] T034 [US1] Add dark gradient scrim overlay to header image for text readability using Brush.verticalGradient from Color.Transparent to Color.Black with 0.7f alpha (per FR-004 and SC-004 contrast requirement)
- [x] T035 [US1] Position line icon at bottom-right of header with 16dp padding
- [x] T036 [US1] Position line name and status text bottom-left of header with 16dp padding

**Checkpoint**: At this point, User Story 1 should be fully functional - basic line details screen displays with header, icon, name, and status. Test independently before proceeding.

---

## Phase 4: User Story 2 - View Comprehensive Line Details (Priority: P2)

**Goal**: Display comprehensive line information including disruptions, closures, and crowding data in scrollable body section

**Independent Test**: Navigate to line details screen and verify body section displays disruption descriptions, closure information with dates, crowding indicators. Long text shows preview with "Read more" expansion. Missing data shows "Information not available" indicators.

### Implementation for User Story 2

- [ ] T037 [P] [US2] Create DisruptionCard composable with expand/collapse functionality in app/src/main/java/com/smartcommute/feature/linedetails/ui/components/DisruptionCard.kt
- [ ] T038 [P] [US2] Create ClosureCard composable with schedule and alternative service display in app/src/main/java/com/smartcommute/feature/linedetails/ui/components/ClosureCard.kt
- [ ] T039 [P] [US2] Create CrowdingCard composable with level indicator and color coding in app/src/main/java/com/smartcommute/feature/linedetails/ui/components/CrowdingCard.kt
- [ ] T040 [P] [US2] Create StatusSummaryCard composable for overall line status in app/src/main/java/com/smartcommute/feature/linedetails/ui/components/StatusSummaryCard.kt
- [ ] T041 [P] [US2] Create EmptyStateCard composable for missing data indicators in app/src/main/java/com/smartcommute/feature/linedetails/ui/components/EmptyStateCard.kt
- [ ] T042 [US2] Add LazyColumn body section to LineDetailsScreen with Card components
- [ ] T043 [US2] Implement expandable text with AnimatedVisibility for disruptions using maxLines = 3 when collapsed (per FR-014 requirement for "first 2-3 lines" preview)
- [ ] T044 [US2] Implement expandable text with AnimatedVisibility for closures using maxLines = 3 when collapsed (per FR-014)
- [ ] T045 [US2] Add toggleDisruptionExpansion function to LineDetailsViewModel
- [ ] T046 [US2] Add toggleClosureExpansion function to LineDetailsViewModel
- [ ] T047 [US2] Update LineDetailsUiState.Success to include expandedDisruptions and expandedClosures sets
- [ ] T048 [US2] Add 16dp horizontal padding and 8dp vertical spacing between cards following Material Design
- [ ] T049 [US2] Implement placeholder display when headerImageRes is "placeholder" or missing
- [ ] T050 [US2] Display EmptyStateCard when disruptions list is empty
- [ ] T051 [US2] Display EmptyStateCard when closures list is empty
- [ ] T052 [US2] Display EmptyStateCard when crowding data is null

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - comprehensive line details are now fully displayed with all available information.

---

## Phase 5: User Story 3 - Smooth Visual Transitions (Priority: P3)

**Goal**: Implement shared element transitions for line icon, name, and status text between tube status list and line details screen

**Independent Test**: Navigate to and from line details screen and verify line icon, title, and status text animate smoothly from list item positions to detail screen positions in <400ms. Verify transitions work in both directions (forward and back).

### Implementation for User Story 3

- [ ] T053 [P] [US3] Add sharedElement modifier to line icon in LineDetailsHeader with unique key based on lineId
- [ ] T054 [P] [US3] Add sharedElement modifier to line name Text in LineDetailsHeader with unique key
- [ ] T055 [P] [US3] Add sharedElement modifier to status Text in LineDetailsHeader with unique key
- [ ] T056 [US3] Update LineStatusItem to add matching sharedElement modifiers with same keys in app/src/main/java/com/smartcommute/feature/linestatus/ui/components/LineStatusItem.kt
- [ ] T057 [US3] Wrap navigation in SharedTransitionLayout in AppNavigation
- [ ] T058 [US3] Configure shared element transition duration to <400ms per SC-002 requirement
- [ ] T059 [US3] Test and verify smooth animations at 60fps with Android Profiler
- [ ] T060 [US3] Verify shared elements animate back to original positions when pressing back button

**Checkpoint**: All user stories should now be independently functional with polished shared element transitions completing the feature.

---

## Phase 6: Integration with Tube Status Screen (Prerequisite for Data Population)

**Purpose**: Update Tube Status Screen to fetch and cache detailed line information including disruptions, closures, and crowding

**⚠️ DEPENDENCY NOTE**: This phase MUST be completed before runtime testing of any user story. User stories P1-P3 can be implemented and reviewed without this phase, but functional testing requires actual cached data from the TFL API. Phase 6 can run in parallel with user story implementation but must complete before end-to-end validation.

**Note**: This phase modifies the existing Tube Status Screen feature to populate data that Line Details Screen will display.

- [ ] T061 [P] Add disruption endpoints to TflApiService in app/src/main/java/com/smartcommute/core/network/TflApiService.kt
- [ ] T062 [P] Add closure endpoints to TflApiService
- [ ] T063 [P] Create DTOs for disruptions, closures, and crowding from TFL API
- [ ] T064 Update LineStatusRepositoryImpl to fetch disruptions/closures/crowding during refresh in app/src/main/java/com/smartcommute/feature/linestatus/data/repository/LineStatusRepositoryImpl.kt
- [ ] T065 Implement entity mapping from TFL API DTOs to DisruptionEntity, ClosureEntity, CrowdingEntity
- [ ] T066 Update refreshLineStatuses to cache disruptions using LineDetailsDao.insertDisruptions
- [ ] T067 Update refreshLineStatuses to cache closures using LineDetailsDao.insertClosures
- [ ] T068 Update refreshLineStatuses to cache crowding using LineDetailsDao.insertCrowding
- [ ] T069 Set headerImageRes field based on lineId (e.g., "line_header_bakerloo") during caching
- [ ] T070 Handle comma-separated station names parsing from TFL API for affectedStops and affectedStations fields

**Checkpoint**: Tube Status Screen now populates all tables. Verify data is cached by inspecting database with Device File Explorer.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements, edge case handling, and manual verification

- [ ] T071 [P] Verify text contrast ratio ≥4.5:1 on header image with gradient scrim per SC-004
- [ ] T072 [P] Test screen load time <2 seconds per SC-001 with Android Profiler
- [ ] T073 [P] Test shared element animations complete in <400ms per SC-002
- [ ] T074 [P] Verify images display without pixelation on 3x density devices per SC-003
- [ ] T075 Test device rotation to verify state preservation in ViewModel
- [ ] T076 Test with missing crowding data to verify EmptyStateCard displays
- [ ] T077 Test with no disruptions to verify EmptyStateCard displays
- [ ] T078 Test with very long disruption text to verify "Read more" expansion works
- [ ] T079 Test with lineId not in database to verify error state displays
- [ ] T080 Test back navigation returns to tube status screen with reverse animations
- [ ] T081 Verify no ANR during database queries (queries run on IO dispatcher)
- [ ] T082 Test on multiple screen sizes and aspect ratios for responsive layout
- [ ] T083 Verify TalkBack accessibility support for content descriptions
- [ ] T084 Test dark mode to ensure theme applies correctly
- [ ] T085 Run complete manual test checklist from quickstart.md
- [ ] T086 [P] Code cleanup: Remove unused imports and TODOs
- [ ] T087 [P] Verify all string literals are in strings.xml (no hardcoded text)
- [ ] T088 Final build and APK size verification (<2MB increase for images)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Tube Status Integration (Phase 6)**: Can run in parallel with user stories OR after (required for runtime data population)
- **Polish (Phase 7)**: Depends on desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories ✅ MVP
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Builds on US1 but independently testable
- **User Story 3 (P3)**: Can start after US1 is complete (needs LineDetailsHeader and LineStatusItem to exist)

### Within Each User Story

- Foundation entities and DAOs must exist (Phase 2)
- Repository before ViewModel
- ViewModel before Screen
- UI components can be built in parallel (marked [P])
- Core screen implementation before polish

### Parallel Opportunities

- Phase 1: T002, T003, T004 can run in parallel (different files)
- Phase 2: T008, T009, T010 (entities) can run in parallel
- Phase 2: T013, T014, T015, T016, T017 (domain models) can run in parallel
- User Story 1: T018, T019, T021, T022, T024, T026, T027, T028, T031, T032 can run in parallel
- User Story 2: T037-T041 (all card components) can run in parallel
- User Story 3: T053, T054, T055 (shared element modifiers) can run in parallel
- Phase 6: T061, T062, T063 can run in parallel
- Polish: T071-T074, T086, T087 can run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all parallel tasks for User Story 1 together:
Task: "Create LineDetailsRepository interface"
Task: "Create TubeLineMapper"
Task: "Create LineDetailsModule"
Task: "Create LineDetailsUiState"
Task: "Add LineDetails navigation route"
Task: "Create LineDetailsHeader composable"
Task: "Create LoadingState composable"
Task: "Create ErrorState composable"
Task: "Generate header images"
Task: "Add string resources"
```

---

## Parallel Example: User Story 2

```bash
# Launch all card components for User Story 2 together:
Task: "Create DisruptionCard"
Task: "Create ClosureCard"
Task: "Create CrowdingCard"
Task: "Create StatusSummaryCard"
Task: "Create EmptyStateCard"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (database migration)
2. Complete Phase 2: Foundational (entities and domain models) - CRITICAL
3. Complete Phase 3: User Story 1 (basic line details display)
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready - Users can now view line details with header and status

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP! 🎯)
3. Add User Story 2 → Test independently → Deploy/Demo (Comprehensive details added)
4. Add User Story 3 → Test independently → Deploy/Demo (Polished animations)
5. Complete Phase 6 → Full data population from TFL API
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers after Foundational phase completes:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (T018-T036)
   - Developer B: User Story 2 (T037-T052) after US1 screen exists
   - Developer C: Phase 6 Tube Status Integration (T061-T070)
3. Developer D: User Story 3 after US1 completes (T053-T060)
4. Stories complete and integrate independently

---

## Task Summary

**Total Tasks**: 88

**Tasks per Phase**:
- Phase 1 (Setup): 7 tasks
- Phase 2 (Foundational): 10 tasks (CRITICAL - blocks all stories)
- Phase 3 (User Story 1 - MVP): 19 tasks
- Phase 4 (User Story 2): 16 tasks
- Phase 5 (User Story 3): 8 tasks
- Phase 6 (Tube Status Integration): 10 tasks
- Phase 7 (Polish): 18 tasks

**Parallel Opportunities**: 32 tasks marked [P] can run in parallel

**Independent Test Criteria**:
- ✅ User Story 1: Navigate to screen, see header/icon/name/status
- ✅ User Story 2: Scroll body, see disruptions/closures/crowding cards
- ✅ User Story 3: Navigate back and forth, see smooth <400ms animations

**Suggested MVP Scope**: Phases 1, 2, and 3 only (User Story 1)

---

## Notes

- [P] tasks = different files, no dependencies on incomplete tasks
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- NO automated tests per constitution III - manual verification only
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- User Story 1 is the MVP - can stop after Phase 3 for minimal viable product
- Phases 1 and 2 MUST complete before any user story work begins
- Database migration (Phase 1) and entity creation (Phase 2) are blocking prerequisites
