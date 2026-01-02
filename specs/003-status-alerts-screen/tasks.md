# Implementation Tasks: Status Alerts Screen

**Feature**: `003-status-alerts-screen` | **Date**: 2025-12-29
**Branch**: `003-status-alerts-screen` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Overview

This document provides executable implementation tasks organized by user story priority. Each phase corresponds to a user story from the specification, enabling independent implementation and incremental delivery of value.

**User Input**: do not generate any test tasks (both manual or automated)

---

## Implementation Strategy

### MVP Scope (Recommended First Release)
**User Story 1 (P1)**: Grant Notification Permission
**User Story 2 (P1)**: Create Basic Status Alert
**User Story 3 (P1)**: Manage Multiple Tube Lines Per Alarm
**User Story 4 (P1)**: Schedule Recurring Weekly Alarms

This MVP delivers the core value: Users can create recurring alarms for multiple tube lines and receive notifications at scheduled times.

### Post-MVP Enhancements
- **User Story 5-7 (P2)**: One-time alarms, enable/disable, editing
- **User Story 8-9 (P3)**: Deletion and system limits

### Parallel Execution Strategy
Tasks marked with **[P]** can be executed in parallel with other [P] tasks in the same phase, as they operate on different files with no mutual dependencies.

---

## Phase 1: Setup & Infrastructure

**Goal**: Prepare project structure, manifest permissions, and foundational components.

**Tasks**:
- [x] T001 Add notification permissions to AndroidManifest.xml at app/src/main/AndroidManifest.xml (POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, RECEIVE_BOOT_COMPLETED)
- [x] T002 Create feature package structure at app/src/main/java/com/smartcommute/feature/statusalerts/ with subdirectories: ui, data/local, data/repository, data/receiver, domain/model, domain/util, notification, di
- [x] T003 Create notification channels initialization in Application class (create silent, default, and urgent channels in SmartCommuteApplication.kt onCreate)

**Completion Criteria**: Manifest permissions added, directory structure created, notification channels registered.

---

## Phase 2: Foundational Data Layer

**Goal**: Build Room database entities, DAOs, domain models, and repository infrastructure that all user stories depend on.

**Dependencies**: Phase 1 complete

**Tasks**:
- [x] T004 Create StatusAlert domain model in app/src/main/java/com/smartcommute/feature/statusalerts/domain/model/StatusAlert.kt (include id, time, selectedDays, selectedTubeLines, isEnabled, createdAt, lastModifiedAt, getDisplayTime, getDisplayDays methods)
- [x] T005 [P] Create StatusAlertEntity Room entity in app/src/main/java/com/smartcommute/feature/statusalerts/data/local/StatusAlertEntity.kt (with toDomain and toEntity extension functions)
- [x] T006 Create StatusAlertDao interface in app/src/main/java/com/smartcommute/feature/statusalerts/data/local/StatusAlertDao.kt (observeAll, getEnabledAlarms, getById, insert, update, deleteById, getCount, enable, disable methods)
- [x] T007 Update existing Room database class to include StatusAlertEntity and StatusAlertDao (increment database version, add migration MIGRATION_1_2)
- [x] T008 [P] Create AlarmScheduler interface in app/src/main/java/com/smartcommute/feature/statusalerts/domain/util/AlarmScheduler.kt
- [x] T009 Implement AlarmSchedulerImpl in app/src/main/java/com/smartcommute/feature/statusalerts/domain/util/AlarmSchedulerImpl.kt (scheduleAlarm, cancelAlarm, calculateNextTriggerTime methods with exact alarm API)
- [x] T010 Create StatusAlertsRepository interface in app/src/main/java/com/smartcommute/feature/statusalerts/data/repository/StatusAlertsRepository.kt (observeAllAlarms, getEnabledAlarms, getAlarmById, createAlarm, updateAlarm, deleteAlarm, enableAlarm, disableAlarm, getAlarmCount methods)
- [x] T011 Implement StatusAlertsRepositoryImpl in app/src/main/java/com/smartcommute/feature/statusalerts/data/repository/StatusAlertsRepositoryImpl.kt (integrate Room DAO and AlarmScheduler, implement 10-alarm limit validation)
- [x] T012 [P] Create AlarmModule Hilt module in app/src/main/java/com/smartcommute/feature/statusalerts/di/AlarmModule.kt (provide AlarmManager and AlarmScheduler)
- [x] T013 [P] Create StatusAlertsRepositoryModule Hilt module in app/src/main/java/com/smartcommute/feature/statusalerts/di/StatusAlertsRepositoryModule.kt (bind repository interface to implementation)
- [x] T014 [P] Create UI state models in app/src/main/java/com/smartcommute/feature/statusalerts/ui/StatusAlertsUiState.kt (StatusAlertsUiState sealed class with Loading, Success, Error states)
- [x] T015 [P] Create AlarmConfigurationState in app/src/main/java/com/smartcommute/feature/statusalerts/ui/AlarmConfigurationState.kt (with validation logic)

