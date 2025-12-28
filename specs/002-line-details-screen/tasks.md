# Line Details Screen - Implementation Tasks

**Feature**: 002-line-details-screen
**Status**: Planning
**Total Tasks**: 25

---

## Phase 1: Data Layer Enhancement (6 tasks)

### T053: Update UndergroundLine domain model ⏳
**Estimate**: 15 min
**Description**: Add optional fields for detailed information
**Dependencies**: None
**Files**:
- `app/src/main/java/com/smartcommute/feature/linestatus/domain/model/UndergroundLine.kt`

**Changes**:
- Add `serviceType: String?` field
- Add `disruption: Disruption?` field
- Add `routeInfo: RouteInfo?` field

---

### T054: Create Disruption data class ⏳
**Estimate**: 10 min
**Description**: Model for disruption information
**Dependencies**: None
**Files**:
- `app/src/main/java/com/smartcommute/feature/linestatus/domain/model/Disruption.kt`

**Implementation**:
```kotlin
data class Disruption(
    val category: String,
    val categoryDescription: String,
    val description: String,
    val affectedStops: List<String> = emptyList(),
    val closureText: String? = null
)
```

---

### T055: Create RouteInfo data class ⏳
**Estimate**: 5 min
**Description**: Model for route information
**Dependencies**: None
**Files**:
- `app/src/main/java/com/smartcommute/feature/linestatus/domain/model/RouteInfo.kt`

**Implementation**:
```kotlin
data class RouteInfo(
    val routeSectionName: String,
    val direction: String? = null
)
```

---

### T056: Update LineStatusDto ⏳
**Estimate**: 15 min
**Description**: Capture additional fields from TfL API
**Dependencies**: T054, T055
**Files**:
- `app/src/main/java/com/smartcommute/feature/linestatus/data/remote/dto/LineStatusDto.kt`

**Changes**:
- Add `serviceTypes` field
- Update `LineStatusResponseDto` with `disruption` object

---

### T057: Update LineStatusEntity ⏳
**Estimate**: 15 min
**Description**: Store additional fields in Room database
**Dependencies**: T053
**Files**:
- `app/src/main/java/com/smartcommute/feature/linestatus/data/local/entity/LineStatusEntity.kt`

**Changes**:
- Add fields for service type
- Add fields for disruption info (JSON or flattened)
- Update database version

---

### T058: Update mapper extension functions ⏳
**Estimate**: 20 min
**Description**: Map new fields between layers
**Dependencies**: T053-T057
**Files**:
- `app/src/main/java/com/smartcommute/feature/linestatus/data/remote/mapper/LineStatusMapper.kt`

**Changes**:
- Update `LineStatusDto.toDomain()` to extract new fields
- Update `UndergroundLine.toEntity()` to store new fields
- Update `LineStatusEntity.toDomain()` to restore new fields

---

## Phase 2: Image Assets (2 tasks)

### T059: Generate station hero images ⏳
**Estimate**: 60 min
**Description**: Create high-quality station images for each line
**Dependencies**: None

**Requirements**:
- Resolution: Minimum 1080px width
- Format: JPEG (WebP optional)
- Content: Busy station scenes
- Quality: High (suitable for hero images)

**Images needed**:
1. Bakerloo - Paddington Station
2. Central - Liverpool Street Station
3. Circle - King's Cross Station
4. District - Westminster Station
5. Hammersmith & City - Baker Street Station
6. Jubilee - Canary Wharf Station
7. Metropolitan - Harrow-on-the-Hill Station
8. Northern - Bank Station
9. Piccadilly - Piccadilly Circus Station
10. Victoria - Victoria Station
11. Waterloo & City - Waterloo Station

---

### T060: Add image resources ⏳
**Estimate**: 10 min
**Description**: Add images to drawable folders
**Dependencies**: T059
**Files**:
- `app/src/main/res/drawable-nodpi/hero_bakerloo.jpg`
- `app/src/main/res/drawable-nodpi/hero_central.jpg`
- (... one for each line)

---

## Phase 3: Navigation Setup (3 tasks)

### T061: Add LineDetails navigation route ⏳
**Estimate**: 10 min
**Description**: Define navigation route for line details
**Dependencies**: None
**Files**:
- `app/src/main/java/com/smartcommute/core/navigation/NavigationScreen.kt`

**Implementation**:
```kotlin
@Serializable
data class LineDetails(val lineId: String) : NavigationScreen("line_details/{lineId}")
```

---

### T062: Update AppNavigation ⏳
**Estimate**: 15 min
**Description**: Add line details route to navigation graph
**Dependencies**: T061
**Files**:
- `app/src/main/java/com/smartcommute/core/navigation/AppNavigation.kt`

**Implementation**:
```kotlin
composable<LineDetails> { backStackEntry ->
    val lineDetails: LineDetails = backStackEntry.toRoute()
    LineDetailsScreen(lineId = lineDetails.lineId)
}
```

---

### T063: Add click handler to LineStatusItem ⏳
**Estimate**: 10 min
**Description**: Navigate to details on item click
**Dependencies**: T061, T062
**Files**:
- `app/src/main/java/com/smartcommute/feature/linestatus/ui/components/LineStatusItem.kt`

**Changes**:
- Add `onClick` parameter
- Wrap Card in `clickable` modifier
- Add shared element transition IDs

