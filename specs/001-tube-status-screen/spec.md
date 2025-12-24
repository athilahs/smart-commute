# Feature Specification: Tube Status Screen

**Feature Branch**: `001-tube-status-screen`
**Created**: 2025-12-24
**Status**: Draft
**Input**: User description: "create the foundations of an Android app and its first feature: a screen that lists all the lines of the London underground metro and their current statuses (good service, minor delays, major delays, etc); each item of the list must show the corresponding line logo as the item icon, the line name as the title and the status as the subtitle; add this screen as a tab of a bottom navigation bar for ease scalability in the future"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Current Line Statuses (Priority: P1)

A commuter opens the app to check which Underground lines are running smoothly and which have delays before planning their journey. The app displays a scrollable list showing all London Underground lines with their current service status, making it immediately clear which lines to avoid or plan around.

**Why this priority**: This is the core value proposition of the app - providing at-a-glance status information for all tube lines. Without this, the app has no purpose. This delivers immediate, actionable information to users planning their commute.

**Independent Test**: Can be fully tested by launching the app and viewing the status screen. Success is demonstrated when all tube lines appear with their logos, names, and current status information in an easily scannable format.

**Acceptance Scenarios**:

1. **Given** the user opens the app for the first time, **When** the app loads, **Then** a list of all London Underground lines is displayed showing each line's logo, name, and current status
2. **Given** the status screen is displayed, **When** a line has good service, **Then** the status subtitle shows "Good Service" with appropriate visual indicator
3. **Given** the status screen is displayed, **When** a line has minor delays, **Then** the status subtitle shows "Minor Delays" with appropriate visual indicator
4. **Given** the status screen is displayed, **When** a line has major delays, **Then** the status subtitle shows "Major Delays" with appropriate visual indicator
5. **Given** the status screen is displayed, **When** a line has severe delays or closure, **Then** the status subtitle shows the appropriate status with appropriate visual indicator
6. **Given** the user is viewing the status list, **When** there are more lines than fit on screen, **Then** the user can scroll to view all lines

---

### User Story 2 - Navigate Using Bottom Navigation (Priority: P2)

A user wants to access the line status screen from anywhere in the app and expects the app structure to support additional features in the future. The app provides a bottom navigation bar with the status screen as one tab, allowing easy access and setting the foundation for future feature additions.

**Why this priority**: This establishes the app's navigation foundation and scalability. While the status screen is the primary feature now, the bottom navigation structure enables future features (like journey planning, saved routes, etc.) without requiring architectural changes.

**Independent Test**: Can be tested by opening the app and verifying the bottom navigation bar is present with the status screen as an accessible tab. Success is demonstrated when users can tap the status tab icon to view/return to the status screen.

**Acceptance Scenarios**:

1. **Given** the user opens the app, **When** the app loads, **Then** a bottom navigation bar is visible with at least one tab for the status screen
2. **Given** the user is on the status screen, **When** they tap the status tab in the bottom navigation, **Then** they remain on the status screen (tab is already selected)
3. **Given** the bottom navigation is visible, **When** the status tab is the active screen, **Then** the status tab appears visually selected/highlighted
4. **Given** the user will navigate to future features, **When** they tap the status tab, **Then** they return to the status screen

---

### Edge Cases

