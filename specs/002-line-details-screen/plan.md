# Implementation Plan: Line Details Screen

**Branch**: `002-line-details-screen` | **Date**: 2025-12-28 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-line-details-screen/spec.md`

## Summary

The Line Details Screen displays comprehensive offline information about a selected London Underground line, including status, disruptions, closures, and crowding data. The screen features a hero header image with shared element transitions from the Tube Status list, Material Design 3 layouts, and operates entirely from local cache without network requests. Technical approach uses Jetpack Compose with MVVM architecture, Room database for offline storage, and Compose's native Shared Element API for smooth animations.

## Technical Context

**Language/Version**: Kotlin 2.3.0 with Java 17 target
**Primary Dependencies**: Jetpack Compose (BOM 2025.12.01), Material Design 3, Hilt 2.57.2, Room 2.8.4, Glide Compose 1.0.0-beta08, Kotlin Coroutines 1.10.2, Navigation Compose 2.9.6
**Storage**: Room database (SQLite) for offline-first line details caching
**Testing**: Manual verification only (no automated tests per constitution III)
**Target Platform**: Android 8.0+ (API 26), Target SDK 36, Compile SDK 36
**Project Type**: Mobile Android application (single activity, Jetpack Compose)
**Performance Goals**: <2 second screen load (SC-001), <400ms shared element animations (SC-002), 60fps scrolling, <10MB memory footprint
**Constraints**: NO network requests allowed in this screen (FR-007), offline-only data access, image quality at 3x density (1440px), WCAG AA contrast (4.5:1 ratio per SC-004)
**Scale/Scope**: 11 London Underground lines, ~15-20 new files, database schema migration (v1→v2), extends existing Line Status feature

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### User-First Design ✅
- **Status**: PASS
- **Evidence**: Spec.md contains 3 prioritized user stories (P1-P3) with acceptance criteria. Each story is independently testable and delivers incremental value. P1 provides core MVP (view basic info), P2 adds comprehensive details, P3 adds polish.

### Specification-Driven Development ✅
- **Status**: PASS
- **Evidence**: Complete spec.md exists with user scenarios, 14 functional requirements (FR-001 to FR-014), 6 success criteria (SC-001 to SC-006), edge cases, assumptions, dependencies, and out-of-scope items. Clarification session completed with 4 questions resolved.

### No Automated Testing ✅
- **Status**: PASS
- **Evidence**: Plan includes no test file creation. Manual verification checkpoints defined in quickstart.md. No test tasks will be generated in tasks.md (enforced by task generation workflow).

### Progressive Planning ✅
- **Status**: PASS
- **Evidence**: Following three-phase approach:
  - Phase completed: Specification (spec.md approved)
  - Phase completed: Clarification (clarifications.md)
  - Current phase: Planning (this document)
  - Next phase: Task generation (/speckit.tasks)
  - Final phase: Implementation (/speckit.implement)

### Independent User Stories ✅
- **Status**: PASS
- **Evidence**:
  - P1 (View Basic Line Information): MVP - displays header, status, shared element transitions. Can be deployed independently as functional enhancement.
  - P2 (View Comprehensive Details): Builds on P1, adds disruptions/closures/crowding display. Can be deployed without P3.
  - P3 (Smooth Transitions): Pure UX polish, enhances but not required for P1/P2 functionality.

### Simplicity and YAGNI ✅
- **Status**: PASS
- **Evidence**: No premature abstractions added. Uses existing patterns (MVVM, Repository, Room DAO). No generic frameworks created. Expandable text uses built-in AnimatedVisibility (not custom animation framework). No caching layers beyond Room. No pagination (data size small). Complexity limited to specification requirements only.

### Explicit Over Implicit ✅
- **Status**: PASS
- **Evidence**: All technical decisions documented in research.md. Concrete file paths in quickstart.md. Entity schemas fully defined in data-model.md. No placeholders in contracts. No ambiguous "will be determined later" statements. Migration SQL explicit in repository-contract.md.

### Android Architecture Requirements ✅
- **Status**: PASS
- **Evidence**:
  - MVVM: LineDetailsViewModel + LineDetailsScreen composable
  - Single Activity: Extends existing MainActivity with Compose Navigation
  - Repository Pattern: LineDetailsRepository + LineDetailsRepositoryImpl
  - Dependency Injection: Hilt with LineDetailsModule
  - Reactive Streams: StateFlow for UI state, Flow from repository

### Android UI/UX Standards ✅
- **Status**: PASS
- **Evidence**:
  - Material Design 3: Uses Material3 Card, TopAppBar, Button, Text components
  - Jetpack Compose: 100% Compose implementation (no XML layouts)
  - Dark Mode: Inherits from existing theme (SmartCommuteTheme supports dark mode)
  - Accessibility: Content descriptions on icons, TalkBack navigation support, 4.5:1 contrast requirement (SC-004)
  - Localization: String resources in strings.xml (no hardcoded text in composables)

### Android Data & State Management ✅
- **Status**: PASS
- **Evidence**:
  - ViewModels: UI state in LineDetailsViewModel, not in composables
  - StateFlow: `StateFlow<LineDetailsUiState>` exposes state to UI
  - Room Database: Extends existing LineStatusDatabase with new entities
  - DataStore: N/A (no user preferences in this feature)
  - WorkManager: N/A (no background tasks in this feature)
  - Offline-First: Explicit requirement (FR-007), no network access allowed

### Android Performance Standards ✅
- **Status**: PASS
- **Evidence**:
  - Lazy Loading: LazyColumn for scrollable content in body
  - Image Loading: Glide with Compose integration, lifecycle-aware
  - Main Thread: Repository uses `flowOn(Dispatchers.IO)`, database queries on IO dispatcher
  - Coroutines: viewModelScope for lifecycle-aware coroutines
  - Startup Time: N/A (feature screen, not app startup)
  - Memory: No Activity/Context references in ViewModel, proper lifecycle handling

### Android Security Standards ✅
- **Status**: PASS
- **Evidence**:
  - Network Security: N/A (no network requests in this screen)
  - Data Encryption: N/A (public TFL data, no sensitive info)
  - ProGuard/R8: Existing project configuration applies
  - Permissions: No new permissions required
  - Authentication: N/A (no auth in this feature)
  - API Keys: N/A (no network calls, API key used by Tube Status Screen only)

### Android Code Organization ✅
- **Status**: PASS
- **Evidence**: Feature-based package structure:
```
com.smartcommute.feature.linedetails/
├── data/
│   ├── local/entity/  (entities)
│   ├── local/dao/     (DAO)
│   ├── mapper/        (entity-to-domain)
│   └── repository/    (repository impl)
├── domain/
│   ├── model/         (domain models)
│   └── repository/    (repository interface)
└── ui/
    ├── components/    (reusable UI)
    └── LineDetailsScreen.kt
