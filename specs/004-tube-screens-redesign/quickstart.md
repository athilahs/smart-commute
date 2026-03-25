# Quickstart: Tube Status & Line Details Screen Redesign

**Feature**: 004-tube-screens-redesign
**Branch**: `004-tube-screens-redesign`

## Prerequisites

- Android Studio (latest stable)
- JDK 17
- Android SDK 36
- Branch `004-tube-screens-redesign` checked out

## Build & Run

```bash
git checkout 004-tube-screens-redesign
./gradlew assembleDebug
```

Run on emulator or device (min SDK 26).

## What to Verify

### Tube Status Screen

1. Open the app — the tube status list is the first screen
2. Verify each line item shows:
   - Circular icon container with lightly tinted background (line brand colour at 15% opacity)
   - Line name as heading text
   - Colour-coded status text (green/amber/red depending on status)
   - Trailing status icon (checkmark circle / warning triangle / error circle)
3. Verify the refresh button has NOT changed from the previous design
4. Verify pull-to-refresh still works
5. Verify offline banner still appears when disconnected

### Line Details Screen

1. Tap any line from the status list
2. Verify hero section:
   - Large centred roundel icon in tinted circular background
   - Bold centred line name
   - Colour-coded status with icon, centred
   - "Last updated" timestamp centred below
3. Verify cards:
   - Operation Hours card with clock icon, Mon-Fri / Sat / Sun rows
   - Night Tube card (only for Central, Jubilee, Northern, Piccadilly, Victoria)
   - Crowding Information card (only if crowding data available)
4. Verify cards are hidden when their data is unavailable
5. Verify back button returns to tube status list
6. Verify light grey background (#F9FAFB) behind cards

### Shared Element Transitions

1. Tap a line item and watch the icon, name, and status animate to the details screen
2. Press back and watch them animate back to the list
3. Rapid navigation (tap → back → tap) should not cause glitches

## Key Files Modified

| File | Change |
|---|---|
| `feature/linestatus/ui/components/LineStatusItem.kt` | Updated list item layout |
| `feature/linestatus/ui/components/StatusIndicator.kt` | New outlined icon styles |
| `feature/linestatus/ui/LineStatusScreen.kt` | Minor header layout adjustments |
| `feature/linedetails/ui/LineDetailsScreen.kt` | Replaced screen layout with hero + cards |
| `feature/linedetails/ui/components/LineDetailsHeader.kt` | Replaced with centred hero section |
| `feature/linedetails/ui/components/OperationHoursCard.kt` | New card component |
| `feature/linedetails/ui/components/NightTubeCard.kt` | New card component |
| `feature/linedetails/ui/components/CrowdingInformationCard.kt` | Redesigned crowding card |
| `core/ui/theme/Color.kt` | Updated status colors |
