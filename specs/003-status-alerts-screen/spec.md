# Feature Specification: Status Alerts Screen

**Feature Branch**: `003-status-alerts-screen`
**Created**: 2025-12-29
**Status**: Draft
**Input**: User description: "I want to add a second tab to the bottom navigation which will allow the user to configure alarms that will notify them about the tube line's status at a given time. This screen should allow the user to select what time they'd like to be notified (time picker), which days of the week (week day selector), and what tube lines (tube line picker). The screen should also display a list view similar to Android's Clock app, where users can see all their configured alarms, and enable/disable them. When a user taps a list item, a bottom sheet will show up, allowing the user to see and configure the details of the selected alarm. When the user opens this screen for the first time, or if they have deleted all the alarms and there are no alarms configured, they will see an empty state. At all times (except when looking at the empty state) the user will see a FAB that will trigger the bottom sheet to create a new alarm. However, if the user has created 10 alarms, the FAB should be hidden. When an alarm triggers and the tube line status is 'Good Service', a silent notification should be triggered. If the status is anything else other than 'Good Service', a notification with audio should be triggered."

## User Scenarios & Testing

### User Story 1 - Grant Notification Permission (Priority: P1)

As a user, I want to be prompted to grant notification permission when I open the Status Alerts screen, so I can receive alarm notifications.

**Why this priority**: Essential prerequisite for the entire feature. Without notification permission, alarms cannot function. Must be handled before any alarm creation.

**Independent Test**: Can be fully tested by opening the Status Alerts screen without notification permission granted, verifying the permission prompt appears, and confirming that alarms can be created and trigger notifications after permission is granted.

**Acceptance Scenarios**:

1. **Given** the user has not granted notification permission, **When** they navigate to the Status Alerts screen, **Then** the Android system permission dialog is displayed
2. **Given** the user grants notification permission, **When** the permission is granted, **Then** the screen displays the normal empty state or alarm list
3. **Given** the user denies notification permission, **When** the permission is denied, **Then** an informative message explains that notifications are required and provides a way to access app settings
4. **Given** the user has previously denied permission, **When** they return to the Status Alerts screen, **Then** the app guides them to manually enable notifications in system settings

---

### User Story 2 - Create Basic Status Alert (Priority: P1)

As a regular commuter, I want to create a status alert for my usual tube line(s) at my typical commute time, so I can be notified of service disruptions before I leave home.

**Why this priority**: This is the core value proposition of the feature. Without the ability to create a basic alert, the entire feature provides no value. This is the minimal viable product (MVP).

**Independent Test**: Can be fully tested by creating a new alarm with a time, at least one weekday, and at least one tube line, then verifying the alarm appears in the list and triggers a notification at the scheduled time. Delivers immediate value by enabling users to receive proactive service status updates.

**Acceptance Scenarios**:

1. **Given** the user opens the Status Alerts screen (with notification permission granted), **When** they tap the FAB button, **Then** a bottom sheet appears with time picker, weekday selector, and tube line picker
2. **Given** the user has selected a time (e.g., 7:30 AM), at least one weekday (e.g., Monday), and at least one tube line (e.g., Central), **When** they save the alarm, **Then** the alarm appears in the list view with the configured details
3. **Given** an alarm is scheduled for 7:30 AM on Monday for the Central line, **When** Monday arrives and the time is 7:30 AM, **Then** the user receives a notification with the Central line's current status
4. **Given** the alarm triggers and the Central line status is "Good Service", **When** the notification is displayed, **Then** it is a silent notification
5. **Given** the alarm triggers and the Central line status is "Minor Delays", **When** the notification is displayed, **Then** it is an audible notification with sound

---

### User Story 3 - Manage Multiple Tube Lines Per Alarm (Priority: P1)

As a commuter with route options, I want to monitor multiple tube lines in a single alarm (e.g., both Piccadilly and Northern lines), so I can decide which route to take based on current service status.

**Why this priority**: Essential for practical use. Most London commuters have multiple route options and need to compare status across lines. Without this, users would need multiple alarms for the same time, which is cumbersome and defeats the purpose of flexible route planning.

