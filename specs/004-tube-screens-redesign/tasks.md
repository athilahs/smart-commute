# Tasks: Tube Status & Line Details Screen Redesign

**Input**: Design documents from `/specs/004-tube-screens-redesign/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/ui-contract.md

**Tests**: None (Constitution Principle III — No Automated Testing)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Update shared theme colours that both screens depend on

- [x] T001 Update status colour constants in `app/src/main/java/com/smartcommute/core/ui/theme/Color.kt` — change `GoodServiceColor` from `#4CAF50` to `#00A63E`, `MinorDelaysColor` from `#FFC107` to `#FE9A00`, `MajorDelaysColor` from `#FF9800` to `#F54900`, add/update `SevereDelaysColor` to `#E53E3E`, `ClosureColor` to `#DC2626`, `ServiceDisruptionColor` to `#6B7280`. Ensure all composables referencing these colours pick up the new values. Reference: `specs/004-tube-screens-redesign/data-model.md` Status Color Mapping table.

---

## Phase 2: User Story 1 — Redesigned Tube Status List (Priority: P1) MVP

**Goal**: Redesign each tube line list item to match the Figma design: circular tinted icon container, colour-coded status text, and outlined trailing status icons. Keep the existing refresh button unchanged.

**Independent Test**: Launch the app and verify the status list matches the Figma design (node 2:184). Each line shows a circular roundel in a tinted background, line name as heading, colour-coded status subtitle, and an outlined trailing icon (checkmark/warning/error).

### Implementation for User Story 1

- [x] T002 [P] [US1] Rewrite `StatusIndicator` composable in `app/src/main/java/com/smartcommute/feature/linestatus/ui/components/StatusIndicator.kt` — remove the filled 40dp background circle design. Replace with outlined-only icons at 28dp: `CheckCircle` outlined in green (`#00A63E`) for GOOD_SERVICE, `Warning` outlined in amber (`#FE9A00`) for MINOR_DELAYS, `Error` outlined in red/orange (`#F54900`) for MAJOR_DELAYS, `Error` outlined in red (`#E53E3E`) for SEVERE_DELAYS, `Error` outlined in dark red (`#DC2626`) for CLOSURE, `Warning` outlined in grey (`#6B7280`) for SERVICE_DISRUPTION. No filled background — icon only with colour tint. Reference: `specs/004-tube-screens-redesign/contracts/ui-contract.md` Status → Colour + Icon Mapping table.

- [x] T003 [US1] Redesign `LineStatusItem` composable in `app/src/main/java/com/smartcommute/feature/linestatus/ui/components/LineStatusItem.kt` — update the Row layout to match Figma proportions: (1) Icon container: 64dp circular `Box` with `lineColor.copy(alpha = 0.15f)` background, roundel icon at 36dp inside. (2) Text column with 16dp gap from icon: line name at 18sp `FontWeight.Medium` colour `#0A0A0A`, status text at 14sp `FontWeight.Medium` with colour from `StatusType` mapping (green/amber/red). (3) Trailing: updated `StatusIndicator` (from T002) at 28dp. (4) Row padding: 16dp horizontal. (5) Bottom border: `0.686dp` divider with `Color.Black.copy(alpha = 0.1f)`. (6) CRITICAL: Preserve existing `sharedElement()` modifiers on the icon (`line_icon_{lineId}`), name (`line_name_{lineId}`), and status (`line_status_{lineId}`) — do NOT remove or rename these keys.

- [x] T004 [US1] Adjust `LineStatusScreen` header layout in `app/src/main/java/com/smartcommute/feature/linestatus/ui/LineStatusScreen.kt` — verify the "London Underground" title and "Last updated: X" subtitle match the Figma layout. Keep the existing refresh button design unchanged (FR-005). Preserve all existing functionality: pull-to-refresh, offline banner, error states, loading states, caching behaviour, and bottom navigation integration. No structural changes to the screen — only cosmetic alignment of the header section if needed.

**Checkpoint**: At this point, the tube status list screen should display with the new Figma design. All existing functionality (refresh, offline, errors) continues to work. The app can be demo'd at this point as MVP.

---

## Phase 3: User Story 2 — Redesigned Line Details Screen (Priority: P2)