---

## Phase 4: UI Components (8 tasks)

### T064: Create LineDetailsViewModel ⏳
**Estimate**: 30 min
**Description**: ViewModel for retrieving cached line data
**Dependencies**: T053-T058
**Files**:
- `app/src/main/java/com/smartcommute/feature/linedetails/ui/LineDetailsViewModel.kt`

**Implementation**:
- Read line data from Room DB by lineId
- Expose as StateFlow
- No network requests

---

### T065: Create HeroImageSection ⏳
**Estimate**: 30 min
**Description**: Hero image with gradient overlay
**Dependencies**: T060
**Files**:
- `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/HeroImageSection.kt`

**Features**:
- Display hero image based on lineId
- Bottom gradient overlay (black 80% → transparent)
- Proper aspect ratio (1:3 height)

---

### T066: Create StatusSection ⏳
**Estimate**: 20 min
**Description**: Status badge and full description
**Dependencies**: None
**Files**:
- `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/StatusSection.kt`

**Features**:
- Status badge with color
- Full status description (not truncated)
- Material card layout

---

### T067: Create KeyInfoSection ⏳
**Estimate**: 20 min
**Description**: Key information about the line
**Dependencies**: None
**Files**:
- `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/KeyInfoSection.kt`

**Features**:
- Display mode name
- Display service type
- Material list layout

---

### T068: Create DisruptionsSection ⏳
**Estimate**: 30 min
**Description**: Disruption details or all-clear message
**Dependencies**: T054
**Files**:
- `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/DisruptionsSection.kt`

**Features**:
- Show disruption info if exists
- Show "All clear!" if no disruptions
- Display closure text if available
- Material card layout

---

### T069: Implement shared element transition - Icon ⏳
**Estimate**: 30 min
**Description**: Animate line icon from list to details
**Dependencies**: T063, T065
**Files**:
- `LineStatusItem.kt`
- `HeroImageSection.kt`

**Implementation**:
- Add `sharedTransitionKey` to icon in list item
- Add matching key in hero section
- Transition name: `line_icon_{lineId}`

---

### T070: Implement shared element transition - Name ⏳
**Estimate**: 20 min
**Description**: Animate line name from list to details
**Dependencies**: T063, T065
**Files**:
- `LineStatusItem.kt`
- `HeroImageSection.kt`

**Implementation**:
- Add `sharedTransitionKey` to name text
- Add matching key in hero section
- Transition name: `line_name_{lineId}`

---

### T071: Implement shared element transition - Status ⏳
**Estimate**: 20 min
**Description**: Animate status text from list to details
**Dependencies**: T063, T065
**Files**:
- `LineStatusItem.kt`
- `HeroImageSection.kt`

**Implementation**:
- Add `sharedTransitionKey` to status text
- Add matching key in hero section
- Transition name: `line_status_{lineId}`

---

## Phase 5: Main Screen Assembly (3 tasks)

### T072: Create LineDetailsScreen ⏳
**Estimate**: 40 min
**Description**: Main screen composable
**Dependencies**: T064-T068
**Files**:
- `app/src/main/java/com/smartcommute/feature/linedetails/ui/LineDetailsScreen.kt`

**Features**:
- Scaffold with back button
- Hero section at top
- Scrollable content body
- Proper padding and spacing

---

### T073: Add gradient overlay to hero ⏳
**Estimate**: 15 min
**Description**: Ensure text readability
**Dependencies**: T065
**Files**:
- `HeroImageSection.kt`

**Implementation**:
- Vertical gradient from black 80% to transparent
- Height: 40% of image height
- Applied via Box overlay

---

### T074: Test transitions and layouts ⏳
**Estimate**: 30 min
**Description**: Manual testing
**Dependencies**: T069-T073

**Testing**:
- Test all 11 lines
- Verify transitions are smooth
- Test on different screen sizes
- Verify text readability

---

## Phase 6: Polish (3 tasks)

### T075: Add string resources ⏳
**Estimate**: 15 min
**Description**: Localize all text
**Dependencies**: T072
**Files**:
- `app/src/main/res/values/strings.xml`

**Strings needed**:
- Screen title format
- Section headers
- Empty state messages
- Content descriptions

---

### T076: Add accessibility ⏳
**Estimate**: 20 min
**Description**: Content descriptions and TalkBack support
**Dependencies**: T072
**Files**:
- All component files

**Requirements**:
- Hero image content description
- Section headers announced
- Interactive elements accessible

---

### T077: Verify Material Design compliance ⏳
**Estimate**: 20 min
**Description**: Design review
**Dependencies**: T072

**Checklist**:
- Proper elevation and shadows
- Correct spacing (8dp grid)
- Typography hierarchy
- Color contrast (WCAG AA)

---

## Summary

**Total Estimated Time**: ~8-9 hours

**Phase Breakdown**:
- Phase 1 (Data): 80 min
- Phase 2 (Images): 70 min
- Phase 3 (Navigation): 35 min
- Phase 4 (UI): 200 min
- Phase 5 (Assembly): 85 min
- Phase 6 (Polish): 55 min

**Critical Path**:
T053-T058 → T064 → T065-T068 → T072 → T069-T071 → T073-T074

**Parallel Work Possible**:
- T059-T060 can be done independently
- T061-T063 can start early
- T075-T077 can be done at the end
