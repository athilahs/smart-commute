# Manual Verification Guide - SmartCommute

**Feature**: Tube Status Screen (001-tube-status-screen)
**Date**: 2025-12-27
**Purpose**: Manual testing checklist for all requirements and success criteria

---

## Prerequisites

Before starting verification, ensure:
- [ ] TfL API key is configured in `local.properties` (`TFL_API_KEY=your_key_here`)
- [ ] App is built successfully (`./gradlew assembleDebug`)
- [ ] Test device or emulator is available

---

## Phase 1: Performance Goals (T047)

**Target**: App loads and displays cached data within 3 seconds; fresh data fetch within 3 seconds; 60 fps UI rendering; <200ms interaction response

### Test Steps

1. **First Launch Performance**
   - [ ] Clear app data (Settings > Apps > SmartCommute > Clear Data)
   - [ ] Launch app with good internet connection
   - [ ] Use stopwatch to measure time until all tube lines are displayed
   - [ ] **Expected**: All lines visible within 3 seconds
   - [ ] **Result**: _____ seconds

2. **Cached Data Load Performance**
   - [ ] Close app (don't clear data)
   - [ ] Enable airplane mode
   - [ ] Launch app
   - [ ] Use stopwatch to measure time until cached data is displayed
   - [ ] **Expected**: Cached data visible within 1 second
   - [ ] **Result**: _____ seconds

3. **Refresh Performance**
   - [ ] Disable airplane mode
   - [ ] Pull down to refresh
   - [ ] Measure time until updated data is displayed
   - [ ] **Expected**: Fresh data within 3 seconds
   - [ ] **Result**: _____ seconds

4. **UI Rendering Performance**
   - [ ] Enable "Profile GPU Rendering" in Developer Options > "On screen as bars"
   - [ ] Scroll through the list of tube lines
   - [ ] Observe the green bars (should stay below the horizontal line at 16ms for 60fps)
   - [ ] **Expected**: Smooth 60fps scrolling, no jank
   - [ ] **Result**: Pass / Fail

5. **Interaction Response Time**
   - [ ] Tap the refresh button in the top bar
   - [ ] Observe how quickly the refresh indicator appears
   - [ ] **Expected**: Immediate visual feedback (<200ms)
   - [ ] **Result**: Pass / Fail

---

## Phase 2: Screen Size Compatibility (T048)

**Target**: App displays correctly on screens ranging from 5 inches to 10+ inches

### Test Steps

1. **Small Screen (5 inches, ~360x640 dp)**
   - [ ] Test on device/emulator: Pixel 2 or similar (5.0")
   - [ ] Launch app
   - [ ] Verify all 11 tube lines are visible in scrollable list
   - [ ] Verify no horizontal scrolling is required
   - [ ] Verify line names and status text are fully readable
   - [ ] Verify status indicators are visible and properly sized
   - [ ] Verify bottom navigation bar is visible and accessible
   - [ ] **Result**: Pass / Fail

2. **Medium Screen (6 inches, ~411x823 dp)**
   - [ ] Test on device/emulator: Pixel 5 or similar (6.0")
   - [ ] Launch app
   - [ ] Verify layout is well-balanced (not too cramped or too spacious)
   - [ ] Verify all UI elements scale appropriately
   - [ ] **Result**: Pass / Fail

3. **Large Screen (10+ inches, tablet)**
   - [ ] Test on device/emulator: Pixel Tablet or similar (10.95")
   - [ ] Launch app in portrait mode
   - [ ] Verify list items don't stretch awkwardly
   - [ ] Verify text remains readable (not too large)
   - [ ] Rotate to landscape mode
   - [ ] Verify layout adapts properly in landscape
   - [ ] **Result**: Pass / Fail

4. **Font Scaling**
   - [ ] Go to Settings > Display > Font size
   - [ ] Set to "Largest"
   - [ ] Return to app
   - [ ] Verify all text is visible and doesn't get cut off
   - [ ] Verify line status items don't overlap
   - [ ] **Result**: Pass / Fail

---

## Phase 3: User Story 1 - Manual Verification Scenarios (T049)

**Reference**: specs/001-tube-status-screen/spec.md - User Story 1 Acceptance Scenarios

### Scenario 1: First Launch
- [ ] Clear app data
- [ ] Ensure device has internet connection
- [ ] Launch app
- [ ] **Expected**: Loading spinner appears first
- [ ] **Expected**: List of all 11 London Underground lines displays
- [ ] **Expected**: Each line shows: line color indicator, line name, current status
- [ ] **Result**: Pass / Fail

### Scenario 2: Good Service Status
- [ ] Identify a line with "Good Service" status
- [ ] Verify the status indicator shows a green checkmark icon
- [ ] Verify status subtitle reads "Good Service"
- [ ] **Result**: Pass / Fail

### Scenario 3: Minor Delays Status
- [ ] If available, identify a line with "Minor Delays" status
- [ ] Verify the status indicator shows a warning icon with yellow/amber background
- [ ] Verify status subtitle reads "Minor Delays"
- [ ] Verify delay reason is displayed if provided by TfL
- [ ] **Result**: Pass / Fail (or N/A if no delays at test time)

### Scenario 4: Major Delays Status
- [ ] If available, identify a line with "Major Delays" status
- [ ] Verify the status indicator shows a warning icon with orange background
- [ ] Verify status subtitle reads "Major Delays"
- [ ] **Result**: Pass / Fail (or N/A if no delays at test time)

### Scenario 5: Severe Status
- [ ] If available, identify a line with severe delays or closure
- [ ] Verify the status indicator shows appropriate icon and red background
- [ ] Verify status subtitle matches the severity
- [ ] **Result**: Pass / Fail (or N/A if no severe issues at test time)

### Scenario 6: Scrollability
- [ ] Scroll to the bottom of the list
- [ ] Verify all 11 lines are accessible
- [ ] Verify scrolling is smooth (see Phase 1 for fps check)
- [ ] **Result**: Pass / Fail

### Scenario 7: Offline Mode (No Connection)
- [ ] Launch app with internet connection (to cache data)
- [ ] Wait for data to load
- [ ] Enable airplane mode
- [ ] Pull to refresh
- [ ] **Expected**: Banner displays "No connection"
- [ ] **Expected**: "Last updated" timestamp is visible
- [ ] **Expected**: Cached data remains visible
- [ ] **Expected**: No crash or empty screen
- [ ] **Result**: Pass / Fail

### Scenario 8: API Error Handling
- [ ] Edit `local.properties` to use an invalid API key
- [ ] Clear app data
- [ ] Launch app
- [ ] **Expected**: Error message displays with retry button
- [ ] **Expected**: Message indicates configuration error
- [ ] Tap retry button
- [ ] **Expected**: Retry attempt is made
- [ ] Restore valid API key
- [ ] **Result**: Pass / Fail

### Scenario 9: Pull-to-Refresh
- [ ] Ensure app is showing current data
- [ ] Pull down on the list
- [ ] **Expected**: Refresh indicator appears at top
- [ ] **Expected**: Data updates (check timestamp)
- [ ] **Result**: Pass / Fail

### Scenario 10: Manual Refresh Button
- [ ] Tap the refresh icon in the top app bar
- [ ] **Expected**: Refresh indicator appears
- [ ] **Expected**: Data updates (check timestamp)
- [ ] **Result**: Pass / Fail

---

## Phase 4: User Story 2 - Manual Verification (T049 continued)

**Reference**: specs/001-tube-status-screen/spec.md - User Story 2 Acceptance Scenarios

### Scenario 1: Bottom Navigation Visibility
- [ ] Launch app
- [ ] **Expected**: Bottom navigation bar is visible
- [ ] **Expected**: "Status" tab is present
- [ ] **Expected**: Status tab appears selected/highlighted
- [ ] **Result**: Pass / Fail

### Scenario 2: Tab Selection State
- [ ] Observe the Status tab in bottom navigation
- [ ] **Expected**: Status tab shows selected state (highlighted icon/text)
- [ ] Tap the Status tab while already on status screen
- [ ] **Expected**: Remains on status screen (no navigation)
- [ ] **Expected**: Tab remains selected
- [ ] **Result**: Pass / Fail

### Scenario 3: State Persistence
- [ ] Scroll down the list to the bottom
- [ ] Note the scroll position
- [ ] Tap the Status tab again
- [ ] **Expected**: Scroll position is maintained (still at bottom)
- [ ] **Expected**: No screen reload or reset
- [ ] **Result**: Pass / Fail

---

## Phase 5: Accessibility with TalkBack (T050)

**Target**: App is fully accessible with TalkBack screen reader enabled

### Test Steps

1. **Enable TalkBack**
   - [ ] Go to Settings > Accessibility > TalkBack
   - [ ] Enable TalkBack
   - [ ] Return to SmartCommute app

2. **Navigate Screen with TalkBack**
   - [ ] Swipe right to navigate through UI elements
   - [ ] **Expected**: Each line item announces: "[Line name] line: [Status]"
   - [ ] Example: "Central line: Minor Delays"
   - [ ] **Expected**: Refresh button announces "Refresh status"
   - [ ] **Expected**: Bottom navigation announces "Status"
   - [ ] **Result**: Pass / Fail

3. **Line Status Details**
   - [ ] Focus on a line with delays
   - [ ] **Expected**: TalkBack reads full status including delay reason
   - [ ] **Result**: Pass / Fail

4. **Interactive Elements**
   - [ ] Double-tap refresh button with TalkBack
   - [ ] **Expected**: Refresh action is triggered
   - [ ] **Expected**: Loading state is announced
   - [ ] **Result**: Pass / Fail

5. **Banner Announcements**
   - [ ] Enable airplane mode
   - [ ] Pull to refresh
   - [ ] **Expected**: "No connection" banner is announced by TalkBack
   - [ ] **Result**: Pass / Fail

6. **Disable TalkBack**
   - [ ] Disable TalkBack to continue other tests

---

## Phase 6: Dark Mode Theme (T051)

**Target**: App supports both light and dark themes correctly

### Test Steps

1. **Light Mode Verification**
   - [ ] Ensure device is in light mode (Settings > Display > Light mode)
   - [ ] Launch app
   - [ ] Verify UI uses light theme colors
   - [ ] Verify text is readable (dark text on light background)
   - [ ] Verify status indicators are clearly visible
   - [ ] **Result**: Pass / Fail

2. **Dark Mode Verification**
   - [ ] Change device to dark mode (Settings > Display > Dark mode)
   - [ ] Return to app
   - [ ] **Expected**: UI automatically switches to dark theme
   - [ ] Verify UI uses dark theme colors
   - [ ] Verify text is readable (light text on dark background)
   - [ ] Verify status indicators remain clearly visible
   - [ ] Verify no white "flash" or jarring transition
   - [ ] **Result**: Pass / Fail

3. **Theme Consistency**
   - [ ] While in dark mode, scroll through entire list
   - [ ] Verify all list items use dark theme consistently
   - [ ] Verify top app bar uses dark theme
   - [ ] Verify bottom navigation uses dark theme
   - [ ] Verify error banners use appropriate dark theme colors
   - [ ] **Result**: Pass / Fail

4. **Dynamic Theme Switching**
   - [ ] With app in foreground, open device quick settings
   - [ ] Toggle dark mode on/off
   - [ ] Return to app
   - [ ] **Expected**: App theme updates dynamically without restart
   - [ ] **Result**: Pass / Fail

---

## Phase 7: Screen Rotation State Persistence (T052)

**Target**: App maintains state correctly when device is rotated

### Test Steps

1. **Basic Rotation**
   - [ ] Launch app in portrait mode
   - [ ] Wait for data to load
   - [ ] Rotate device to landscape
   - [ ] **Expected**: UI rotates smoothly
   - [ ] **Expected**: Data remains displayed (no reload)
   - [ ] **Expected**: No crash
   - [ ] **Result**: Pass / Fail

2. **Scroll Position Persistence**
   - [ ] In portrait mode, scroll to the bottom of the list
   - [ ] Rotate to landscape
   - [ ] **Expected**: Scroll position is maintained (still at bottom)
   - [ ] Rotate back to portrait
   - [ ] **Expected**: Scroll position still maintained
   - [ ] **Result**: Pass / Fail

3. **Loading State During Rotation**
   - [ ] Clear app data
   - [ ] Launch app
   - [ ] While loading spinner is visible, rotate device
   - [ ] **Expected**: Loading spinner continues showing
   - [ ] **Expected**: No crash
   - [ ] Wait for data to load
   - [ ] **Expected**: Data displays correctly after rotation
   - [ ] **Result**: Pass / Fail

4. **Error State During Rotation**
   - [ ] Create an error state (use invalid API key or airplane mode with no cache)
   - [ ] When error message is displayed, rotate device
   - [ ] **Expected**: Error message persists
   - [ ] **Expected**: Retry button remains functional
   - [ ] **Result**: Pass / Fail

5. **Refresh During Rotation**
   - [ ] Start a pull-to-refresh
   - [ ] While refresh indicator is visible, rotate device
   - [ ] **Expected**: Refresh completes successfully
   - [ ] **Expected**: No crash or frozen state
   - [ ] **Result**: Pass / Fail

---

## Edge Cases Verification

### Edge Case 1: No Internet on First Launch
- [ ] Clear app data
- [ ] Enable airplane mode
- [ ] Launch app
- [ ] **Expected**: Error message with retry button
- [ ] **Expected**: Message indicates no connection
- [ ] Disable airplane mode
- [ ] Tap retry
- [ ] **Expected**: Data loads successfully
- [ ] **Result**: Pass / Fail

### Edge Case 2: Internet Lost During Data Fetch
- [ ] Clear app data
- [ ] Launch app
- [ ] Immediately enable airplane mode (before data loads)
- [ ] **Expected**: Error message after timeout
- [ ] **Expected**: Retry button available
- [ ] **Result**: Pass / Fail

### Edge Case 3: Rapid Refresh Requests
- [ ] Load app with data
- [ ] Rapidly tap refresh button multiple times (5-6 times quickly)
- [ ] **Expected**: No crash
- [ ] **Expected**: No duplicate data
- [ ] **Expected**: Single refresh completes
- [ ] **Result**: Pass / Fail

### Edge Case 4: Background/Foreground Transitions
- [ ] Load app with data visible
- [ ] Press home button (send app to background)
- [ ] Wait 30 seconds
- [ ] Return to app
- [ ] **Expected**: Data still visible (not reset)
- [ ] **Expected**: No loading spinner on return
- [ ] **Result**: Pass / Fail

---

## Test Summary

### Overall Results

**Performance (T047)**
- First Launch Performance: ☐ Pass ☐ Fail
- Cached Load Performance: ☐ Pass ☐ Fail
- Refresh Performance: ☐ Pass ☐ Fail
- UI Rendering (60fps): ☐ Pass ☐ Fail
- Interaction Response: ☐ Pass ☐ Fail

**Screen Compatibility (T048)**
- Small Screen (5"): ☐ Pass ☐ Fail
- Medium Screen (6"): ☐ Pass ☐ Fail
- Large Screen (10"+): ☐ Pass ☐ Fail
- Font Scaling: ☐ Pass ☐ Fail

**User Story 1 (T049)**
- All 10 scenarios: ☐ Pass ☐ Fail

**User Story 2 (T049)**
- All 3 scenarios: ☐ Pass ☐ Fail

**Accessibility (T050)**
- TalkBack Navigation: ☐ Pass ☐ Fail

**Dark Mode (T051)**
- Theme Switching: ☐ Pass ☐ Fail

**Rotation (T052)**
- State Persistence: ☐ Pass ☐ Fail

**Edge Cases**
- All edge cases: ☐ Pass ☐ Fail

---

## Issues Found

Document any issues discovered during testing:

| Issue # | Phase | Description | Severity | Status |
|---------|-------|-------------|----------|--------|
| 1       |       |             |          |        |
| 2       |       |             |          |        |
| 3       |       |             |          |        |

---

## Sign-off

- [ ] All critical tests passed (Performance, User Stories, Accessibility)
- [ ] All blockers resolved
- [ ] App ready for production release

**Tester Name**: _________________
**Date**: _________________
**Signature**: _________________
