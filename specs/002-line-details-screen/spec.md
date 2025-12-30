# Feature Specification: Line Details Screen

**Feature Branch**: `002-line-details-screen`
**Created**: 2025-12-28
**Status**: Draft
**Input**: User description: "Line details screen. Let's create the line details screen. This screen will be opened when the user clicks on an item from the list on the tube status screen. The screen must be laid out following the material design recommendations: -It must have an image on the top of the screen taking all the width of the screen and about 1/3 of its height. This image must be of a station from that line on a busy day showing people coming in and out of the station. Generate the images yourself and include them in the project. Make sure to use high resolution images to prevent pixelated images on high end devices. -At the bottom right of the image, anchored to the bottom edge of it, add the icon of the line. This icon is the same as the one in list item that was clicked and must be animated into the line details screen, using shared element transitions -On top of the image, bottom-aligned, add the title and the line status as showed on the list item that was clicked. These elements must also be animated from the tube status screen into the line details screen just like the icon. This animation must use, again, the shared element transition. Make sure to apply proper masks, blur etc on the image to make the title and status texts readable on top of the image -On the body of the screen add as much info about the line as possible, as returned by the TFL API. Ex: planned closure, how busy it is, etc. Come up with a nice design, following the material design guidelines for this section. Make sure to include what you are planning to add to this screen on the plan document. IMPORTANT: NO NETWORK REQUEST MUST BE MADE IN THIS SREEN. All the info must have been already fetched by the tube status screen and cached locally."

## Clarifications

### Session 2025-12-28

- Q: When cached line data is incomplete or a header image is missing, what should the screen display? → A: Display placeholder image and show available data with clear indicator for missing fields
- Q: The spec mentions using station images showing "people coming in and out of the station." Should the images include identifiable people, or should privacy be considered? → A: Use AI-generated images with blurred or anonymized crowd scenes (per research.md, images will be generated as WebP at 1440x960px with anonymized figures)
- Q: How should the screen handle extremely long disruption descriptions or multiple simultaneous service issues? → A: Show preview (first 2-3 lines) with "Read more" expansion option
- Q: How should text readability be ensured when header images have varying brightness levels? → A: Apply dark gradient scrim (overlay) on bottom portion where text appears

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Basic Line Information (Priority: P1)

As a commuter, when I select a tube line from the status screen, I want to see detailed information about that line including its current status and visual branding, so that I can understand the line's current operational state at a glance.

**Why this priority**: This is the core functionality of the screen - displaying essential line information. Without this, the screen has no purpose. Users need immediate visual confirmation they're viewing the correct line with its current status.

**Independent Test**: Can be fully tested by tapping any line from the tube status screen and verifying the line details screen displays the header image, line icon, line name, and current status. Delivers value by providing users with comprehensive line information without requiring additional network calls.

**Acceptance Scenarios**:

1. **Given** I am viewing the tube status screen with multiple lines, **When** I tap on any line item, **Then** the line details screen opens with a full-width header image showing a station from that line
2. **Given** I am viewing a line item on the tube status screen, **When** I navigate to the line details screen, **Then** the line icon, line name, and status are displayed over the header image with sufficient contrast for readability
3. **Given** the line details screen is loading, **When** the screen appears, **Then** the line icon, title, and status smoothly animate from their positions on the list item using shared element transitions
4. **Given** I am using a high-resolution device, **When** I view the line details screen, **Then** the header image appears sharp and crisp without pixelation

---

### User Story 2 - View Comprehensive Line Details (Priority: P2)

As a commuter planning my journey, when I'm viewing a line's details, I want to see all available information about the line including planned closures, crowding levels, and service disruptions, so that I can make informed decisions about my travel plans.

**Why this priority**: This provides additional value beyond basic status by showing comprehensive operational information. It's P2 because the screen is useful with just P1, but this makes it significantly more valuable for journey planning.

**Independent Test**: Can be tested independently by verifying that all cached line data from the TFL API is displayed in the body section, including disruption descriptions, closure information, and crowding indicators. Delivers value by eliminating the need for users to visit external sources for detailed line information.

**Acceptance Scenarios**:

1. **Given** a line has planned closures, **When** I view the line details screen, **Then** I see the closure information displayed in the body section with dates and affected stations
2. **Given** a line has service disruptions, **When** I view the details, **Then** I see the disruption reason and severity clearly displayed
3. **Given** a line has crowding information, **When** I view the details, **Then** I see indicators showing how busy the line is
4. **Given** I am viewing line details, **When** I scroll through the body content, **Then** all information is organized following Material Design guidelines with clear visual hierarchy

---

### User Story 3 - Smooth Visual Transitions (Priority: P3)

As a user navigating the app, when I transition between the tube status list and line details screen, I want to experience smooth, delightful animations that maintain visual continuity, so that the app feels polished and easy to follow.

**Why this priority**: This enhances user experience through visual polish but isn't essential for functionality. The screen works without transitions, but they improve perceived app quality and help users maintain context during navigation.

**Independent Test**: Can be tested independently by navigating to and from the line details screen and verifying that the line icon, title, and status text animate smoothly using shared element transitions. Delivers value by creating a premium, fluid user experience.

**Acceptance Scenarios**:

1. **Given** I tap a line item, **When** the line details screen opens, **Then** the line icon animates from its position in the list to the bottom-right corner of the header image
2. **Given** I tap a line item, **When** navigating to details, **Then** the line name and status text animate from the list item to their positions over the header image
3. **Given** I press back from the line details screen, **When** returning to the list, **Then** the shared elements animate back to their original positions in the list
4. **Given** transitions are in progress, **When** elements animate, **Then** the animation feels smooth and natural without lag or jarring movements

---

### Edge Cases

- When cached line data is incomplete or missing optional fields (e.g., no crowding data available), the screen displays available data with a clear visual indicator (e.g., "No crowding information available") for missing sections
- When a line has no associated header image asset, the screen displays a generic placeholder image (e.g., solid color matching the line's brand color with subtle pattern)
- When disruption descriptions are lengthy or multiple simultaneous service issues exist, the screen displays a preview (first 2-3 lines) with an expandable "Read more" option to reveal full details
- To ensure text readability across varying image brightness levels, a dark gradient scrim (semi-transparent overlay) is applied to the bottom portion of the header image where text appears
- When the user's device has a non-standard aspect ratio affecting the 1/3 height image calculation, the screen uses BoxWithConstraints to calculate maxHeight * 0.33f dynamically, ensuring the header adapts proportionally to any screen size. On extremely tall devices (>21:9 ratio), a maximum height constraint of 400.dp prevents the header from becoming excessively large.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display a full-width header image occupying approximately one-third of screen height when the line details screen opens
- **FR-002**: System MUST display the line icon anchored to the bottom-right corner of the header image
- **FR-003**: System MUST display the line name and current status text overlaid on the header image, bottom-aligned
- **FR-004**: System MUST apply a dark gradient scrim (semi-transparent overlay) to the bottom portion of the header image where text appears to ensure readability across all image brightness levels
- **FR-005**: System MUST animate the line icon from the list item to the line details screen using shared element transitions
- **FR-006**: System MUST animate the line name and status text from the list item to the line details screen using shared element transitions
- **FR-007**: System MUST retrieve all displayed information from local cache without making network requests
- **FR-008**: System MUST display comprehensive line information in the body section including service status details, disruption descriptions, planned closures, and crowding information where available
- **FR-009**: System MUST use high-resolution images (at least 1080px width) for header images to prevent pixelation on high-DPI devices
- **FR-010**: System MUST follow Material Design guidelines for layout, spacing, typography, and component usage in the body section
- **FR-011**: System MUST include station images showing busy commuter scenarios with blurred or anonymized crowd scenes to protect privacy
- **FR-012**: System MUST display a generic placeholder image when a header image asset is missing. The placeholder uses the line's brand color as a solid background with a centered Material Icons "train" icon (48.dp size) in white with 60% opacity, creating a simple, branded fallback.
- **FR-013**: System MUST display clear visual indicators (e.g., "Information not available") for any missing or incomplete data fields in the body section
- **FR-014**: System MUST display lengthy disruption descriptions as a preview (first 2-3 lines) with an expandable "Read more" option to reveal full content

### Key Entities

- **Line**: Represents a tube line with attributes including line name, line identifier, brand color, icon, current operational status, status severity level, and associated header image asset
- **Service Disruption**: Represents a service issue affecting a line including disruption description, severity level, affected stations, start time, and estimated end time
- **Planned Closure**: Represents scheduled maintenance or closures including closure description, affected stations, start date/time, end date/time, and alternative service information
- **Crowding Information**: Represents passenger volume indicators including crowding level (e.g., quiet, moderate, busy, very busy) and time of measurement

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can navigate from the tube status list to the line details screen and view complete line information in under 2 seconds
- **SC-002**: Shared element transitions complete smoothly with animation duration under 400 milliseconds
- **SC-003**: Header images display without visible pixelation on devices with up to 3x pixel density (1440px effective resolution)
- **SC-004**: Text overlaid on header images maintains a contrast ratio of at least 4.5:1 for readability (WCAG AA standard)
- **SC-005**: The screen successfully displays all available cached line information without requiring network connectivity
- **SC-006**: 95% of users successfully access detailed line information on their first attempt by tapping a line from the status screen

## Assumptions

- The tube status screen already fetches and caches comprehensive line data from the TFL API including all fields needed for the details screen
- **The Line Details Screen assumes the Tube Status Screen has successfully fetched and cached data at least once before the user navigates to Line Details. On first app launch with an empty database, the Line Details Screen will display an error state prompting the user to refresh the Tube Status Screen first.**
- The caching mechanism persists data reliably and makes it available to the line details screen
- Each tube line has a unique identifier that can be used to retrieve its cached data
- High-resolution station images for all tube lines are available or can be generated
- The Material Design component library is already integrated in the project
- The navigation framework supports shared element transitions between screens
- Devices running the app have sufficient storage for high-resolution images
- Users are familiar with the standard back navigation pattern to return to the tube status screen

## Dependencies

- Tube status screen must implement comprehensive data fetching and caching before this screen can function
- Local cache/database system must be implemented and populated with TFL API data
- High-resolution station images must be created or sourced for all tube lines
- Shared element transition support must be available in the navigation framework
- Material Design component library must be integrated

## Out of Scope

- Real-time updates to line information while viewing the details screen (users must return to status screen and re-enter to see updates)
- Interactive maps showing line routes or station locations
- Journey planning functionality from the line details screen
- Favorite or bookmark functionality for specific lines
- Push notifications for line status changes
- Sharing line status information with other users
- Accessibility features beyond standard text contrast requirements (will be addressed in a separate accessibility improvement feature)