**Independent Test**: Can be fully tested by creating an alarm with multiple tube lines selected (e.g., Piccadilly and Northern), verifying the alarm displays all selected lines, and confirming the notification includes status for all selected lines. Delivers value by enabling route comparison.

**Acceptance Scenarios**:

1. **Given** the user is creating/editing an alarm, **When** they open the tube line picker, **Then** they can select multiple tube lines (multi-select interface)
2. **Given** the user has selected 3 tube lines (e.g., Central, Northern, Victoria), **When** they save the alarm, **Then** the list item displays all 3 tube lines
3. **Given** an alarm is configured with 3 tube lines, **When** the alarm triggers, **Then** the notification displays the status for all 3 tube lines
4. **Given** an alarm with multiple tube lines triggers and at least one line has disruptions, **When** the notification is displayed, **Then** it is audible (not silent)
5. **Given** an alarm with multiple tube lines triggers and all lines have "Good Service", **When** the notification is displayed, **Then** it is silent

---

### User Story 4 - Schedule Recurring Weekly Alarms (Priority: P1)

As a weekday commuter, I want to set an alarm that repeats on specific days of the week (e.g., Monday through Friday), so I don't have to create separate alarms for each day.

**Why this priority**: Essential for commuters who have regular weekly schedules. Without recurring alarms, users would need 5 separate alarms for a typical work week, making the feature impractical for daily use.

**Independent Test**: Can be fully tested by creating an alarm for weekdays only (M-F), verifying it triggers on Monday-Friday at the scheduled time but not on Saturday-Sunday, then confirming it continues to trigger in subsequent weeks. Delivers value by matching real-world commuter patterns.

**Acceptance Scenarios**:

1. **Given** the user is creating/editing an alarm, **When** they open the weekday selector, **Then** they can select multiple days of the week
2. **Given** the user has selected Monday, Tuesday, Wednesday, Thursday, and Friday, **When** they save the alarm, **Then** the list item displays "Weekdays" or "M-F"
3. **Given** an alarm is configured for weekdays only, **When** Monday arrives at the scheduled time, **Then** the notification is triggered
4. **Given** an alarm is configured for weekdays only, **When** Saturday arrives at the scheduled time, **Then** no notification is triggered
5. **Given** an alarm triggered on Monday, **When** the next Monday arrives at the scheduled time, **Then** the notification is triggered again (recurring behavior)

---

### User Story 5 - Create One-Time Alarms (Priority: P2)

As an occasional traveler, I want to create an alarm that only triggers once (no recurring days), so I can be notified for a specific one-time journey.

**Why this priority**: Adds flexibility for users who need alerts for non-regular journeys (e.g., weekend trips, special events). While not essential for the core commuter use case, it makes the feature more versatile.

**Independent Test**: Can be fully tested by creating an alarm with no weekdays selected (one-time mode), verifying it triggers once at the scheduled time/date, then confirming it doesn't trigger again. Delivers value for ad-hoc journey planning.

**Acceptance Scenarios**:

1. **Given** the user is creating an alarm, **When** they don't select any weekdays (or explicitly select "One-time" mode), **Then** the alarm is configured as a one-time alarm
2. **Given** a one-time alarm is scheduled for 3:00 PM today, **When** 3:00 PM arrives, **Then** the notification is triggered once
3. **Given** a one-time alarm has already triggered, **When** the next day arrives at the same time, **Then** no notification is triggered
4. **Given** a one-time alarm has triggered, **When** viewing the alarms list, **Then** the alarm is automatically disabled or marked as expired

---

### User Story 6 - Enable/Disable Alarms Without Deleting (Priority: P2)

As a user with varying schedules, I want to temporarily disable an alarm without deleting it, so I can quickly reactivate it later (e.g., during vacation or schedule changes).

**Why this priority**: Significantly improves usability by allowing users to pause alarms without losing their configuration. Common use case for holidays, schedule changes, or remote work days.

**Independent Test**: Can be fully tested by creating an alarm, toggling it off, verifying no notification is triggered at the scheduled time, then toggling it back on and confirming notifications resume. Delivers value by preserving alarm configurations.

**Acceptance Scenarios**:

