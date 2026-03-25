# Research: Tube Status & Line Details Screen Redesign

**Feature**: 004-tube-screens-redesign
**Date**: 2026-03-25

## 1. Status Color Mapping — Figma vs Current Implementation

### Decision: Update status colors to match Figma design

The Figma designs use different status colors than the current implementation. The redesign will adopt the Figma colors.

| Status Type | Current Color | Figma Color | Change Required |
|---|---|---|---|
| Good Service | `#4CAF50` | `#00A63E` | Yes |
| Minor Delays | `#FFC107` | `#FE9A00` | Yes |
| Major Delays | `#FF9800` | `#F54900` | Yes |
| Severe Delays | `#FF5722` | Keep or derive | Minor |
| Closure | `#F44336` | Keep or derive | Minor |
| Service Disruption | `#9E9E9E` | Keep or derive | Minor |

**Rationale**: The Figma designs are the source of truth for the visual redesign. Updating colors ensures pixel-fidelity with the approved designs.

**Alternatives considered**: Keeping current colors — rejected because the redesign explicitly provides new color values in the Figma.

### Decision: Update status indicator icons to match Figma design

The Figma design shows three distinct trailing icon styles:

| Status | Current Icon | Figma Icon |
|---|---|---|
| Good Service | Filled circle with checkmark (green bg, white icon) | Outlined circle with checkmark (green stroke) |
| Minor Delays | Filled circle with warning (amber bg, white icon) | Outlined warning triangle (amber stroke) |
| Major Delays+ | Filled circle with warning (orange/red bg, white icon) | Outlined error circle with exclamation (red stroke) |

**Rationale**: The Figma uses outlined/stroke icons rather than filled backgrounds, creating a lighter visual weight. This matches the overall cleaner design direction.

## 2. Operation Hours Data Source

### Decision: Keep operation hours as hardcoded UI constants

**Finding**: Operation hours are NOT available from the TfL API and NOT stored in the database. They are currently hardcoded in `LineInfoCard.kt`:

- Monday–Friday: 05:30 – 00:15 (Figma) / 05:30 – 00:30 (current)
- Saturday: 05:45 – 00:15 (Figma) / same as weekday (current)
- Sunday: 06:30 – 23:45 (Figma) / 06:30 – 23:30 (current)

The Figma shows slightly different times. These will be updated to match the Figma reference.

**Rationale**: TfL API does not expose structured operating hours data. London Underground lines operate on broadly similar fixed schedules, making hardcoded values appropriate. Adding a database table for static data that never changes from the API would add unnecessary complexity (Constitution Principle VI: Simplicity and YAGNI).

**Alternatives considered**:
1. Add operating hours to Room database — rejected (no API source, data is static)
2. Fetch from TfL timetable API — rejected (out of scope, massive API surface for marginal benefit)
3. Per-line operating hours — rejected (all lines share broadly the same schedule, per-line differences are minimal)

## 3. Night Tube Data Source

### Decision: Keep night tube info as hardcoded UI constants

**Finding**: Night tube availability is currently determined by line ID:
- Lines with Night Tube: Central, Jubilee, Northern, Piccadilly, Victoria
- Night Tube runs 24-hour service on Friday and Saturday nights

The Figma shows: "24-hour service on Friday and Saturday nights." with "Frequency: Every 10 mins".

The frequency ("Every 10 mins") is new information not in the current implementation. This will be hardcoded per line.

**Rationale**: Same as operation hours — static data with no API source.

## 4. Crowding Information — Figma vs Current Data Model

### Decision: Adapt crowding card to display available data; use hardcoded peak/off-peak info

**Finding**: The current `Crowding` model has:
```
level: CrowdingLevel (QUIET, MODERATE, BUSY, VERY_BUSY)
measurementTime: Long
dataSource: String
notes: String?
```

The Figma design shows structured peak/off-peak information:
- Peak Times: "Very busy 08:00-09:30, 17:00-18:30"
- Off-Peak: "Moderate throughout the day"

This structured data is NOT available from the TfL API or current data model.

**Rationale**: The peak/off-peak time ranges shown in the Figma are common knowledge about London Underground commuting patterns and can be hardcoded as general guidance. This avoids adding new API endpoints or database fields for static information.

**Alternatives considered**:
1. Add peak/off-peak fields to Crowding model — rejected (no API source for this structured data)
2. Show only the current crowding level — acceptable fallback, but doesn't match the Figma
3. Hardcode typical peak/off-peak descriptions — chosen, matches Figma design intent

## 5. Sections Removed from Line Details Screen

### Decision: Remove header image, status summary card, disruption cards, and closure cards

**Finding**: The Figma design for the line details screen shows only:
1. Hero section (icon, name, status, last updated)
2. Operation Hours card
3. Night Tube card
4. Crowding Information card

The current implementation additionally shows:
- Header with station image and gradient
- Status Summary Card (status type, description, expected resume time)
- Disruption Card(s) (expandable with affected stations, duration)
- Closure Card(s) (expandable with reason, alternative routes)

These are NOT present in the Figma redesign.

**Rationale**: The Figma design is the authoritative source for the new layout. The hero section now incorporates the essential status information (type + last updated). Detailed disruption/closure information may be better served by a future dedicated screen or notification system.

**Alternatives considered**:
1. Keep disruption/closure cards below the new cards — rejected (not in Figma, would break the clean design)
2. Add disruption info to the status section in hero — rejected (would clutter the focused hero design)

## 6. Shared Element Transition Adaptation

### Decision: Reuse existing shared element keys, update target positions

**Finding**: Current shared element keys:
- `line_icon_{lineId}` — icon
- `line_name_{lineId}` — line name text
- `line_status_{lineId}` — status text

Current transition: icon moves to bottom-right of header image, name/status move to bottom-left of header image.

New transition: icon moves to top-center (large hero circle), name moves to center below icon, status moves to center below name.

The `SharedTransitionScope` and `sharedElement()` modifier infrastructure remains the same. Only the target composable positions change.

**Rationale**: The existing shared element API handles position changes automatically — the modifier calculates start/end positions based on where the composables are laid out. Changing the layout of the details screen automatically changes the transition endpoints.

## 7. Line Status Item Layout Change

### Decision: Update layout to match Figma proportions

**Finding**: Current `LineStatusItem` uses:
- 52.dp icon container
- `titleMedium` for line name
- `bodyMedium` for status text
- 40.dp status indicator (filled circle)

Figma design uses:
- ~64dp icon container with 15% opacity tinted background
- 18sp medium weight for line name
- 14sp medium weight for status text
- 28dp trailing status icon (outlined style)

**Rationale**: Matching Figma proportions ensures visual fidelity with the approved design.

## 8. Back Button on Line Details Screen

### Decision: Add explicit back button to top bar

**Finding**: The Figma shows a left arrow + "Back" text in a top bar. The current implementation uses a transparent `TopAppBar` that becomes opaque on scroll with a back arrow icon only.

The new design uses a simple white top bar with a back button, no collapsing behavior.

**Rationale**: The new design simplifies the top bar — no scroll-based animations needed since the header image is removed. A simple fixed top bar with back navigation matches the Figma.
