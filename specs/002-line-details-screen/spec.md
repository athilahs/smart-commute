# Feature Specification: Line Details Screen

**Feature ID**: 002-line-details-screen
**Status**: Planning
**Created**: 2025-12-28
**Dependencies**: 001-tube-status-screen

---

## Overview

A detailed view screen that displays comprehensive information about a specific London Underground line when a user taps on a line item from the Tube Status screen. The screen features a hero image, shared element transitions, and detailed line information from cached TfL API data.

---

## User Stories

### User Story 1: View Line Details
**As a** commuter
**I want to** see detailed information about a specific tube line
**So that** I can make informed decisions about my journey

**Acceptance Criteria**:
- Tapping a line item on the Tube Status screen navigates to Line Details screen
- Line icon, name, and status animate smoothly using shared element transitions
- Hero image shows a relevant station from that line
- All information is loaded from cache (no network requests)
- Screen follows Material Design guidelines
- Text is readable over the hero image with proper contrast

### User Story 2: View Service Disruptions
**As a** commuter
**I want to** see detailed disruption information
**So that** I can understand why there are delays and plan accordingly

**Acceptance Criteria**:
- Full status description is visible (not truncated like in list)
- Disruption reasons are clearly displayed
- Planned closures/works are shown if available
- Status severity is visually indicated

---

## Design Specifications

### Layout Structure

```
┌─────────────────────────────────┐
│                                 │
│    HERO IMAGE (1/3 height)     │ ← Station photo
│                                 │
│  ┌──────────────────────┐      │
│  │ Central              │      │ ← Line name + status
│  │ Minor Delays      [🔴]│     │ ← Line icon (animated)
│  └──────────────────────┘      │
├─────────────────────────────────┤
│                                 │
│  📊 Service Status              │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│  [Status badge]                 │
│  Full description text...       │
│                                 │
│  📍 Key Information             │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│  • Mode: Underground            │
│  • Service Type: Regular        │
│                                 │
│  ⚠️ Current Disruptions         │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━    │
│  [If delays] Full reason...     │
│  [If none] All clear!           │
│                                 │
└─────────────────────────────────┘
```

### Hero Image Requirements

**Dimensions**: Full width × 33% screen height
**Content**: Busy station scene from the specific line
**Quality**: High resolution (at least 1080px width)
**Format**: WebP for smaller file sizes, JPEG fallback
**Overlay**: Bottom gradient overlay for text readability

**Image Assignments**:
- **Bakerloo**: Paddington Station
- **Central**: Liverpool Street Station
- **Circle**: King's Cross Station
- **District**: Westminster Station
- **Hammersmith & City**: Baker Street Station
- **Jubilee**: Canary Wharf Station
- **Metropolitan**: Harrow-on-the-Hill Station
- **Northern**: Bank Station
- **Piccadilly**: Piccadilly Circus Station
- **Victoria**: Victoria Station
- **Waterloo & City**: Waterloo Station

### Shared Element Transitions

**Animated Elements**:
1. **Line Icon** (roundel)
   - Transition name: `line_icon_{lineId}`
   - Animation: Position + scale

2. **Line Name**
   - Transition name: `line_name_{lineId}`
   - Animation: Position + text style

3. **Status Text**
   - Transition name: `line_status_{lineId}`
   - Animation: Position + text style

**Animation Duration**: 300ms
**Easing**: Material Standard (cubic-bezier)

### Text Readability

**Bottom Gradient Overlay**:
- Type: Linear gradient
- Direction: Bottom to top
- Colors:
  - Bottom: Black @ 80% opacity
  - Top: Transparent @ 0% opacity
- Height: 40% of hero image height

**Text Styling on Hero**:
- Line name: White, titleLarge, bold, shadow
- Status: White, bodyMedium, shadow
- Drop shadow: 0dp 2dp 4dp rgba(0,0,0,0.5)

---

## Technical Architecture

### Navigation

**Route**: `line_details/{lineId}`
**Parameters**:
- `lineId`: String (e.g., "central", "victoria")

**Navigation Args**:
```kotlin
@Serializable
data class LineDetails(val lineId: String)
```

### Data Flow

```
┌──────────────────────┐
│  TubeStatusScreen    │
│  (cached data)       │
└──────────┬───────────┘
           │ User taps line item
           ▼
┌──────────────────────┐
│ LineDetailsScreen    │
│ • Reads from Room DB │ ← NO network requests
│ • Displays cached    │
│   data only          │
└──────────────────────┘
```

### Data Model Enhancement

Update `UndergroundLine` domain model to include additional fields from TfL API:

```kotlin
data class UndergroundLine(
    val id: String,
    val name: String,
    val modeName: String,
    val status: ServiceStatus,

    // New fields for details screen:
    val serviceType: String? = null,           // "Regular", "Night"
    val disruption: Disruption? = null,        // Detailed disruption info
    val routeInfo: RouteInfo? = null           // Route details
)

data class Disruption(
    val category: String,                      // "RealTime", "PlannedWork"
    val categoryDescription: String,
    val description: String,
    val affectedStops: List<String> = emptyList(),
    val closureText: String? = null
)

data class RouteInfo(
    val routeSectionName: String,
    val direction: String? = null
)
```

### File Structure