```

### **GATE RESULT: PASS** ✅

All constitution principles satisfied. Proceed to Phase 0 research.

### Post-Phase 1 Re-Check ✅

**Status**: PASS

**Changes Since Initial Check**: Design phase completed. All unknowns resolved via research.md. Data model and contracts define concrete implementation. No new violations introduced.

**Confirmed**:
- No test infrastructure added
- MVVM architecture maintained
- Repository pattern justified (existing pattern, not over-engineering)
- Room database extensions follow existing schema patterns
- Hilt modules consistent with existing DI setup
- No premature abstractions beyond spec requirements

**Ready for Phase 2**: Task generation (/speckit.tasks)

## Project Structure

### Documentation (this feature)

```text
specs/002-line-details-screen/
├── spec.md              # Feature specification (COMPLETE)
├── plan.md              # This file (IN PROGRESS)
├── research.md          # Phase 0 output (COMPLETE)
├── data-model.md        # Phase 1 output (COMPLETE)
├── quickstart.md        # Phase 1 output (COMPLETE)
├── contracts/
│   └── repository-contract.md  # Phase 1 output (COMPLETE)
├── checklists/
│   └── requirements.md  # Spec validation checklist (COMPLETE)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT YET CREATED)
```

### Source Code (repository root)

```text
app/src/main/java/com/smartcommute/
├── core/
│   ├── di/
│   │   ├── AppModule.kt              # Existing
│   │   ├── DatabaseModule.kt         # TO MODIFY (add MIGRATION_1_2, provide LineDetailsDao)
│   │   └── LineDetailsModule.kt      # NEW (bind LineDetailsRepository)
│   ├── navigation/
│   │   ├── NavigationScreen.kt       # TO MODIFY (add LineDetails route)
│   │   └── AppNavigation.kt          # TO MODIFY (add LineDetails composable)
│   ├── network/
│   │   └── TflApiService.kt          # TO MODIFY (add disruption/closure endpoints - Tube Status Screen integration)
│   └── ui/theme/
│       ├── Color.kt                  # Existing (already has line colors)
│       └── Theme.kt                  # Existing
├── feature/
│   ├── linestatus/                   # Existing feature (Tube Status Screen)
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── LineStatusDatabase.kt        # TO MODIFY (add new entities, increment version)
│   │   │   │   ├── dao/
│   │   │   │   │   └── LineStatusDao.kt         # Existing
│   │   │   │   └── entity/
│   │   │   │       └── LineStatusEntity.kt      # Existing
│   │   │   ├── remote/
│   │   │   │   └── mapper/LineStatusMapper.kt   # Existing
│   │   │   └── repository/
│   │   │       └── LineStatusRepositoryImpl.kt  # TO MODIFY (add cache methods)
│   │   ├── domain/
│   │   │   └── model/UndergroundLine.kt         # Existing
│   │   └── ui/
│   │       ├── LineStatusScreen.kt              # TO MODIFY (add onLineClick parameter)
│   │       └── components/
│   │           └── LineStatusItem.kt            # TO MODIFY (add shared element keys)
│   └── linedetails/                  # NEW FEATURE (this implementation)
│       ├── data/
│       │   ├── local/
│       │   │   ├── dao/
│       │   │   │   └── LineDetailsDao.kt                 # NEW
│       │   │   └── entity/
│       │   │       ├── LineDetailsEntity.kt              # NEW
│       │   │       ├── DisruptionEntity.kt               # NEW
│       │   │       ├── ClosureEntity.kt                  # NEW
│       │   │       └── CrowdingEntity.kt                 # NEW
│       │   ├── mapper/
│       │   │   └── LineDetailsMapper.kt                  # NEW
│       │   └── repository/
│       │       └── LineDetailsRepositoryImpl.kt          # NEW
│       ├── domain/
│       │   ├── model/
│       │   │   ├── UndergroundLineDetails.kt             # NEW
│       │   │   ├── Disruption.kt                         # NEW
│       │   │   ├── Closure.kt                            # NEW
│       │   │   ├── Crowding.kt                           # NEW
│       │   │   └── StatusSeverity.kt                     # NEW (if not exists)
│       │   └── repository/
│       │       └── LineDetailsRepository.kt              # NEW (interface)
│       └── ui/
│           ├── LineDetailsScreen.kt                      # NEW (main composable)
│           ├── LineDetailsViewModel.kt                   # NEW
│           ├── LineDetailsUiState.kt                     # NEW
│           └── components/
│               ├── LineDetailsHeader.kt                  # NEW
│               ├── DisruptionCard.kt                     # NEW
│               ├── ClosureCard.kt                        # NEW
│               ├── CrowdingCard.kt                       # NEW
│               ├── StatusSummaryCard.kt                  # NEW
│               └── EmptyStateCard.kt                     # NEW
└── ui/
    └── MainScreen.kt                 # Existing (no changes needed)

