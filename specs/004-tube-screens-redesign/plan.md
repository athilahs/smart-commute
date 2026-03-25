# Implementation Plan: Tube Status & Line Details Screen Redesign

**Branch**: `004-tube-screens-redesign` | **Date**: 2026-03-25 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-tube-screens-redesign/spec.md`

## Summary

Redesign the tube status list screen and line details screen to match new Figma designs. The tube status screen gets updated list items with circular tinted icon containers, colour-coded status text, and outlined trailing status icons. The line details screen replaces the header image/gradient design with a clean hero section (centred icon, name, status) and card-based information layout (Operation Hours, Night Tube, Crowding). Shared element transitions for the icon, name, and status are preserved between screens. This is a UI-only change — no data layer modifications required.

## Technical Context

**Language/Version**: Kotlin 2.3.0 / Java 17 target
**Primary Dependencies**: Jetpack Compose (BOM 2025.12.01), Material Design 3, Hilt 2.57.2, Navigation Compose 2.9.6, Glide Compose 1.0.0-beta08, Kotlin Coroutines 1.10.2
**Storage**: Room 2.8.4 (existing, no changes)
**Testing**: None (Constitution Principle III — No Automated Testing)
**Target Platform**: Android (minSdk 26, targetSdk 36, compileSdk 36)
**Project Type**: Mobile (Android, single module)
**Performance Goals**: 60 fps during shared element transitions, instant screen load from local cache
**Constraints**: Offline-capable (all line details from local cache), no network requests on line details screen
**Scale/Scope**: 2 screens redesigned, ~10 composable files modified/created

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. User-First Design | PASS | Spec has documented user scenarios and acceptance criteria (3 user stories with priorities) |
| II. Specification-Driven Development | PASS | Complete spec.md approved with functional requirements, success criteria, edge cases |
| III. No Automated Testing | PASS | No test tasks will be created. Manual verification via quickstart.md |
| IV. Progressive Planning | PASS | Spec → Plan → Tasks workflow followed |
| V. Independent User Stories | PASS | P1 (tube status list) can be delivered independently; P2 (line details) builds on P1; P3 (transitions) is polish |
| VI. Simplicity and YAGNI | PASS | UI-only changes, no new abstractions, hardcoded data kept as-is |
| VII. Explicit Over Implicit | PASS | All colours, sizes, and data sources documented explicitly. No NEEDS CLARIFICATION markers remain |
| VIII. Proactive Documentation Access | PASS | Figma designs referenced directly for authoritative design specs |
| MVVM Pattern | PASS | No ViewModel changes — existing ViewModels and StateFlows reused |
| Material Design 3 | PASS | New components follow MD3 guidelines |
| Jetpack Compose | PASS | All UI changes in Compose |
| Package by Feature | PASS | Changes scoped to existing feature packages (linestatus, linedetails) |

**Post-Phase 1 Re-check**: All gates still pass. No new abstractions, patterns, or data layer changes introduced.

## Project Structure

### Documentation (this feature)

```text
specs/004-tube-screens-redesign/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0: data availability, colour mapping, design decisions
├── data-model.md        # Phase 1: existing model mapping (no changes)
├── quickstart.md        # Phase 1: build/run/verify guide
├── contracts/
│   └── ui-contract.md   # Phase 1: UI component input/output contracts
├── checklists/
│   └── requirements.md  # Spec quality validation
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
app/src/main/java/com/smartcommute/
├── core/
│   ├── navigation/
│   │   └── AppNavigation.kt              # No changes expected (transition keys unchanged)
│   └── ui/theme/
│       └── Color.kt                      # MODIFY: Update status colours to Figma values
│
├── feature/
│   ├── linestatus/
│   │   └── ui/
│   │       ├── LineStatusScreen.kt       # MODIFY: Minor header layout adjustments
│   │       └── components/
│   │           ├── LineStatusItem.kt      # MODIFY: Updated layout — tinted icon, new text styles
│   │           └── StatusIndicator.kt     # MODIFY: Outlined icons, new colours
│   │
│   └── linedetails/
│       └── ui/
│           ├── LineDetailsScreen.kt       # MODIFY: Replace LazyColumn content — hero + cards
│           └── components/
│               ├── LineDetailsHeader.kt   # REWRITE: Hero section (centred icon, name, status)
│               ├── OperationHoursCard.kt  # NEW: Hardcoded operation hours card
│               ├── NightTubeCard.kt       # NEW: Night tube info card (line-ID based)
│               ├── CrowdingInformationCard.kt  # NEW: Redesigned crowding card
│               ├── StatusSummaryCard.kt   # DELETE: Replaced by hero section
│               ├── LineInfoCard.kt        # DELETE: Split into OperationHoursCard + NightTubeCard
│               ├── DisruptionCard.kt      # DELETE: Not in new Figma design
│               ├── ClosureCard.kt         # DELETE: Not in new Figma design
│               ├── CrowdingCard.kt        # DELETE: Replaced by CrowdingInformationCard
│               └── EmptyStateCard.kt      # DELETE: No empty states in new design
```

**Structure Decision**: Existing feature-based package structure is maintained. Changes are scoped entirely to UI composable files within `feature/linestatus/ui/` and `feature/linedetails/ui/`. No new packages or modules are introduced.

## Phase 2: Implementation Approach

### Story P1 — Redesigned Tube Status List

**Files to modify**:

1. **`Color.kt`** — Update status colour constants:
   - `GoodServiceColor`: `#4CAF50` → `#00A63E`
   - `MinorDelaysColor`: `#FFC107` → `#FE9A00`
   - `MajorDelaysColor`: `#FF9800` → `#F54900`
   - Add/update `SevereDelaysColor`, `ClosureColor`, `ServiceDisruptionColor` to derived values