1. **Given** the user is viewing the alarms list, **When** they see an active alarm, **Then** a toggle switch (or similar control) is displayed next to the alarm
2. **Given** an alarm is currently enabled, **When** the user taps the toggle to disable it, **Then** the alarm is disabled and no longer triggers notifications
3. **Given** an alarm is currently disabled, **When** the scheduled time arrives, **Then** no notification is triggered
4. **Given** an alarm is disabled, **When** the user taps the toggle to enable it, **Then** the alarm is re-enabled and resumes triggering notifications at scheduled times
5. **Given** an alarm is disabled, **When** viewing the list, **Then** the alarm is visually distinguished (e.g., grayed out or with a "disabled" indicator)

---

### User Story 7 - Edit Existing Alarms (Priority: P2)

As a user whose schedule has changed, I want to edit an existing alarm's time, days, or tube lines, so I can update my notifications without deleting and recreating the alarm.

**Why this priority**: Improves user experience by allowing modifications to existing alarms. While users could delete and recreate alarms, editing is more convenient and intuitive.

**Independent Test**: Can be fully tested by creating an alarm, tapping it to open the bottom sheet, modifying the time/days/lines, saving, and verifying the changes are reflected in the list and notification behavior. Delivers value through convenience.

**Acceptance Scenarios**:

1. **Given** the user taps an existing alarm in the list, **When** the bottom sheet appears, **Then** it displays the current alarm configuration (time, weekdays, tube lines) in editable form
2. **Given** the user has modified the alarm time from 7:30 AM to 8:00 AM, **When** they save the changes, **Then** the alarm list reflects the new time
3. **Given** the user has modified the weekdays from "Weekdays" to "Monday and Wednesday only", **When** they save the changes, **Then** the alarm only triggers on Mondays and Wednesdays
4. **Given** the user has added the Victoria line to an alarm that previously only had the Central line, **When** they save the changes, **Then** the notification includes both Central and Victoria line statuses
5. **Given** the user has made changes in the bottom sheet, **When** they cancel or dismiss without saving, **Then** the alarm retains its original configuration

---

### User Story 8 - Delete Alarms (Priority: P3)

As a user who no longer needs an alarm, I want to delete it permanently, so my alarms list stays clean and organized.

**Why this priority**: Basic housekeeping functionality. While important for long-term usability, it's not critical for the core value proposition. Users can always disable alarms if deletion isn't available immediately.

**Independent Test**: Can be fully tested by creating an alarm, deleting it (via swipe, long-press, or delete button), verifying it's removed from the list, and confirming no notifications are triggered. Delivers value through list management.

**Acceptance Scenarios**:

1. **Given** the user is viewing an alarm in the list or bottom sheet, **When** they access the delete option (e.g., swipe to delete, delete button), **Then** a confirmation prompt appears
2. **Given** the user confirms deletion, **When** the deletion is processed, **Then** the alarm is permanently removed from the list
3. **Given** an alarm has been deleted, **When** the scheduled time arrives, **Then** no notification is triggered
4. **Given** the user has deleted their last alarm, **When** viewing the alarms list, **Then** the empty state is displayed

---

### User Story 9 - Prevent Exceeding Maximum Alarm Limit (Priority: P3)

As the system, I want to enforce a maximum of 10 alarms per user, so the feature remains performant and the UI doesn't become overwhelming.

**Why this priority**: System constraint and edge case handling. Important for maintaining app quality and performance, but not part of the core user journey. Most users won't hit this limit in normal usage.

**Independent Test**: Can be fully tested by creating 10 alarms, verifying the FAB is hidden, attempting to create an 11th alarm (should fail or show message), then deleting one alarm and confirming the FAB reappears. Delivers value by preventing system abuse.

**Acceptance Scenarios**:

1. **Given** the user has created 9 alarms, **When** they view the alarms list, **Then** the FAB is still visible
2. **Given** the user has created 10 alarms, **When** they view the alarms list, **Then** the FAB is hidden
3. **Given** the user has 10 alarms and tries to create another via any method, **When** the attempt is made, **Then** an error message explains the limit has been reached
4. **Given** the user has 10 alarms and deletes one, **When** they return to the list, **Then** the FAB reappears
5. **Given** the user has 10 alarms, **When** viewing the list, **Then** a visual indicator (e.g., "10/10 alarms") shows they've reached the limit

