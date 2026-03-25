# UI Contract: Tube Status & Line Details Screen Redesign

**Feature**: 004-tube-screens-redesign
**Date**: 2026-03-25

## Overview

This redesign is purely UI-layer. No new API endpoints, repository methods, or database queries are introduced. This contract documents the interface between the existing data layer and the new UI components.

## Tube Status Screen — LineStatusItem Contract

### Input Data (unchanged)

```
LineStatusItem(
    line: UndergroundLine,         // Existing model — id, name, status
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit
)
```

### Visual Output Mapping

| Data Field | UI Element | Style |
|---|---|---|
| `line.id` | Circular icon container background | Line brand colour at 15% opacity |
| `line.id` | TfL roundel icon inside container | Existing drawable resource |
| `line.name` | Primary heading text | 18sp, medium weight, #0A0A0A |
| `line.status.type` | Status subtitle text | 14sp, medium weight, colour-coded |
| `line.status.type` | Trailing status icon | 28dp outlined icon, colour-coded |

### Status → Colour + Icon Mapping

| StatusType | Text Colour | Icon | Icon Colour |
|---|---|---|---|
| GOOD_SERVICE | #00A63E | Outlined CheckCircle | #00A63E |
| MINOR_DELAYS | #FE9A00 | Outlined Warning | #FE9A00 |
| MAJOR_DELAYS | #F54900 | Outlined Error | #F54900 |
| SEVERE_DELAYS | #E53E3E | Outlined Error | #E53E3E |
| CLOSURE | #DC2626 | Outlined Error | #DC2626 |
| SERVICE_DISRUPTION | #6B7280 | Outlined Warning | #6B7280 |

## Line Details Screen — Component Contracts

### Hero Section

```
LineDetailsHero(
    lineDetails: UndergroundLineDetails,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
)
```

| Data Field | UI Element | Style |
|---|---|---|
| `lineDetails.id` | Large circular tinted background | Brand colour at 15% opacity, ~80dp |
| `lineDetails.id` | TfL roundel icon centred | ~52dp |
| `lineDetails.name` | Bold centred heading | 30sp, bold, #0A0A0A |
| `lineDetails.status.type` | Status icon + text centred | 18sp, colour-coded |
| `lineDetails.lastUpdated` | Timestamp centred below status | 14sp, #4A5565 |

### Operation Hours Card

```
OperationHoursCard()  // No data parameters — all hardcoded
```

Displayed always. Uses hardcoded schedule data.

### Night Tube Card

```
NightTubeCard(lineId: String)  // Determines visibility and frequency
```

Displayed only when `lineId` is one of: central, jubilee, northern, piccadilly, victoria.

### Crowding Information Card

```
CrowdingInformationCard(crowding: Crowding)
```

Displayed only when `crowding != null`. Peak/off-peak descriptions are hardcoded; `crowding.level` supplements the off-peak description.

## Shared Element Transition Keys (unchanged)

| Key Pattern | Element | Source (List) | Target (Details) |
|---|---|---|---|
| `line_icon_{lineId}` | Roundel icon | Left side of list item | Top centre of hero |
| `line_name_{lineId}` | Line name text | Middle of list item | Centre below icon |
| `line_status_{lineId}` | Status text | Below line name | Centre below name |