**Completion Criteria**: All foundational data infrastructure ready for user story implementation.

---

## Phase 3: User Story 1 - Grant Notification Permission (P1)

**Goal**: Request and handle notification permission when user opens Status Alerts screen.

**Dependencies**: Phase 2 complete

**User Story**: As a user, I want to be prompted to grant notification permission when I open the Status Alerts screen, so I can receive alarm notifications.

**Independent Test**: Open Status Alerts screen without notification permission granted → verify permission prompt appears → grant permission → confirm normal UI displays.

**Tasks**:
- [x] T016 [P] [US1] Create basic StatusAlertsViewModel in app/src/main/java/com/smartcommute/feature/statusalerts/ui/StatusAlertsViewModel.kt (inject repository, expose StateFlow<StatusAlertsUiState>, implement observeAlarms method)
- [x] T017 [US1] Create StatusAlertsScreen composable in app/src/main/java/com/smartcommute/feature/statusalerts/ui/StatusAlertsScreen.kt (implement notification permission request logic using rememberLauncherForActivityResult, display permission denial dialog with settings navigation)
- [x] T018 [US1] Add status alerts navigation route to NavGraph.kt in app/src/main/java/com/smartcommute/core/navigation/NavGraph.kt (route: "status_alerts")
- [x] T019 [US1] Update bottom navigation in MainScreen.kt to include "Alerts" tab with navigation to StatusAlertsScreen

**Completion Criteria**: Permission requested on screen open, denial handled with settings guidance, normal UI displays after grant.

---

## Phase 4: User Story 2 - Create Basic Status Alert (P1)

**Goal**: Enable users to create alarms with time, weekdays, and tube lines via FAB and bottom sheet.

**Dependencies**: Phase 3 complete

**User Story**: As a regular commuter, I want to create a status alert for my usual tube line(s) at my typical commute time, so I can be notified of service disruptions before I leave home.

**Independent Test**: Tap FAB → configure time, weekday, tube line → save → verify alarm appears in list → verify notification triggers at scheduled time.

**Tasks**:
- [ ] T020 [P] [US2] Create EmptyStateView composable in app/src/main/java/com/smartcommute/feature/statusalerts/ui/EmptyStateView.kt (display when no alarms exist, include call-to-action)
- [ ] T021 [P] [US2] Create TimePickerDialog composable in app/src/main/java/com/smartcommute/feature/statusalerts/ui/components/TimePickerDialog.kt (Material Design 3 TimePicker in AlertDialog)
- [ ] T022 [P] [US2] Create WeekdaySelector composable in app/src/main/java/com/smartcommute/feature/statusalerts/ui/components/WeekdaySelector.kt (FilterChip multi-select for days of week)
- [ ] T023 [P] [US2] Create TubeLinePicker composable in app/src/main/java/com/smartcommute/feature/statusalerts/ui/components/TubeLinePicker.kt (Checkbox + LazyColumn, fetch available lines from LineStatusRepository)
- [ ] T024 [US2] Create AlarmBottomSheet composable in app/src/main/java/com/smartcommute/feature/statusalerts/ui/AlarmBottomSheet.kt (ModalBottomSheet with time picker, weekday selector, tube line picker, save/cancel buttons, validation error display)
- [ ] T025 [US2] Update StatusAlertsScreen to display empty state, alarm list (LazyColumn), and FAB (visible when alarm count < 10)
- [ ] T026 [US2] Implement createAlarm action in StatusAlertsViewModel (validate, call repository, handle errors)
- [ ] T027 [P] [US2] Create AlarmReceiver BroadcastReceiver in app/src/main/java/com/smartcommute/feature/statusalerts/data/receiver/AlarmReceiver.kt (fetch TfL API status, determine silent vs audible, send notification)
- [ ] T028 [P] [US2] Create BootCompletedReceiver BroadcastReceiver in app/src/main/java/com/smartcommute/feature/statusalerts/data/receiver/BootCompletedReceiver.kt (reschedule all enabled alarms on boot)
- [ ] T029 [US2] Register AlarmReceiver and BootCompletedReceiver in AndroidManifest.xml with intent filters
- [ ] T030 [P] [US2] Create NotificationManager utility in app/src/main/java/com/smartcommute/feature/statusalerts/notification/NotificationManager.kt (sendStatusNotification, sendErrorNotification, use InboxStyle for multiple lines)
- [ ] T031 [US2] Implement TfL API integration in AlarmReceiver (reuse existing TfL API service from feature 001 to fetch line statuses)