---

### Edge Cases

- **Past time handling**: When a user creates an alarm with a time that has already passed today, the system MUST wait until tomorrow at the specified time for one-time alarms, or wait for the next selected weekday for recurring alarms
- **Missed alarm handling**: When the user's device is turned off or in airplane mode at the scheduled time, the system MUST silently skip the missed alarm and wait for the next scheduled occurrence (no missed notification shown)
- What happens when TfL API is unavailable or returns an error at the time an alarm triggers? System MUST display a notification with the error message: "We tried to check the status for the following lines: [line names] but an error occurred"
- **Multiple tube lines notification**: When a user selects all 11 tube lines (or many lines) for a single alarm, the system MUST display as a single long notification with expandable content (not split into multiple notifications)
- How does the system handle daylight saving time transitions? If an alarm is set for 2:30 AM on a DST change day, what behavior occurs?
- What happens when the user's device is in Do Not Disturb mode at the scheduled time? Should alarms override DND for disruption notifications?
- How does the system handle rapid status changes? If the status changes between the alarm scheduling and trigger time, which status is shown?
- **Conflicting alarms**: When a user has both a recurring alarm and a one-time alarm for the same time and tube line, the system MUST only trigger the recurring alarm (recurring takes precedence, one-time is skipped)
- How does the system handle alarm triggers during app updates or device restarts? Should alarms persist and trigger as expected?

## Requirements

### Functional Requirements

#### Notification Permission
- **FR-001**: System MUST check notification permission status when the Status Alerts screen is opened
- **FR-002**: System MUST request notification permission using Android's runtime permission framework if not already granted
- **FR-003**: System MUST display an informative message if notification permission is denied, explaining that alarms require notifications
- **FR-004**: System MUST provide a way to navigate to system settings if permission was previously denied and cannot be requested again

#### Navigation & Screen Structure
- **FR-005**: System MUST add a second tab to the bottom navigation bar labeled "Alerts" or "Status Alerts"
- **FR-006**: System MUST display the Status Alerts screen when the user taps the Alerts tab
- **FR-007**: System MUST maintain navigation state when switching between tabs (e.g., returning to Alerts tab should preserve scroll position)

#### Empty State
- **FR-008**: System MUST display an empty state when the user has no configured alarms
- **FR-009**: Empty state MUST include explanatory text guiding the user to create their first alarm
- **FR-010**: Empty state MUST include a visible call-to-action (e.g., "Create your first alert" button or prominent FAB)

#### Alarm List View
- **FR-011**: System MUST display all configured alarms in a scrollable list view
- **FR-012**: Each alarm list item MUST display the scheduled time (e.g., "7:30 AM")
- **FR-013**: Each alarm list item MUST display the selected weekdays (e.g., "Weekdays", "M W F", or "Every day")
- **FR-014**: Each alarm list item MUST display the tube line(s) associated with the alarm (e.g., line names or colored badges)
- **FR-015**: Each alarm list item MUST include an enable/disable toggle that persists the alarm's active state
- **FR-016**: System MUST visually distinguish disabled alarms from enabled alarms (e.g., grayed out or reduced opacity)
- **FR-017**: System MUST sort alarms by time (earliest to latest) in the list view
- **FR-018**: System MUST allow the user to tap a list item to open the alarm configuration bottom sheet

#### Floating Action Button (FAB)
- **FR-019**: System MUST display a FAB on the Status Alerts screen when fewer than 10 alarms exist
- **FR-020**: System MUST hide the FAB when the user has created 10 alarms (maximum limit)
- **FR-021**: System MUST open the alarm configuration bottom sheet in "create new" mode when the FAB is tapped
- **FR-022**: System MUST NOT display the FAB when viewing the empty state if there's already a prominent call-to-action button