**Goal**: Replace the current header image/gradient line details screen with a clean hero section and card-based information layout. The hero section shows centred icon, name, status, and timestamp. Below, cards display operation hours, night tube, and crowding info. Hidden when data unavailable.

**Independent Test**: Tap any line from the status list and verify the details screen matches the Figma design (node 2:480): centred hero section with large tinted icon, bold name, colour-coded status, and timestamp. Below: Operation Hours card (always), Night Tube card (Central/Jubilee/Northern/Piccadilly/Victoria only), Crowding Information card (when data available). Cards not shown when data is unavailable.

### Implementation for User Story 2

- [x] T005 [P] [US2] Create `OperationHoursCard` composable in `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/OperationHoursCard.kt` — white card with 14dp rounded corners and `0.686dp` border in `Color.Black.copy(alpha = 0.1f)`. Internal padding 24dp. Header row: Material `Schedule` icon (20dp, `#0A0A0A`) + "Operation Hours" text (16sp, `FontWeight.Medium`, `#0A0A0A`) with 8dp gap. Below header (24dp gap): three `Row(horizontalArrangement = SpaceBetween)` items: "Monday - Friday" / "05:30 - 00:15", "Saturday" / "05:45 - 00:15", "Sunday" / "06:30 - 23:45". Labels in `#4A5565` (16sp regular), values in `#0A0A0A` (16sp regular). 12dp vertical gap between rows. All data hardcoded — no parameters. Reference: Figma node 2:504.

- [x] T006 [P] [US2] Create `NightTubeCard` composable in `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/NightTubeCard.kt` — same card styling as OperationHoursCard (14dp corners, border, 24dp internal padding). Header row: Material `NightsStay` icon (20dp) + "Night Tube" text (16sp medium). Body (24dp below header): "24-hour service on Friday and Saturday nights." (14sp, `#0A0A0A`). Below (12dp gap): "Frequency: " in `#4A5565` (16sp) + "Every 10 mins" in `#0A0A0A` (16sp) as a `buildAnnotatedString` or two `Text` composables in a Row. Parameter: `lineId: String`. Card content only renders when `lineId.lowercase()` is one of: "central", "jubilee", "northern", "piccadilly", "victoria". For other lines, the composable should not emit any content (return early / empty). Reference: Figma node 2:527.

- [x] T007 [P] [US2] Create `CrowdingInformationCard` composable in `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/CrowdingInformationCard.kt` — same card styling as OperationHoursCard. Header row: Material `Groups` icon (20dp) + "Crowding Information" text (16sp medium). Body (24dp below header): two sub-sections with 12dp gap between them. Sub-section 1: "Peak Times" label (`#4A5565`, 16sp) + "Very busy 08:00-09:30, 17:00-18:30" (14sp, `#0A0A0A`) with 4dp gap between label and value. Sub-section 2: "Off-Peak" label (`#4A5565`, 16sp) + "Moderate throughout the day" (14sp, `#0A0A0A`) with 4dp gap. Parameter: `crowding: Crowding`. The off-peak description can be derived from `crowding.level` if available (map QUIET→"Quiet throughout the day", MODERATE→"Moderate throughout the day", BUSY→"Busy throughout the day", VERY_BUSY→"Very busy throughout the day"). Card only shown when crowding is non-null. Reference: Figma node 2:538.

- [x] T008 [US2] Rewrite `LineDetailsHeader` composable in `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/LineDetailsHeader.kt` — remove the entire current implementation (gradient background, station image, dark scrim, bottom-aligned text, overlapping icon, scroll-based fade). Replace with a centred hero section: `Column(horizontalAlignment = CenterHorizontally)` with white background. (1) 80dp circular `Box` with `brandColor.copy(alpha = 0.15f)` background, centred 52dp roundel icon inside — apply `sharedElement()` modifier with key `line_icon_{lineId}`. (2) Line name in 30sp `FontWeight.Bold` `#0A0A0A`, centred — apply `sharedElement()` with key `line_name_{lineId}`. (3) Status `Row`: colour-coded icon (same as `StatusIndicator` mapping from T002, ~24dp) + status text (18sp, colour-coded) — apply `sharedElement()` with key `line_status_{lineId}`. (4) "Last updated: DD MMM YYYY, HH:mm" timestamp (14sp, `#4A5565`, centred). Spacing: 24dp top padding, 16dp between icon and name, 12dp between name and status, 8dp between status and timestamp, 24dp bottom padding. Parameters: `lineDetails: UndergroundLineDetails`, `sharedTransitionScope: SharedTransitionScope`, `animatedVisibilityScope: AnimatedVisibilityScope`. Reference: Figma node 2:488, contracts/ui-contract.md Hero Section.