**Completion Criteria**: Users can create alarms via FAB, alarms persist and display in list, notifications trigger at scheduled time with correct sound behavior.

---

## Phase 5: User Story 3 - Manage Multiple Tube Lines Per Alarm (P1)

**Goal**: Allow users to select multiple tube lines in a single alarm.

**Dependencies**: Phase 4 complete

**User Story**: As a commuter with route options, I want to monitor multiple tube lines in a single alarm, so I can decide which route to take based on current service status.

**Independent Test**: Create alarm with 3 tube lines → save → verify all 3 lines display in list item → trigger alarm → verify notification shows status for all 3 lines.

**Tasks**:
- [ ] T032 [US3] Update TubeLinePicker to support multi-select (already implemented in T023, verify behavior)
- [ ] T033 [US3] Create AlarmListItem composable in app/src/main/java/com/smartcommute/feature/statusalerts/ui/AlarmListItem.kt (display time, weekdays, multiple tube lines with colored badges, enable/disable toggle)
- [ ] T034 [US3] Update StatusAlertsScreen to use AlarmListItem in LazyColumn
- [ ] T035 [US3] Update NotificationManager to handle multiple tube lines in single expandable notification (InboxStyle with all line statuses)
- [ ] T036 [US3] Update AlarmReceiver notification logic to determine silent vs audible based on "if ANY line has disruptions, audible; if ALL lines are Good Service, silent"

**Completion Criteria**: Alarms can include multiple tube lines, list displays all selected lines, notifications show all line statuses in single expandable notification.

---

## Phase 6: User Story 4 - Schedule Recurring Weekly Alarms (P1)

**Goal**: Enable recurring alarms on specific weekdays.

**Dependencies**: Phase 5 complete

**User Story**: As a weekday commuter, I want to set an alarm that repeats on specific days of the week, so I don't have to create separate alarms for each day.

**Independent Test**: Create alarm for Monday-Friday → verify triggers on weekdays → verify does not trigger on Saturday-Sunday → verify triggers again next week.

**Tasks**:
- [ ] T037 [US4] Update AlarmSchedulerImpl calculateNextTriggerTime to handle recurring alarms (find next matching weekday, reschedule after trigger)
- [ ] T038 [US4] Update AlarmReceiver to reschedule recurring alarms after trigger (call AlarmScheduler.scheduleAlarm with next occurrence)
- [ ] T039 [US4] Update AlarmListItem to display weekday abbreviations (M-F, Mon/Wed/Fri, or "Weekdays")
- [ ] T040 [US4] Update BootCompletedReceiver to restore all enabled recurring alarms on device reboot

**Completion Criteria**: Recurring alarms trigger on selected weekdays, reschedule automatically after each trigger, persist across device reboots.

---

## Phase 7: User Story 5 - Create One-Time Alarms (P2)

**Goal**: Support alarms that trigger once (no recurring days).

**Dependencies**: Phase 6 complete

**User Story**: As an occasional traveler, I want to create an alarm that only triggers once, so I can be notified for a specific one-time journey.

**Independent Test**: Create alarm with no weekdays selected → trigger once at scheduled time → verify does not trigger again → verify alarm auto-disabled.

**Tasks**:
- [ ] T041 [US5] Update WeekdaySelector to support selecting no days (allow empty selection)
- [ ] T042 [US5] Update AlarmSchedulerImpl to handle one-time alarms (if past time today, schedule for tomorrow)
- [ ] T043 [US5] Update AlarmReceiver to auto-disable one-time alarms after trigger (call repository.disableAlarm)
- [ ] T044 [US5] Update AlarmListItem to display "One time" label for alarms with no selected weekdays

**Completion Criteria**: One-time alarms trigger once at scheduled time, auto-disable after trigger, display correctly in list.

---