#### Alarm Configuration Bottom Sheet
- **FR-023**: System MUST display a bottom sheet for alarm configuration when the FAB is tapped or an existing alarm is tapped
- **FR-024**: Bottom sheet MUST include a time picker component for selecting the notification time
- **FR-025**: Time picker MUST support both 12-hour and 24-hour format based on device settings
- **FR-026**: Bottom sheet MUST include a weekday selector for choosing recurring days (Monday through Sunday)
- **FR-027**: Weekday selector MUST support multi-select (user can select multiple days)
- **FR-028**: Weekday selector MUST support selecting no days (one-time alarm mode)
- **FR-029**: Bottom sheet MUST include a tube line picker for selecting which lines to monitor
- **FR-030**: Tube line picker MUST display all available London Underground tube lines
- **FR-031**: Tube line picker MUST support multi-select (user can select multiple tube lines)
- **FR-032**: Tube line picker MUST require at least one tube line to be selected before saving
- **FR-033**: Bottom sheet MUST include a "Save" button to commit the alarm configuration
- **FR-034**: Bottom sheet MUST include a "Cancel" or dismiss action to discard changes
- **FR-035**: System MUST validate that required fields (time, at least one tube line) are filled before allowing save
- **FR-036**: System MUST display appropriate error messages if validation fails (e.g., "Please select at least one tube line")

#### Alarm Creation & Editing
- **FR-037**: System MUST persist newly created alarms to local storage
- **FR-038**: System MUST update existing alarms when modified via the bottom sheet
- **FR-039**: System MUST immediately reflect changes in the alarms list after save
- **FR-040**: System MUST assign a unique identifier to each alarm for tracking and updates

#### Alarm Deletion
- **FR-041**: System MUST provide a mechanism to delete alarms (e.g., swipe to delete, delete button in bottom sheet)
- **FR-042**: System MUST show a confirmation prompt before permanently deleting an alarm
- **FR-043**: System MUST remove the alarm from the list and cancel all scheduled notifications upon deletion

#### Alarm Limit Enforcement
- **FR-044**: System MUST enforce a maximum limit of 10 alarms per user
- **FR-045**: System MUST prevent creation of new alarms when the limit is reached
- **FR-046**: System MUST display an informative message when the user attempts to create an alarm at maximum capacity
- **FR-047**: System MUST show the current alarm count (e.g., "9/10 alarms") somewhere in the UI

#### Notification Triggering
- **FR-048**: System MUST schedule notifications based on the configured alarm time and weekdays
- **FR-049**: System MUST fetch the current status of all selected tube lines when an alarm triggers
- **FR-050**: System MUST display the tube line status(es) in the notification content
- **FR-051**: System MUST determine notification sound behavior based on tube line status:
  - If ALL tube lines have status "Good Service", notification MUST be silent
  - If ANY tube line has a status other than "Good Service" (e.g., Minor Delays, Severe Delays, Closure), notification MUST include audible sound
- **FR-052**: System MUST display different notification icons or colors based on service status severity
- **FR-053**: System MUST persist alarm schedules across device reboots (alarms remain active after restart)
- **FR-054**: System MUST display an error notification if TfL API is unavailable or returns an error when an alarm triggers
- **FR-055**: Error notification MUST include the message: "We tried to check the status for the following lines: [line names] but an error occurred"
- **FR-056**: System MUST detect conflicting alarms (recurring and one-time with same time and tube lines) and only trigger the recurring alarm (recurring takes precedence)

#### Notification Content
- **FR-057**: Notification MUST display the name(s) of the tube line(s) being monitored
- **FR-058**: Notification MUST display the current status of each tube line (e.g., "Good Service", "Minor Delays")
- **FR-059**: Notification MUST be tappable and open the app to the relevant screen (e.g., Line Details for that line)
- **FR-060**: Notification MUST include the time the status was checked
- **FR-061**: Notification MUST handle multiple tube lines gracefully (e.g., "Central: Good Service, Northern: Minor Delays")
- **FR-062**: Notification MUST NOT include snooze or "remind me later" action buttons (keep notifications simple and informational)
- **FR-063**: Notification MUST display all selected tube lines in a single notification with expandable content (not split into multiple notifications), even when many lines are selected

#### Data Persistence
- **FR-064**: System MUST persist all alarm configurations (time, weekdays, tube lines, enabled state) locally
- **FR-065**: System MUST restore all alarms when the app is reopened
- **FR-066**: System MUST maintain alarm state (enabled/disabled) across app sessions