- [x] T009 [US2] Rewrite `LineDetailsScreen` content in `app/src/main/java/com/smartcommute/feature/linedetails/ui/LineDetailsScreen.kt` — (1) Replace the collapsing `TopAppBar` with a simple fixed `TopAppBar`: white background, left-aligned back arrow icon + "Back" text (14sp medium, `#0A0A0A`), bottom border `0.686dp` in `Color.Black.copy(alpha = 0.1f)`. Back button calls `onNavigateBack`. (2) Replace the `LazyColumn` content: remove all references to StatusSummaryCard, LineInfoCard, DisruptionCard, ClosureCard, CrowdingCard, EmptyStateCard, and the staggered animation logic (delayed visibility, 150ms stagger, 600ms initial delay). New content in a `LazyColumn` or `Column(Modifier.verticalScroll(...))` with `#F9FAFB` background: Item 1: `LineDetailsHeader` hero section (white background area). Item 2: `OperationHoursCard()` — always shown, 16dp horizontal padding, 24dp top margin from hero. Item 3: `NightTubeCard(lineId = lineDetails.id)` — conditionally shown based on line ID, 16dp horizontal padding, 24dp top margin. Item 4: `CrowdingInformationCard(crowding = lineDetails.crowding!!)` — only shown when `lineDetails.crowding != null`, 16dp horizontal padding, 24dp top margin. Keep existing Loading and Error states unchanged. Remove scroll-based header fade-out logic (no longer needed — no collapsing header).

- [x] T010 [US2] Delete unused component files from `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/`: delete `StatusSummaryCard.kt` (replaced by hero section in T008), delete `LineInfoCard.kt` (split into OperationHoursCard T005 + NightTubeCard T006), delete `DisruptionCard.kt` (not in new Figma design), delete `ClosureCard.kt` (not in new Figma design), delete `CrowdingCard.kt` (replaced by CrowdingInformationCard T007), delete `EmptyStateCard.kt` (no empty states in new design). Verify no remaining imports reference these deleted files in `LineDetailsScreen.kt` or anywhere else in the project. Run a build to confirm clean compilation.

**Checkpoint**: At this point, both the tube status list and line details screens should display with the new Figma designs. Navigating between them should work (back button, tap line item). Cards appear conditionally based on data availability.

---

## Phase 4: User Story 3 — Smooth Shared Element Transitions (Priority: P3)

**Goal**: Ensure the icon, name, and status text animate smoothly between their list positions (left-aligned in list item) and their details positions (centred in hero section), in both forward and back navigation.

**Independent Test**: Tap a line item — watch the roundel icon animate from the list to the centred hero position, the name animate from the list to the centred bold heading, and the status animate from the list to the centred status row. Press back — all three should animate back smoothly. Rapid navigation (tap→back→tap) should not glitch.

### Implementation for User Story 3

- [x] T011 [US3] Verify and tune shared element transitions between `LineStatusItem` and `LineDetailsHeader` (hero) — navigate forward and back for multiple lines to confirm: (1) Icon animates from 64dp list position to 80dp hero position smoothly (size + position change). (2) Line name animates from left-aligned 18sp to centred 30sp bold (size + position + weight change). (3) Status text animates from left-aligned 14sp to centred 18sp (size + position change). If the size difference between list elements and hero elements causes visual artefacts (text clipping, icon distortion), add `boundsTransform` or `skipToLookaheadSize()` modifiers to the shared elements in both `LineStatusItem.kt` and `LineDetailsHeader.kt`. If the text weight change (Medium → Bold for name) causes a jarring snap, consider keeping `FontWeight.Medium` on the shared element and wrapping it differently, or adjusting the `boundsTransform` to interpolate smoothly. Animation spec should remain at 500ms tween with `FastOutSlowInEasing` (existing in `AppNavigation.kt`). Files: `app/src/main/java/com/smartcommute/feature/linestatus/ui/components/LineStatusItem.kt`, `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/LineDetailsHeader.kt`.