- When the device has no internet connection and status data cannot be retrieved, the system displays the last successfully retrieved status data with a "Last updated: [timestamp]" indicator and a "No connection" warning banner, and automatically retries fetching data in the background
- When the TfL API service is unavailable or returns an error: if cached data exists, display it with a "Service temporarily unavailable" banner and manual retry button; if no cached data exists (first launch), show an error message with retry button
- When status information is loading: on initial app load (no cached data), display a full-screen loading spinner overlay; for subsequent refreshes (cached data exists), show existing data immediately with a small loading indicator at the top of the screen
- When a line has a status not in the standard categories (good service, minor delays, major delays, severe delays, closure), the system maps the unknown status to "Service Disruption" and displays it with a warning icon
- What happens when the device screen is very small or very large?
- What happens when the user rotates the device while viewing the status screen?
- What happens when the system's accessibility features (large text, TalkBack) are enabled?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display all London Underground lines in a scrollable list
- **FR-002**: System MUST display the official line logo/icon for each Underground line
- **FR-003**: System MUST display the line name as the primary text for each list item
- **FR-004**: System MUST display the current service status as secondary text for each list item
- **FR-005**: System MUST support at minimum these status types: Good Service, Minor Delays, Major Delays, Severe Delays, Closure, and Service Disruption (for unknown statuses)
- **FR-006**: System MUST retrieve current line status information from the Transport for London (TfL) Official API
- **FR-007**: System MUST map any unrecognized status from TfL API to "Service Disruption" with a warning icon
- **FR-008**: System MUST include a bottom navigation bar component
- **FR-009**: System MUST display the status screen as a tab in the bottom navigation bar
- **FR-010**: System MUST allow users to scroll through the entire list of lines
- **FR-011**: System MUST visually differentiate between different status types (good service vs delays)
- **FR-012**: System MUST maintain the status screen's state when navigating away and returning via bottom navigation
- **FR-013**: System MUST display a full-screen loading spinner overlay during initial app load when no cached data exists
- **FR-014**: System MUST display existing cached data immediately during subsequent refreshes with a small loading indicator at the top
- **FR-015**: System MUST handle error states when line status information cannot be retrieved
- **FR-016**: System MUST display status information in a way that is readable on various screen sizes
- **FR-017**: System MUST cache the most recent successful status data locally for offline viewing
- **FR-018**: System MUST display a "Last updated: [timestamp]" indicator when showing cached data
- **FR-019**: System MUST display a "No connection" warning banner when offline and showing cached data
- **FR-020**: System MUST automatically retry fetching status data in the background when connection is restored
- **FR-021**: System MUST display a "Service temporarily unavailable" banner when TfL API is unavailable but cached data exists
- **FR-022**: System MUST provide a manual retry button when API errors occur
- **FR-023**: System MUST show an error message with retry button when API is unavailable and no cached data exists (first launch scenario)
- **FR-024**: System MUST support pull-to-refresh gesture to manually refresh status data
- **FR-025**: System MUST provide a refresh button to manually update status data

### Key Entities

- **Underground Line**: Represents a London Underground line with attributes including line name, line identifier, official line logo/colors, and current service status
- **Service Status**: Represents the current operational status of a line, including status type (good service, minor delays, major delays, severe delays, closure) and optional status description or reason
- **Navigation Tab**: Represents a tab in the bottom navigation bar, including tab icon, tab label, and associated screen destination

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can view the complete list of all London Underground lines within 3 seconds of opening the app
- **SC-002**: Users can identify a line's status (good service vs delays) within 1 second of viewing the list
- **SC-003**: The status screen displays correctly on screens ranging from 5 inches to 10+ inches without information being cut off or requiring horizontal scrolling
- **SC-004**: 95% of users can successfully locate and return to the status screen using the bottom navigation on first attempt
- **SC-005**: The app displays accurate line logos matching Transport for London's official branding for all lines
- **SC-006**: Status information remains readable and accessible when device accessibility features are enabled

## Clarifications

### Session 2025-12-24

- Q: No internet connection behavior → A: Store the last successful status data locally and display it with a clear "Last updated" timestamp and "No connection" indicator, then retry in the background
- Q: TfL API error or unavailability behavior → A: If cached data exists, show it with "Service temporarily unavailable" banner and retry button; if no cached data exists, show error message with retry button
- Q: Loading state behavior → A: Show a loading spinner overlay on the screen for initial load; for subsequent refreshes, show data immediately with a small loading indicator at the top
- Q: Unknown or non-standard status handling → A: Map any unknown status to "Service Disruption" with a warning icon
- Q: Status data refresh interval → A: Refresh only when user manually pulls to refresh or taps a refresh button

## Assumptions

- Line logos will be sourced from Transport for London's official brand guidelines or public asset repositories
- The app will follow standard London Underground status categories (Good Service, Minor Delays, Major Delays, Severe Delays, Closure)
- Internet connectivity is required for real-time status updates via TfL API
- The app targets modern devices running recent versions of the operating system
- Status information updates only when the user manually refreshes (pull-to-refresh or refresh button) - no automatic polling
- The bottom navigation bar will initially contain only the status tab, with placeholders or disabled tabs for future features
- Line order in the list follows Transport for London's standard line ordering (alphabetical or by line type)
- TfL API access will require registration for an API key (free tier assumed sufficient for MVP)
- TfL API rate limits are sufficient for app usage patterns (manual refresh only, no automated polling)

## Out of Scope

- Automatic polling or background refresh (refresh is manual-only for MVP)
- Detailed disruption information (reasons for delays, affected stations) - this feature displays status only
- Filtering or searching lines (with ~11 lines, full list is manageable)
- Push notifications for status changes
- Journey planning or route calculation
- Saved favorite lines or personalized views
- Historical status data or trends
- Integration with other transport modes (buses, Overground, Elizabeth Line) - focused on Underground only for MVP
- User accounts or authentication
