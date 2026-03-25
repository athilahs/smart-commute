# Data Model: Tube Status & Line Details Screen Redesign

**Feature**: 004-tube-screens-redesign
**Date**: 2026-03-25

## Overview

This is a UI-only redesign. **No changes to the data layer (models, entities, repositories, API) are required.** All data needed for the new designs is already available through existing models and hardcoded UI constants.

## Existing Models Used (No Changes)

### UndergroundLine (Tube Status Screen)

```
UndergroundLine
├── id: String                    → Used to resolve brand colour and roundel icon
├── name: String                  → Displayed as heading text in list item
├── modeName: String              → (unused in redesign)
└── status: ServiceStatus         → Drives status text colour and trailing icon
```

### ServiceStatus (Both Screens)

```
ServiceStatus
├── type: StatusType              → Determines colour + icon style
│   ├── GOOD_SERVICE              → Green (#00A63E) + checkmark circle
│   ├── MINOR_DELAYS              → Amber (#FE9A00) + warning triangle
│   ├── MAJOR_DELAYS              → Red/orange (#F54900) + error circle
│   ├── SEVERE_DELAYS             → Red + error circle
│   ├── CLOSURE                   → Red + error circle
│   └── SERVICE_DISRUPTION        → Grey + warning
├── description: String           → (removed from line details — no status summary card)
├── severity: Int                 → (unused in redesign)
└── validUntil: Long?             → (removed from line details — no expected resume time)
```

### UndergroundLineDetails (Line Details Screen)

```
UndergroundLineDetails
├── id: String                    → Used for brand colour, roundel icon, hardcoded data lookup
├── name: String                  → Displayed centred in hero section
├── modeName: String              → (unused in redesign)
├── status: ServiceStatus         → Displayed centred with icon in hero section
├── brandColor: String            → Used for hero icon tinted background
├── lastUpdated: Long             → Formatted as "Last updated: DD MMM YYYY, HH:mm"
├── disruptions: List<Disruption> → (NOT displayed in redesign)
├── closures: List<Closure>       → (NOT displayed in redesign)
└── crowding: Crowding?           → Used for Crowding Information card (if non-null)
```

### Crowding (Line Details Screen — Crowding Card)

```
Crowding
├── level: CrowdingLevel          → Used to derive peak/off-peak text descriptions
├── measurementTime: Long         → (not displayed in new design)
├── dataSource: String            → (not displayed in new design)
└── notes: String?                → Can supplement peak/off-peak descriptions
```

## Hardcoded UI Data (No Model Changes)

### Operation Hours (per Figma design)

Applied to ALL lines uniformly:

| Day | Open | Close |
|---|---|---|
| Monday – Friday | 05:30 | 00:15 |
| Saturday | 05:45 | 00:15 |
| Sunday | 06:30 | 23:45 |

**Source**: Hardcoded in UI component. No model or database representation.

### Night Tube Availability

| Line ID | Has Night Tube | Frequency |
|---|---|---|
| central | Yes | Every 10 mins |
| jubilee | Yes | Every 10 mins |
| northern | Yes | Every 10 mins |
| piccadilly | Yes | Every 10 mins |
| victoria | Yes | Every 10 mins |
| (all others) | No | N/A |

**Source**: Hardcoded based on line ID. No model or database representation.

### Peak/Off-Peak Crowding Descriptions

Applied as general London Underground guidance when crowding data is available:

| Period | Description |
|---|---|
| Peak Times | "Very busy 08:00-09:30, 17:00-18:30" |
| Off-Peak | "Moderate throughout the day" |

**Source**: Hardcoded in UI component. The `Crowding.level` field can override the off-peak description if it provides real-time data.

## Status Color Mapping (Updated Values)

| StatusType | Old Color | New Color (Figma) |
|---|---|---|
| GOOD_SERVICE | `#4CAF50` | `#00A63E` |
| MINOR_DELAYS | `#FFC107` | `#FE9A00` |
| MAJOR_DELAYS | `#FF9800` | `#F54900` |
| SEVERE_DELAYS | `#FF5722` | `#E53E3E` (derived) |
| CLOSURE | `#F44336` | `#DC2626` (derived) |
| SERVICE_DISRUPTION | `#9E9E9E` | `#6B7280` (derived) |

## Data Flow (Unchanged)

```
Room DB → Repository → ViewModel (StateFlow) → Screen (Composable)
```

No new queries, no new tables, no new API calls. The redesign only changes how existing data is rendered in the UI layer.