app/src/main/res/
├── drawable-nodpi/
│   ├── line_header_bakerloo.webp     # NEW (11 images total)
│   ├── line_header_central.webp      # NEW
│   ├── line_header_circle.webp       # NEW
│   ├── line_header_district.webp     # NEW
│   ├── line_header_hammersmith_city.webp  # NEW
│   ├── line_header_jubilee.webp      # NEW
│   ├── line_header_metropolitan.webp # NEW
│   ├── line_header_northern.webp     # NEW
│   ├── line_header_piccadilly.webp   # NEW
│   ├── line_header_victoria.webp     # NEW
│   └── line_header_waterloo_city.webp  # NEW
└── values/
    └── strings.xml                   # TO MODIFY (add new string resources)
```

**Structure Decision**: Android mobile application with single Activity and feature-based package organization. All new code follows existing patterns in `com.smartcommute.feature.linestatus/`. Database entities and DAOs extend existing Room infrastructure. Compose UI components follow existing Material3 setup. No test directories created (per constitution III).

## Complexity Tracking

**No violations** - All complexity is justified by specification requirements and follows existing architectural patterns in the codebase. No unjustified abstractions added.

## Phase 0: Research (COMPLETE)

**Status**: ✅ COMPLETE

**Output**: [research.md](./research.md)

**Key Decisions Made**:
1. Shared Element Transitions → Compose native API (compose-animation 1.7.0+)
2. Gradient Scrim → Brush.verticalGradient() with Color.Transparent to Black(0.7f alpha)
3. Station Images → AI-generated WebP at 1440x960px, anonymized crowds
4. Expandable Text → AnimatedVisibility with maxLines toggle
5. Offline Access → Repository pattern reading Room database only
6. M3 Detail Screen → Hero header + scrollable Card sections
7. Aspect Ratio Handling → BoxWithConstraints with maxHeight * 0.33f
8. Lifecycle → ViewModel + StateFlow with viewModelScope

**All Unknowns Resolved**: No "NEEDS CLARIFICATION" markers remain. Ready for Phase 1.

## Phase 1: Design & Contracts (COMPLETE)

**Status**: ✅ COMPLETE

**Outputs**:
- [data-model.md](./data-model.md) - Entity schemas, domain models, DAOs, mappers
- [contracts/repository-contract.md](./contracts/repository-contract.md) - Repository interface, error handling, integration points
- [quickstart.md](./quickstart.md) - Step-by-step implementation guide

**Data Model Summary**:
- 4 new Room entities: LineDetailsEntity, DisruptionEntity, ClosureEntity, CrowdingEntity
- Foreign key relationships with CASCADE delete
- Database migration v1→v2 SQL defined
- Entity-to-domain mappers specified
- 5 domain models for UI layer

**Contracts Summary**:
- LineDetailsRepository interface with offline-only access
- NetworkResult<T> sealed class for state management
- LineDetailsDao with @Transaction query for complete details
- Navigation route with lineId parameter
- Hilt bindings for DI

**Quickstart Summary**:
- 12-step implementation guide
- Estimated 8-12 hours for experienced developer
- Manual testing checklists
- Troubleshooting guide
- Integration points with Tube Status Screen

## Phase 2: Tasks (PENDING)

**Status**: ⏳ PENDING

**Next Command**: `/speckit.tasks`

**Expected Output**: `tasks.md` with prioritized, actionable tasks mapped to user stories (P1, P2, P3)

**Task Categories** (to be generated):
- Database schema migration tasks
- Entity and DAO creation tasks
- Repository and mapper implementation tasks
- ViewModel and UI state tasks
- Composable UI component tasks
- Navigation integration tasks
- Image asset preparation tasks
- Manual testing verification tasks

**No Test Tasks**: Per constitution III, no automated test tasks will be generated

## Implementation Roadmap

### Pre-Implementation Requirements

- [x] Spec.md approved
- [x] Clarifications resolved
- [x] Research decisions documented
- [x] Data model designed
- [x] Contracts defined
- [x] Quickstart guide created
- [ ] Tasks generated (/speckit.tasks)
- [ ] Tube Status Screen updated to cache line details (prerequisite)

### Implementation Phases (Post-Task Generation)

**Phase P1 Implementation** (MVP - Core Functionality):
- Database migration v1→v2
- Create entities, DAOs, mappers
- Implement repository and ViewModel
- Create basic UI: header + status display
- Add navigation integration
- Implement shared element transitions
- Add 11 header images

**Phase P2 Implementation** (Enhanced Information):
- Add DisruptionCard with expand/collapse
- Add ClosureCard with expand/collapse
- Add CrowdingCard indicator
- Implement missing data placeholders
- Add empty state handling

**Phase P3 Implementation** (Polish):
- Refine shared element transitions (<400ms)
- Optimize animation performance (60fps)
- Add loading and error states UI polish
- Verify WCAG contrast ratios
- Test edge cases (rotation, missing data, etc.)

### Success Metrics Verification

Post-implementation, manually verify:
- [ ] SC-001: Screen loads in <2 seconds
- [ ] SC-002: Animations complete in <400ms
- [ ] SC-003: Images sharp at 3x density
- [ ] SC-004: Text contrast ≥4.5:1 ratio
- [ ] SC-005: Works without network
- [ ] SC-006: 95% users succeed on first try (user testing)

## Dependencies & Integration Points

### External Dependencies (Already in build.gradle.kts)
- Jetpack Compose BOM 2025.12.01 ✅
- Material Design 3 ✅
- Hilt 2.57.2 ✅
- Room 2.8.4 ✅
- Navigation Compose 2.9.6 ✅
- Glide Compose 1.0.0-beta08 ✅
- Kotlin Coroutines 1.10.2 ✅
- Kotlin Serialization 1.9.0 ✅

**No new dependencies required** ✅

### Internal Dependencies

**Depends On**:
- LineStatusDatabase (existing) - will be extended
- LineStatusRepository (existing) - will be extended with cache methods
- NavigationScreen (existing) - will add LineDetails route
- SmartCommuteTheme (existing) - provides Material3 theming

**Provides To**:
- Navigation graph - new LineDetails screen route
- Users - detailed offline line information

### Integration with Tube Status Screen (Critical Path)

**Required Changes to Existing Feature**:
1. Extend TflApiService with disruption/closure endpoints
2. Update LineStatusRepositoryImpl to fetch and cache detailed data
3. Add onLineClick callback to LineStatusScreen
4. Add shared element keys to LineStatusItem

**Data Flow**:
```
Tube Status Screen (fetch TFL API)
    ↓