2. **`LineStatusItem.kt`** — Update list item composable:
   - Icon container: Change to ~64dp circular `Box` with `lineColor.copy(alpha = 0.15f)` background
   - Roundel icon: Keep existing Glide-loaded icon, adjust size to ~36dp inside container
   - Line name: Update to 18sp, `FontWeight.Medium`, colour `#0A0A0A`
   - Status text: Update to 14sp, `FontWeight.Medium`, use new colour mapping from `StatusType`
   - Trailing status icon: Replace current `StatusIndicator` with new outlined icon style
   - Row height: Adjust to match Figma (~97dp with padding)
   - Bottom border: Thin divider `0.686dp` with `rgba(0,0,0,0.1)` (from Figma)
   - Shared element modifiers: Keep existing keys — `line_icon_{lineId}`, `line_name_{lineId}`, `line_status_{lineId}`

3. **`StatusIndicator.kt`** — Replace filled circle design with outlined icons:
   - Good Service: Outlined `CheckCircle` icon, green stroke
   - Minor Delays: Outlined `Warning` triangle icon, amber stroke
   - Major Delays / Severe / Closure: Outlined `Error` circle icon, red stroke
   - Service Disruption: Outlined `Warning` icon, grey stroke
   - Size: ~28dp (from Figma) instead of current 40dp
   - Remove filled background circle — icon only with colour

4. **`LineStatusScreen.kt`** — Minor adjustments:
   - Keep existing refresh button unchanged (per FR-005)
   - Verify header title and "Last updated" text match Figma layout
   - Preserve pull-to-refresh, offline banner, error states, loading states

### Story P2 — Redesigned Line Details Screen

**Files to create**:

5. **`OperationHoursCard.kt`** — New card composable:
   - White card with 14dp rounded corners, subtle border (`rgba(0,0,0,0.1)`)
   - Header: Clock icon (Material `Schedule`) + "Operation Hours" text (16sp medium)
   - Three data rows (justified space-between):
     - "Monday - Friday" → "05:30 - 00:15"
     - "Saturday" → "05:45 - 00:15"
     - "Sunday" → "06:30 - 23:45"
   - Labels in grey (#4A5565), values in black (#0A0A0A)
   - All data hardcoded — no parameters needed

6. **`NightTubeCard.kt`** — New card composable:
   - Same card styling as Operation Hours
   - Header: Moon icon (Material `NightsStay`) + "Night Tube" text
   - Body: "24-hour service on Friday and Saturday nights." (14sp, #0A0A0A)
   - Footer: "Frequency: " (grey) + "Every 10 mins" (black)
   - Parameter: `lineId: String` — card only renders when line is central/jubilee/northern/piccadilly/victoria

7. **`CrowdingInformationCard.kt`** — New card composable:
   - Same card styling as Operation Hours
   - Header: People icon (Material `Groups`) + "Crowding Information" text
   - Two sub-sections:
     - "Peak Times" label (grey, 16sp) + description "Very busy 08:00-09:30, 17:00-18:30" (14sp, black)
     - "Off-Peak" label (grey, 16sp) + description "Moderate throughout the day" (14sp, black)
   - Parameter: `crowding: Crowding` — card only shown when crowding data is non-null
   - Peak/off-peak descriptions are hardcoded; `crowding.level` can supplement off-peak text

**Files to rewrite**:

8. **`LineDetailsHeader.kt`** → **Hero section**:
   - Remove: gradient background, station image, dark scrim, bottom-aligned text, overlapping icon
   - Add: White background section at top
   - Centred large (~80dp) circular container with `brandColor.copy(alpha = 0.15f)` background
   - Centred roundel icon (~52dp) inside container
   - Centred line name (30sp, bold, #0A0A0A) — shared element `line_name_{lineId}`
   - Centred status: icon + text (18sp, colour-coded) — shared element `line_status_{lineId}`
   - Centred "Last updated" timestamp (14sp, #4A5565)
   - Keep shared element modifiers with existing keys

**Files to modify**:

9. **`LineDetailsScreen.kt`** — Replace screen content:
   - Remove: Collapsing `TopAppBar` with scroll-based opacity, staggered animation logic
   - Add: Simple fixed `TopAppBar` with back arrow + "Back" text
   - Replace `LazyColumn` content:
     - Item 1: Hero section (LineDetailsHeader)
     - Item 2: OperationHoursCard (always shown)
     - Item 3: NightTubeCard (conditional on lineId)
     - Item 4: CrowdingInformationCard (conditional on crowding != null)
   - Background: `#F9FAFB` for the scrollable area below hero
   - Remove references to StatusSummaryCard, LineInfoCard, DisruptionCard, ClosureCard, CrowdingCard, EmptyStateCard
   - Keep Loading and Error states

**Files to delete**:

10. Remove unused components:
    - `StatusSummaryCard.kt` — replaced by hero section
    - `LineInfoCard.kt` — split into OperationHoursCard + NightTubeCard
    - `DisruptionCard.kt` — not in new Figma design
    - `ClosureCard.kt` — not in new Figma design
    - `CrowdingCard.kt` — replaced by CrowdingInformationCard
    - `EmptyStateCard.kt` — no empty states in new design

### Story P3 — Shared Element Transitions

**No new files** — transitions are maintained through existing infrastructure:

11. **Verify transitions work with new layout positions**:
    - The `sharedElement()` modifier on icon, name, and status composables in both `LineStatusItem` and `LineDetailsHeader` (hero) will automatically animate between the old list positions and new centred positions
    - Transition keys remain: `line_icon_{lineId}`, `line_name_{lineId}`, `line_status_{lineId}`
    - `SharedTransitionLayout` in `AppNavigation.kt` unchanged
    - Animation spec (500ms tween with FastOutSlowInEasing) unchanged
    - May need to adjust `skipToLookaheadSize()` or bounds transform if the size difference between list and hero elements causes visual artefacts

## Complexity Tracking

> No constitution violations. No complexity tracking needed.

No new abstractions, patterns, or architectural changes are introduced. All changes are direct composable modifications within existing feature packages.