#### Time Handling
- **FR-067**: System MUST use the device's current time zone for alarm scheduling
- **FR-068**: System MUST handle daylight saving time transitions correctly
- **FR-069**: System MUST trigger alarms at the configured time in the device's current time zone

### Key Entities

- **StatusAlert**: Represents a configured alarm/alert with attributes:
  - Unique identifier (UUID or auto-increment ID)
  - Time (hour and minute)
  - Selected weekdays (set of days: Monday-Sunday, empty set indicates one-time alarm)
  - Selected tube lines (list of tube line IDs or names)
  - Enabled/disabled state
  - Creation timestamp
  - Last modified timestamp

- **TubeLine**: Represents a London Underground line with attributes:
  - Line ID (e.g., "central", "northern")
  - Display name (e.g., "Central", "Northern")
  - Line color (for visual representation)
  - Relationships: Many-to-many with StatusAlert (one alarm can monitor multiple lines, one line can be in multiple alarms)

- **Notification**: Represents a triggered notification with attributes:
  - Associated StatusAlert ID
  - Trigger timestamp
  - Tube line statuses at trigger time (snapshot)
  - Silent vs. audible flag
  - Delivered state

## Success Criteria

### Measurable Outcomes

- **SC-001**: Users can create a new status alert with time, weekdays, and tube lines in under 60 seconds
- **SC-002**: The alarms list loads and displays all configured alarms in under 500ms
- **SC-003**: Notifications are triggered within 30 seconds of the scheduled alarm time (accounting for system scheduling tolerances)
- **SC-004**: Silent notifications are correctly triggered for "Good Service" status 100% of the time
- **SC-005**: Audible notifications are correctly triggered for disruption statuses (anything other than "Good Service") 100% of the time
- **SC-006**: Users can enable/disable an alarm with a single tap (toggle action)
- **SC-007**: The empty state is displayed correctly when no alarms exist, guiding users to create their first alarm
- **SC-008**: The FAB is hidden when 10 alarms exist and reappears when the count drops below 10
- **SC-009**: Alarms persist and continue functioning correctly after device reboot (100% retention rate)
- **SC-010**: 90% of users successfully create their first alarm on the first attempt without errors or confusion

## Assumptions & Dependencies

### Assumptions
1. Notification permissions will be requested at runtime when the user opens the Status Alerts screen
2. Users have a stable internet connection when alarms trigger (required to fetch real-time TfL status)
3. The existing TfL API integration (from line status screen) can be reused for fetching status at alarm trigger time
4. The existing tube line data model and color mappings (from line status screen) can be reused
5. Users understand the concept of recurring alarms (similar to Clock app patterns)
6. Device has accurate date/time settings for the user's current location

### Dependencies
1. **TfL API**: Requires functional TfL API endpoints for fetching real-time tube line status
2. **Android AlarmManager or WorkManager**: Requires system APIs for scheduling background alarm triggers
3. **Local Database (Room)**: Requires database for persisting alarm configurations
4. **Notification Permissions**: Requires runtime notification permissions (will be requested when screen opens)
5. **Existing Line Status Feature**: Depends on existing tube line data models, color definitions, and status type enums from `001-tube-status-screen` feature
6. **Bottom Navigation**: Depends on existing bottom navigation component from main app structure

## Clarifications

### Session 2025-12-29
- Q: When a user creates an alarm with a time that has already passed today, what should happen? → A: Wait until tomorrow at the specified time for one-time alarms, or wait for next selected weekday for recurring alarms
- Q: Should alarms that miss their trigger time (e.g., device was off or in airplane mode) show a "missed notification" when the device comes back online? → A: No - Silently skip and wait for the next scheduled occurrence (standard alarm behavior)
- Q: Should there be a "snooze" or "remind me later" option in the notification for disrupted services? → A: No - Keep notifications simple and informational only (no action buttons beyond tapping to open app)
- Q: When a user selects all 11 tube lines for a single alarm, how should the notification be displayed? → A: Display as a single long notification with expandable content
- Q: What happens when a user has both a recurring alarm and a one-time alarm for the same time and tube line? → A: Only trigger the recurring alarm (recurring takes precedence)

### Open Questions