LineStatusRepository (cache to Room)
    ↓
Room Database (line_details, disruptions, closures, crowding tables)
    ↓
LineDetailsRepository (read-only access)
    ↓
LineDetailsViewModel (expose StateFlow)
    ↓
LineDetailsScreen (display UI)
```

## Risk Assessment

### Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Database migration fails on user devices | HIGH | LOW | Test migration thoroughly, add fallbackToDestructiveMigration(), log migration errors |
| Shared element transitions janky | MEDIUM | MEDIUM | Profile animation performance, reduce animation complexity if needed, target <400ms |
| Image assets too large (APK size) | MEDIUM | LOW | Use WebP compression at 85%, keep images under 200KB each (~2MB total) |
| TFL API doesn't provide all required data | HIGH | MEDIUM | Design graceful degradation, show "Information not available" for missing fields |
| Screen load time exceeds 2 seconds | MEDIUM | LOW | Optimize database queries with @Transaction, use proper indexing |

### Implementation Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Scope creep (adding features beyond spec) | MEDIUM | MEDIUM | Strict adherence to spec requirements, YAGNI principle enforcement |
| Tube Status Screen integration breaks existing functionality | HIGH | LOW | Careful testing of Tube Status Screen after repository changes |
| Forgot to handle edge cases | MEDIUM | MEDIUM | Use edge cases checklist in spec.md, manual testing verification |

## Compliance Verification

### Specification Requirements Coverage

All 14 functional requirements (FR-001 to FR-014) covered in design:
- FR-001 to FR-006: Header layout and shared element transitions → LineDetailsHeader component
- FR-007: Offline-only access → Repository reads Room database only, no network injection
- FR-008: Comprehensive info display → Disruption, Closure, Crowding cards
- FR-009: High-res images → 1440x960px WebP in drawable-nodpi
- FR-010: Material Design → Material3 components throughout
- FR-011: Anonymized images → Specified in research.md and quickstart.md
- FR-012: Placeholder for missing images → Fallback logic in image loading
- FR-013: Missing data indicators → EmptyStateCard component
- FR-014: Expandable long text → AnimatedVisibility with maxLines

### Success Criteria Coverage

All 6 success criteria (SC-001 to SC-006) verifiable:
- SC-001: <2s load time → Database query optimization, single transaction
- SC-002: <400ms animations → Compose shared element API, manual profiling
- SC-003: 3x density support → 1440px images, WebP format
- SC-004: 4.5:1 contrast → Dark gradient scrim with 0.7 alpha
- SC-005: Offline operation → No network calls, Room database only
- SC-006: 95% success rate → User testing post-implementation

### Constitution Compliance

All 16 constitution principles satisfied (see Constitution Check section above).

## Appendix: File Change Summary

### New Files (26)

**Data Layer** (11 files):
- LineDetailsEntity.kt
- DisruptionEntity.kt
- ClosureEntity.kt
- CrowdingEntity.kt
- LineDetailsDao.kt
- LineDetailsMapper.kt
- LineDetailsRepositoryImpl.kt
- UndergroundLineDetails.kt
- Disruption.kt
- Closure.kt
- Crowding.kt

**Domain Layer** (2 files):
- LineDetailsRepository.kt (interface)
- StatusSeverity.kt (enum, if not exists)

**UI Layer** (8 files):
- LineDetailsScreen.kt
- LineDetailsViewModel.kt
- LineDetailsUiState.kt
- LineDetailsHeader.kt
- DisruptionCard.kt
- ClosureCard.kt
- CrowdingCard.kt
- StatusSummaryCard.kt
- EmptyStateCard.kt

**DI Layer** (1 file):
- LineDetailsModule.kt

**Resources** (11 images):
- line_header_*.webp (11 files, one per line)

### Modified Files (9)

- LineStatusDatabase.kt (add entities, increment version)
- DatabaseModule.kt (add migration, provide DAO)
- NavigationScreen.kt (add LineDetails route)
- AppNavigation.kt (add LineDetails composable)
- LineStatusScreen.kt (add onLineClick parameter)
- LineStatusItem.kt (add shared element keys)
- TflApiService.kt (add disruption/closure endpoints)
- LineStatusRepositoryImpl.kt (add cache methods)
- strings.xml (add new string resources)

### Total Impact

- **New files**: 26
- **Modified files**: 9
- **Lines of code estimate**: ~2500-3000 LOC
- **Database schema change**: Yes (v1 → v2 migration)
- **API changes**: Yes (extend TFL API service)
- **Breaking changes**: No

---

**Plan Status**: ✅ COMPLETE (Phases 0 and 1 finished)

**Next Step**: Run `/speckit.tasks` to generate actionable task list

**Estimated Total Implementation Time**: 8-12 hours (per quickstart.md)