## Phase 8: User Story 6 - Enable/Disable Alarms Without Deleting (P2)

**Goal**: Allow users to toggle alarms on/off without deleting configuration.

**Dependencies**: Phase 7 complete

**User Story**: As a user with varying schedules, I want to temporarily disable an alarm without deleting it, so I can quickly reactivate it later.

**Independent Test**: Create alarm → toggle off → verify no notification at scheduled time → toggle on → verify notifications resume.

**Tasks**:
- [ ] T045 [US6] Update AlarmListItem to include enable/disable toggle Switch (call viewModel.toggleAlarmEnabled on change)
- [ ] T046 [US6] Implement toggleAlarmEnabled action in StatusAlertsViewModel (call repository.enableAlarm or disableAlarm)
- [ ] T047 [US6] Update AlarmListItem to visually distinguish disabled alarms (grayed out, reduced opacity)
- [ ] T048 [US6] Update FAB visibility logic to count only enabled alarms for display (FAB hidden when enabled count >= 10)

**Completion Criteria**: Users can toggle alarms on/off, disabled alarms don't trigger notifications, visual distinction in list.

---

## Phase 9: User Story 7 - Edit Existing Alarms (P2)

**Goal**: Allow users to modify alarm configuration.

**Dependencies**: Phase 8 complete

**User Story**: As a user whose schedule has changed, I want to edit an existing alarm's time, days, or tube lines, so I can update my notifications without deleting and recreating the alarm.

**Independent Test**: Create alarm → tap to open bottom sheet → modify time/days/lines → save → verify changes reflected in list and notification behavior.

**Tasks**:
- [ ] T049 [US7] Update StatusAlertsScreen to open AlarmBottomSheet in edit mode when alarm list item is tapped (pass alarm ID to bottom sheet)
- [ ] T050 [US7] Update AlarmBottomSheet to support edit mode (pre-fill fields with existing alarm data, update Save button to call updateAlarm)
- [ ] T051 [US7] Implement updateAlarm action in StatusAlertsViewModel (validate, call repository, handle errors)
- [ ] T052 [US7] Update AlarmBottomSheet to support cancel action (dismiss without saving changes)

**Completion Criteria**: Users can tap alarms to edit, modifications persist, changes reflected immediately in list and notification behavior.

---

## Phase 10: User Story 8 - Delete Alarms (P3)

**Goal**: Allow users to permanently remove alarms.

**Dependencies**: Phase 9 complete

**User Story**: As a user who no longer needs an alarm, I want to delete it permanently, so my alarms list stays clean and organized.

**Independent Test**: Create alarm → delete via swipe or delete button → confirm deletion → verify removed from list → verify no notifications triggered.

**Tasks**:
- [ ] T053 [P] [US8] Add delete button to AlarmBottomSheet (call viewModel.deleteAlarm with confirmation dialog)
- [ ] T054 [US8] Implement deleteAlarm action in StatusAlertsViewModel (show confirmation dialog, call repository on confirm)
- [ ] T055 [US8] Update StatusAlertsScreen to show empty state when last alarm is deleted
- [ ] T056 [P] [US8] Optional: Implement swipe-to-delete gesture in AlarmListItem using DismissibleState (alternative deletion method)

**Completion Criteria**: Users can delete alarms with confirmation, alarms removed from list, empty state displays when no alarms remain.

---

## Phase 11: User Story 9 - Prevent Exceeding Maximum Alarm Limit (P3)

**Goal**: Enforce 10-alarm limit, hide FAB when limit reached.

**Dependencies**: Phase 10 complete

**User Story**: As the system, I want to enforce a maximum of 10 alarms per user, so the feature remains performant and the UI doesn't become overwhelming.

**Independent Test**: Create 10 alarms → verify FAB hidden → attempt to create 11th → verify error message → delete 1 alarm → verify FAB reappears.

**Tasks**:
- [ ] T057 [US9] Update StatusAlertsScreen FAB visibility logic to hide when alarm count >= 10 (already partially implemented, verify behavior)
- [ ] T058 [US9] Update createAlarm validation in StatusAlertsViewModel to show error message when limit reached ("Maximum 10 alarms reached. Delete an alarm to create a new one.")
- [ ] T059 [US9] Add optional alarm count indicator to StatusAlertsScreen (e.g., "9/10 alarms" text in top bar or list footer)

**Completion Criteria**: FAB hidden at 10 alarms, error message displayed on creation attempt, FAB reappears when count drops below 10.

