# Feature Specification: Tube Status & Line Details Screen Redesign

**Feature Branch**: `004-tube-screens-redesign`
**Created**: 2026-03-25
**Status**: Draft
**Input**: User description: "Redesign the tube status and line details screens based on new Figma designs. Keep the current refresh button design. Maintain shared element transition animations between screens. Hide line details sections when data is unavailable."

**Design References**:
- Tube Status Screen: [Figma node 2:184](https://www.figma.com/design/275cHCVToITByJf6t6HLkO/Untitled?node-id=2-184)
- Line Details Screen: [Figma node 2:480](https://www.figma.com/design/275cHCVToITByJf6t6HLkO/Untitled?node-id=2-480)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Redesigned Tube Status List (Priority: P1)

A commuter opens the app to check tube line statuses and sees a refreshed, cleaner list design. Each line shows a circular roundel icon inside a lightly tinted background circle, the line name as a heading, the current status as colour-coded subtitle text, and a trailing status icon (checkmark for good service, warning triangle for minor delays, error circle for major delays/severe delays/closure).

**Why this priority**: The tube status list is the app's primary screen and the first thing every user sees. Redesigning it delivers immediate visual impact and improved scannability for all users.

**Independent Test**: Can be fully tested by launching the app and verifying the status list matches the new design: circular tinted icon containers, line names as headings, colour-coded status subtitles, and trailing status indicator icons.

**Acceptance Scenarios**:

1. **Given** the user opens the app, **When** the tube status screen loads, **Then** each line item displays a circular roundel icon inside a circular container with the line's brand colour at 15% opacity as background
2. **Given** the tube status screen is displayed, **When** a line has "Good Service", **Then** the status text is green (#00A63E) and a green checkmark icon appears on the right
3. **Given** the tube status screen is displayed, **When** a line has "Minor Delays", **Then** the status text is amber/orange (#FE9A00) and an orange warning triangle icon appears on the right
4. **Given** the tube status screen is displayed, **When** a line has "Major Delays", **Then** the status text is red/orange (#F54900) and a red error circle icon appears on the right
5. **Given** the tube status screen is displayed, **When** a line has "Severe Delays" or "Closure", **Then** the status text and trailing icon reflect the severity using a distinct colour and icon
6. **Given** the user views the tube status screen, **When** they look at the header, **Then** the refresh button retains its current design (not the new Figma refresh button)
7. **Given** the user views the tube status screen, **When** the header is visible, **Then** the title "London Underground" and "Last updated" subtitle are displayed matching the new layout

---

### User Story 2 - View Redesigned Line Details Screen (Priority: P2)

A commuter taps a line from the status list and sees a redesigned details screen with a clean, card-based layout. The screen shows the line's roundel icon centred at the top inside a large tinted circle, the line name in bold centred text, the status with a colour-coded icon and text centred below the name, and a "Last updated" timestamp. Below this hero section, information cards display operation hours, night tube details, and crowding information on a light grey background.

**Why this priority**: The line details screen is the second most-used screen. Replacing the header image/gradient design with a cleaner card-based layout improves readability, simplifies maintenance (no generated images needed), and provides a more modern feel.

**Independent Test**: Can be tested by tapping any line from the status list and verifying the details screen matches the new card-based design with the centred hero section and information cards.

**Acceptance Scenarios**:

1. **Given** I tap a line from the status list, **When** the line details screen opens, **Then** I see the line's roundel icon centred at the top inside a large circular tinted background
2. **Given** I am viewing the line details screen, **When** the screen renders, **Then** the line name appears in bold, centred text below the icon
3. **Given** I am viewing the line details screen, **When** the line has a non-good-service status, **Then** the status is displayed with a colour-coded icon and text centred below the name
4. **Given** the line details screen is displayed, **When** operation hours data is available, **Then** an "Operation Hours" card appears with a clock icon header and rows showing hours for Monday-Friday, Saturday, and Sunday
5. **Given** the line details screen is displayed, **When** night tube data is available, **Then** a "Night Tube" card appears with a moon icon header, a description of the service, and frequency information
6. **Given** the line details screen is displayed, **When** crowding data is available, **Then** a "Crowding Information" card appears with a people icon header showing peak times and off-peak details
7. **Given** the line details screen is displayed, **When** data for a section (operation hours, night tube, or crowding) is not available, **Then** that entire card/section is hidden from the screen
8. **Given** I am viewing the line details screen, **When** I look at the background, **Then** the area behind the cards uses a light grey background colour (#F9FAFB) while the hero section at the top has a white background
9. **Given** I am viewing the line details screen, **When** I tap the back button, **Then** I return to the tube status list

---

### User Story 3 - Smooth Shared Element Transitions Between Screens (Priority: P3)

A user navigating between the tube status list and the line details screen experiences smooth, continuous animations. The line icon, line name, and status text animate from their positions in the list item to their new positions on the details screen (and back), maintaining visual continuity.

**Why this priority**: Transition animations are a polish feature that enhance perceived app quality. The app is fully functional without them, but they create a premium, fluid navigation experience and help users maintain spatial context.

**Independent Test**: Can be tested by navigating to and from the line details screen and verifying that the line icon, name, and status text animate smoothly between their list positions and their centred positions on the details screen.

**Acceptance Scenarios**:

1. **Given** I tap a line item, **When** the line details screen opens, **Then** the roundel icon animates from its list position to the centred position at the top of the details screen
2. **Given** I tap a line item, **When** navigating to details, **Then** the line name animates from its list position to the centred bold heading position on the details screen
3. **Given** I tap a line item, **When** navigating to details, **Then** the status text animates from its list position to the centred status position below the name on the details screen
4. **Given** I press back from the line details screen, **When** returning to the list, **Then** the shared elements (icon, name, status) animate back to their original positions in the list
5. **Given** transitions are in progress, **When** elements animate, **Then** the animation feels smooth and natural without lag or jarring visual artefacts

---

### Edge Cases

- When cached line data is incomplete or missing optional fields (e.g., no crowding data, no night tube info), the corresponding card/section is completely hidden rather than showing empty or placeholder content
- When the status type is unrecognised (not in the standard set), the trailing icon defaults to a generic warning style and the status text colour falls back to a neutral colour
- When the line details screen is opened for a line that only has basic status data (no operation hours, no night tube, no crowding), only the hero section (icon, name, status, last updated) is displayed with no cards below
- When the user navigates quickly between screens (rapid tap then back then tap), the shared element transitions handle interruption gracefully without visual glitches
- When the device uses large text/accessibility settings, the list items and detail cards scale appropriately without clipping or overlapping

## Requirements *(mandatory)*

### Functional Requirements

#### Tube Status Screen

- **FR-001**: System MUST display each line item with a circular roundel icon inside a circular container with the line's brand colour at 15% opacity as background
- **FR-002**: System MUST display the line name as the primary heading text for each list item
- **FR-003**: System MUST display the current status as colour-coded secondary text below the line name, using green (#00A63E) for Good Service, amber (#FE9A00) for Minor Delays, red/orange (#F54900) for Major Delays, and appropriate colours for Severe Delays and Closure
- **FR-004**: System MUST display a trailing status indicator icon on the right side of each list item: a green checkmark circle for Good Service, an amber warning triangle for Minor Delays, and a red error circle for Major Delays/Severe Delays/Closure
- **FR-005**: System MUST retain the current refresh button design, ignoring the refresh button shown in the new Figma design
- **FR-006**: System MUST preserve all existing functionality (pull-to-refresh, offline banner, error states, loading states, caching, bottom navigation)

#### Line Details Screen

- **FR-007**: System MUST replace the current header image/gradient design with a clean hero section featuring the roundel icon centred inside a large tinted circular background at the top
- **FR-008**: System MUST display the line name in bold, centred text below the hero icon
- **FR-009**: System MUST display the current status with a colour-coded icon and text, centred below the line name
- **FR-010**: System MUST display a "Last updated" timestamp centred below the status
- **FR-011**: System MUST use a light grey background colour (#F9FAFB) for the scrollable content area below the hero section
- **FR-012**: System MUST display an "Operation Hours" card with a clock icon header showing weekday, Saturday, and Sunday operating hours when this data is available
- **FR-013**: System MUST display a "Night Tube" card with a moon icon header showing a service description and frequency when this data is available
- **FR-014**: System MUST display a "Crowding Information" card with a people icon header showing peak times and off-peak information when this data is available
- **FR-015**: System MUST completely hide any card/section for which data is not available, showing no empty states or placeholders
- **FR-016**: System MUST display each information card with white background, rounded corners, and a subtle border
- **FR-017**: System MUST include a back button at the top of the screen to navigate back to the tube status list
- **FR-018**: System MUST retrieve all displayed information from local cache without making network requests

#### Shared Element Transitions

- **FR-019**: System MUST animate the line roundel icon between its list position and the centred hero position on the details screen using shared element transitions
- **FR-020**: System MUST animate the line name text between its list position and the centred heading position on the details screen using shared element transitions
- **FR-021**: System MUST animate the status text between its list position and the centred status position on the details screen using shared element transitions
- **FR-022**: System MUST animate all shared elements back to their list positions when navigating back from the details screen

### Key Entities

- **Tube Line**: A London Underground line with brand colour, roundel icon, name, and current service status
- **Service Status**: The operational status of a line including status type, colour coding, and associated icon style (checkmark, warning, error)
- **Operation Hours**: Operating time ranges broken down by day category (weekday, Saturday, Sunday)
- **Night Tube Info**: Night service availability including description and service frequency
- **Crowding Info**: Passenger volume data including peak time ranges, off-peak descriptions, and crowding levels

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can visually identify a line's status type (good service vs delay severity) within 1 second of viewing the redesigned list, matching or improving the current design's scannability
- **SC-002**: The redesigned tube status list displays all lines with the new visual design (circular tinted icon, colour-coded status text, trailing status icon) matching the Figma design reference
- **SC-003**: The redesigned line details screen displays the hero section (centred icon, name, status, timestamp) and information cards matching the Figma design reference
- **SC-004**: Information cards appear only when their corresponding data is available; no empty cards or placeholder content is shown
- **SC-005**: Shared element transitions between the tube status list and line details screen animate smoothly for the icon, name, and status elements in both navigation directions
- **SC-006**: All existing tube status functionality (pull-to-refresh, offline mode, error handling, caching, bottom navigation) continues to work unchanged after the redesign
- **SC-007**: The redesigned screens display correctly on devices ranging from 5 inches to 10+ inches

## Assumptions

- The existing data models (UndergroundLine, UndergroundLineDetails, ServiceStatus) provide sufficient data to populate the new designs without new API calls or data sources
- Operation hours, night tube info, and crowding data are already available from the TfL API and cached locally by the existing implementation
- The current shared element transition infrastructure can be adapted to the new layout positions without architectural changes
- The existing line brand colours and roundel icons are reusable in the new circular tinted container design
- The existing navigation framework supports the back button at the top of the details screen
- The header station images from the current line details design will be removed and are no longer needed

## Out of Scope

- Redesigning the refresh button on the tube status screen (explicitly excluded per user instruction)
- Changes to bottom navigation bar design or behaviour
- Changes to data fetching, caching, or API integration logic
- Adding new data sources or API endpoints
- Changes to the status alerts screen
- Changes to offline/error state behaviour beyond visual styling
- New animations or interactions beyond the shared element transitions for icon, name, and status