- [x] T012 [US3] Handle rapid navigation edge case — test rapid tap→back→tap sequences to ensure shared element transitions handle interruption gracefully. If visual glitches occur (stuck elements, flicker, overlapping), add `animateEnterExit()` or adjust the `AnimatedVisibilityScope` usage in `app/src/main/java/com/smartcommute/core/navigation/AppNavigation.kt`. Verify the `SharedTransitionLayout` handles concurrent forward/back navigation without crash or visual corruption.

**Checkpoint**: All three user stories complete. Navigation between screens is smooth, with shared element transitions animating correctly in both directions.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and cleanup

- [x] T013 [P] Verify accessibility — ensure all new/modified composables in `app/src/main/java/com/smartcommute/feature/linestatus/ui/components/` and `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/` have proper `contentDescription` values on icons and interactive elements. Status indicator icons should announce their status type (e.g., "Good Service", "Minor Delays"). Card header icons should have descriptive labels. Back button should have "Navigate back" content description.

- [x] T014 [P] Verify responsive layout — test both redesigned screens on small (5 inch), medium (6.5 inch), and large (10 inch tablet) screen sizes. Verify: list items don't clip text on small screens, hero section scales proportionally, cards maintain proper padding and alignment, no horizontal overflow occurs. Adjust any hardcoded dp values that break on extreme screen sizes.

- [x] T015 Run full manual verification per `specs/004-tube-screens-redesign/quickstart.md` — walk through all verification steps: tube status screen design fidelity, line details screen design fidelity, shared element transitions, conditional card visibility, back navigation, pull-to-refresh, offline mode, error states. Document any issues found and fix before marking complete.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (US1)**: Depends on Phase 1 (T001 colour update). T002 and T003 can start in parallel after T001.
- **Phase 3 (US2)**: Depends on Phase 1 (T001 colour update). T005, T006, T007 can start in parallel immediately after Phase 1. T008 depends on T002 (reuses same status colour/icon mapping). T009 depends on T005, T006, T007, T008. T010 depends on T009.
- **Phase 4 (US3)**: Depends on Phase 2 + Phase 3 (both screens must be redesigned before transition tuning)
- **Phase 5 (Polish)**: Depends on Phase 4 completion

### User Story Dependencies

- **US1 (P1)**: Can start after Phase 1 — no dependencies on other stories
- **US2 (P2)**: Can start after Phase 1 — the three new cards (T005, T006, T007) are independent of US1. T008 (hero section) benefits from T002 (StatusIndicator) being complete for reusing the icon/colour mapping pattern.
- **US3 (P3)**: Depends on both US1 and US2 — transitions require both source (list item) and target (hero section) to be in their final layout

### Parallel Opportunities

- T002 (StatusIndicator) and T005/T006/T007 (new cards) can ALL run in parallel after T001
- T005, T006, T007 (three new cards) can ALL run in parallel with each other
- T013 and T014 (accessibility + responsive) can run in parallel

---

## Parallel Example: User Story 2

```text
# After T001 (colours) is complete, launch these in parallel:
Task T005: "Create OperationHoursCard in .../OperationHoursCard.kt"
Task T006: "Create NightTubeCard in .../NightTubeCard.kt"
Task T007: "Create CrowdingInformationCard in .../CrowdingInformationCard.kt"

# Then sequentially:
Task T008: "Rewrite LineDetailsHeader as hero section"
Task T009: "Rewrite LineDetailsScreen content"
Task T010: "Delete unused component files"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: T001 (update colours)
2. Complete Phase 2: T002 → T003 → T004 (redesigned tube status list)
3. **STOP and VALIDATE**: Launch app, verify list design matches Figma
4. Deploy/demo if ready — the tube status list is the most-used screen

### Incremental Delivery

1. T001 → Foundation ready (new colours)
2. T002–T004 → US1 complete → Test tube status list → Demo (MVP!)
3. T005–T010 → US2 complete → Test line details screen → Demo
4. T011–T012 → US3 complete → Test transitions → Demo
5. T013–T015 → Polish complete → Final verification

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- No automated tests (Constitution Principle III)
- All data layer remains unchanged — UI composable files only
- Shared element transition keys MUST be preserved exactly: `line_icon_{lineId}`, `line_name_{lineId}`, `line_status_{lineId}`
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