---

## Phase 12: Polish & Cross-Cutting Concerns

**Goal**: Add final polish, error handling, and edge case coverage.

**Dependencies**: All user story phases complete

**Tasks**:
- [ ] T060 [P] Add string resources to app/src/main/res/values/strings.xml for all UI text (no hardcoded strings, support localization)
- [ ] T061 [P] Add content descriptions to all composables for TalkBack accessibility (alarms, FAB, bottom sheet controls)
- [ ] T062 [P] Implement error notification when TfL API fails (display message: "We tried to check the status for the following lines: [line names] but an error occurred")
- [ ] T063 [P] Handle conflicting alarms logic in AlarmReceiver (recurring takes precedence over one-time at same time)
- [ ] T064 [P] Handle past time alarm creation (if time passed today, schedule for tomorrow or next matching weekday)
- [ ] T065 [P] Add sorting to alarm list (earliest to latest time)

**Completion Criteria**: All text localized, accessibility support complete, edge cases handled gracefully.

---

## Dependency Graph

### Story Completion Order

```
Phase 1 (Setup) → Phase 2 (Foundational)
                         ↓
           Phase 3 (US1: Notification Permission) [P1]
                         ↓
           Phase 4 (US2: Create Basic Alert) [P1]
                         ↓
           Phase 5 (US3: Multiple Tube Lines) [P1]
                         ↓
           Phase 6 (US4: Recurring Alarms) [P1]
                         ↓
           Phase 7 (US5: One-Time Alarms) [P2]
                         ↓
           Phase 8 (US6: Enable/Disable) [P2]
                         ↓
           Phase 9 (US7: Edit Alarms) [P2]
                         ↓
           Phase 10 (US8: Delete Alarms) [P3]
                         ↓
           Phase 11 (US9: Alarm Limit) [P3]
                         ↓
           Phase 12 (Polish)
```

### Parallel Execution Examples

**Within Phase 2 (Foundational)**:
- T005 (StatusAlertEntity), T008 (AlarmScheduler interface), T012 (AlarmModule), T013 (RepositoryModule), T014 (UI states), T015 (AlarmConfigurationState) can run in parallel (different files, no dependencies).

**Within Phase 4 (US2)**:
- T020 (EmptyStateView), T021 (TimePickerDialog), T022 (WeekdaySelector), T023 (TubeLinePicker), T027 (AlarmReceiver), T028 (BootCompletedReceiver), T030 (NotificationManager) can run in parallel.

**Within Phase 12 (Polish)**:
- T060, T061, T062, T063, T064, T065 can all run in parallel (independent enhancements).

---

## Task Summary

**Total Tasks**: 65
- Phase 1 (Setup): 3 tasks
- Phase 2 (Foundational): 12 tasks
- Phase 3 (US1 - P1): 4 tasks
- Phase 4 (US2 - P1): 12 tasks
- Phase 5 (US3 - P1): 5 tasks
- Phase 6 (US4 - P1): 4 tasks
- Phase 7 (US5 - P2): 4 tasks
- Phase 8 (US6 - P2): 4 tasks
- Phase 9 (US7 - P2): 4 tasks
- Phase 10 (US8 - P3): 4 tasks
- Phase 11 (US9 - P3): 3 tasks
- Phase 12 (Polish): 6 tasks

**Parallel Opportunities**: 32 tasks marked with [P] can be executed in parallel within their respective phases.

**MVP Scope**: Phases 1-6 (38 tasks) deliver fully functional recurring status alerts with notification permission, alarm creation, multiple tube lines, and recurring scheduling.

**Independent Test Criteria**: Each user story phase includes explicit test scenario for independent validation.

---

## Format Validation

✅ All tasks follow strict checklist format: `- [ ] [TaskID] [P] [StoryLabel] Description with file path`
✅ Tasks organized by user story for independent implementation
✅ Dependencies clearly marked between phases
✅ Parallel execution opportunities identified with [P] marker
✅ File paths specified for each implementation task
✅ No test tasks generated (per user request)

---

## Next Steps

1. **Start with MVP (Phases 1-6)**: Implement P1 user stories first for maximum value delivery
2. **Parallel execution**: Leverage [P] tasks within each phase to speed up development
3. **Independent validation**: Test each phase's completion criteria before proceeding
4. **Incremental deployment**: Each phase delivers working, independently testable functionality

Ready for implementation! 🚀