```
app/src/main/
├── java/com/smartcommute/
│   └── feature/
│       └── linedetails/
│           ├── domain/
│           │   └── model/
│           │       ├── Disruption.kt
│           │       └── RouteInfo.kt
│           └── ui/
│               ├── LineDetailsScreen.kt
│               ├── LineDetailsViewModel.kt
│               └── components/
│                   ├── HeroImageSection.kt
│                   ├── StatusSection.kt
│                   ├── KeyInfoSection.kt
│                   └── DisruptionsSection.kt
└── res/
    └── drawable/
        ├── hero_bakerloo.jpg
        ├── hero_central.jpg
        ├── hero_circle.jpg
        ├── hero_district.jpg
        ├── hero_hammersmith_city.jpg
        ├── hero_jubilee.jpg
        ├── hero_metropolitan.jpg
        ├── hero_northern.jpg
        ├── hero_piccadilly.jpg
        ├── hero_victoria.jpg
        └── hero_waterloo_city.jpg
```

---

## Implementation Plan

### Phase 1: Data Layer Enhancement (T053-T058)

- **T053**: Update `UndergroundLine` domain model with new fields
- **T054**: Update `LineStatusDto` to capture additional TfL API fields
- **T055**: Update `LineStatusEntity` for Room database
- **T056**: Update mapper extension functions
- **T057**: Create `Disruption` data class
- **T058**: Create `RouteInfo` data class

### Phase 2: Image Assets (T059-T060)

- **T059**: Generate high-resolution station images (1080px+)
- **T060**: Add image resources to drawable folders

### Phase 3: Navigation Setup (T061-T063)

- **T061**: Add `LineDetails` navigation route to `NavigationScreen`
- **T062**: Update `AppNavigation` with line details route
- **T063**: Add navigation call from `LineStatusItem` click handler

### Phase 4: UI Components (T064-T071)

- **T064**: Create `LineDetailsViewModel` with cached data retrieval
- **T065**: Create `HeroImageSection` composable with gradient overlay
- **T066**: Create `StatusSection` composable
- **T067**: Create `KeyInfoSection` composable
- **T068**: Create `DisruptionsSection` composable
- **T069**: Implement shared element transition for line icon
- **T070**: Implement shared element transition for line name
- **T071**: Implement shared element transition for status text

### Phase 5: Main Screen Assembly (T072-T074)

- **T072**: Create `LineDetailsScreen` composable
- **T073**: Add proper gradient overlay to hero image
- **T074**: Test all transitions and layouts

### Phase 6: Polish (T075-T077)

- **T075**: Add string resources for all text
- **T076**: Add content descriptions for accessibility
- **T077**: Verify Material Design compliance

---

## TfL API Response Enhancement

Currently we're only capturing basic status. We need to extract:

### From `lineStatuses` array:
```json
{
  "statusSeverity": 10,
  "statusSeverityDescription": "Good Service",
  "reason": "Minor delays due to...",
  "disruption": {
    "category": "RealTime",
    "categoryDescription": "Real Time",
    "description": "Piccadilly Line: Minor delays...",
    "closureText": "..."
  }
}
```

### From root level:
```json
{
  "serviceTypes": [
    {
      "name": "Regular",
      "uri": "/Line/ServiceTypes/Regular"
    }
  ],
  "routeSections": [...]
}
```

---

## Success Criteria

### Functional Requirements
- [ ] Line details screen displays for all 11 Underground lines
- [ ] No network requests made on details screen
- [ ] All data loaded from Room database cache
- [ ] Hero images display at high quality on all devices
- [ ] Text is readable over hero images
- [ ] Shared element transitions are smooth
- [ ] Back navigation returns to status screen with reverse animation

### Performance Requirements
- [ ] Screen loads within 200ms (data from cache)
- [ ] Shared element transition completes within 300ms
- [ ] Hero images load without jank
- [ ] 60fps maintained during transitions

### Accessibility Requirements
- [ ] All images have content descriptions
- [ ] Text has sufficient contrast (WCAG AA)
- [ ] TalkBack announces screen title and status
- [ ] All interactive elements are accessible

### Design Requirements
- [ ] Follows Material Design 3 guidelines
- [ ] Consistent with app's visual language
- [ ] Proper spacing and typography
- [ ] Responsive on different screen sizes

---

## Future Enhancements

Not in scope for this feature, but possible future additions:

1. **Live Updates**: Real-time status updates while viewing details
2. **Station List**: Show all stations on the line
3. **Fare Information**: Display fare zones
4. **Journey Planning**: "Plan journey from here" button
5. **Favorites**: Star/favorite specific lines
6. **Notifications**: Alert me about this line's status

---

## Risk Assessment

**Medium Risk**: Image file sizes
- **Mitigation**: Use WebP format, optimize images, lazy load if needed

**Low Risk**: Animation performance on low-end devices
- **Mitigation**: Test on various devices, provide motion-reduced alternative

**Low Risk**: TfL API schema changes
- **Mitigation**: Graceful fallbacks for missing data fields

---

## Testing Strategy

### Manual Testing
- Tap each line from status screen
- Verify correct hero image displays
- Verify transitions are smooth
- Test on various screen sizes
- Test with TalkBack enabled
- Test back navigation

### Unit Tests
- ViewModel data retrieval from cache
- Mapper functions with extended data
- Navigation parameter handling

### Integration Tests
- End-to-end navigation flow
- Shared element transitions
- Cache data persistence

---

## Acceptance Sign-off

- [ ] Product Owner approval
- [ ] Design review completed
- [ ] Technical review completed
- [ ] All acceptance criteria met
- [ ] Manual testing completed
- [ ] Ready for production
