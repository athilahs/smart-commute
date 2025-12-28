# Research Document: Line Details Screen

**Feature**: 002-line-details-screen
**Date**: 2025-12-28
**Status**: Complete

## Purpose

This document captures research decisions and findings for implementing the Line Details Screen feature in the SmartCommute Android app. The research phase resolves technical unknowns identified during planning and documents best practices for the technologies used.

## Research Areas

### 1. Shared Element Transitions in Jetpack Compose

**Decision**: Use Compose's built-in Shared Element API (available in compose-animation 1.7.0+)

**Rationale**:
- Native support for shared element transitions in Navigation Compose
- Smoother animations with Material Motion specifications
- Better integration with Compose lifecycle
- Reduced complexity compared to custom implementations

**Implementation Approach**:
- Use `SharedTransitionLayout` as parent container
- Mark shared elements with `Modifier.sharedElement()` and unique keys
- Elements to animate: line icon, line name text, status text
- Animation duration: <400ms per specification (SC-002)

**Alternatives Considered**:
- Custom transition animations: Rejected - More complex, less maintainable
- Fragment transitions: Rejected - Not applicable in Compose-only app
- Third-party libraries (Accompanist): Rejected - Native API now available

**Resources**:
- [Jetpack Compose Shared Element Transitions Guide](https://developer.android.com/develop/ui/compose/animation/shared-elements)
- [Material Motion Design Guidelines](https://m3.material.io/styles/motion/overview)

---

### 2. Header Image Gradient Scrim Implementation

**Decision**: Use Compose's Brush.verticalGradient() with alpha transparency

**Rationale**:
- Built-in Compose API for gradient overlays
- Performant rendering on device GPU
- Easy to achieve 4.5:1 contrast ratio (WCAG AA per SC-004)
- Customizable gradient stops for optimal readability

**Implementation Details**:
```kotlin
Box {
    Image(/* header image */)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(/* bottom 1/3 of image */)
            .align(Alignment.BottomCenter)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
    )
    Text(/* line name and status */)
}
```

**Alternatives Considered**:
- Dynamic color adjustment based on image brightness: Rejected - Complex, requires image analysis, less predictable
- Solid background behind text only: Rejected - Less visually appealing, breaks Material Design aesthetic
- Blur effect: Rejected - Higher performance cost, inconsistent across devices

---

### 3. High-Resolution Station Images Strategy

**Decision**: Generate AI-assisted station images with blur/anonymization, stored as drawable resources

**Rationale**:
- No licensing issues with stock photos
- Privacy-compliant (no identifiable individuals per FR-011)
- Controlled file sizes for APK optimization
- High DPI support (1080px+ per FR-009)

**Image Specifications**:
- Format: WebP for compression efficiency
- Resolution: 1440x960px (3:2 aspect ratio, 3x density support)
- Quality: 85% compression
- Color space: sRGB
- Estimated size per image: 100-200KB
- Total for 11 tube lines: ~1.5-2MB

**Storage Location**: `app/src/main/res/drawable-nodpi/` (prevents automatic scaling)

**Naming Convention**:
- `line_header_bakerloo.webp`
- `line_header_central.webp`
- `line_header_circle.webp`
- etc. (11 total for London Underground lines)

**Fallback Strategy**: Generate placeholder images programmatically using line brand colors if specific images unavailable

**Alternatives Considered**:
- Remote image loading: Rejected - Violates offline-first requirement, requires network
- Stock photography: Rejected - Licensing costs, potential identifiable people
- Vector graphics: Rejected - Insufficient realism for "busy station" requirement

---

### 4. Expandable Text for Long Disruptions

**Decision**: Use AnimatedVisibility with state management for expand/collapse

**Rationale**:
- Native Compose animation API
- Smooth height transitions
- Maintains scroll position
- Accessible with TalkBack support

**Implementation Pattern**:
```kotlin
var isExpanded by remember { mutableStateOf(false) }

Column {
    Text(
        text = disruptionText,
        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
        overflow = TextOverflow.Ellipsis
    )

    AnimatedVisibility(visible = disruptionText.length > PREVIEW_LENGTH) {
        TextButton(onClick = { isExpanded = !isExpanded }) {
            Text(if (isExpanded) "Show less" else "Read more")
        }
    }
}
```

**Performance Considerations**:
- Text measurement happens during composition
- Recomposition limited to text expansion state changes
- Minimal overhead for short descriptions

**Alternatives Considered**:
- Modal bottom sheet for full text: Rejected - Disrupts reading flow, more taps required
- Horizontal scrollable text: Rejected - Poor UX, not Material Design compliant
- Truncation without expansion: Rejected - Hides critical information from users

---

### 5. Offline Data Access Pattern

**Decision**: Repository pattern with Room as single source of truth, no network calls in LineDetailsScreen

**Rationale**:
- Adheres to specification requirement (FR-007)
- Data already cached by Tube Status Screen
- Consistent with existing LineStatusRepositoryImpl pattern
- Instant screen load (<2 seconds per SC-001)

**Data Flow**:
1. LineDetailsScreen receives `lineId` from navigation arguments
2. ViewModel queries repository for cached line data
3. Repository reads from Room database via DAO
4. Data exposed as StateFlow to UI
5. Screen displays all available cached fields

**Required Database Extensions**:
- Add detailed line information fields to existing `LineStatusEntity`
- Or create new `LineDetailsEntity` with foreign key to line
- Include: disruptions list, closures list, crowding data, alternative service info

**Cache Invalidation**: Handled by Tube Status Screen on refresh (not this screen's responsibility)

**Alternatives Considered**:
- Direct DAO access from ViewModel: Rejected - Violates repository pattern, reduces testability
- Passing all data as navigation arguments: Rejected - Complex serialization, size limits
- Shared ViewModel between screens: Rejected - Tight coupling, harder to maintain

---

### 6. Material Design 3 Guidelines for Detail Screens

**Decision**: Follow M3 "Detail screen" pattern with hero image and scrollable content

**Rationale**:
- Aligns with specification requirements
- Standard pattern for content-rich detail views
- Native support in Material3 Compose components
- Accessibility-friendly with proper content structure

**Layout Structure**:
```
- Scaffold with TopAppBar (back navigation)
- ScrollableColumn (or LazyColumn)
  - Box (Hero header)
    - Image (station photo)
    - Gradient scrim
    - Icon (bottom-right)
    - Text (line name, status) over scrim
  - Card sections (body content)
    - Service Status Summary
    - Current Disruptions (expandable)
    - Planned Closures (expandable)
    - Crowding Information
    - Alternative Service Info
```

**Component Choices**:
- `Card` with `elevation` for content sections
- `ListItem` for structured information rows
- `Icon` + `Text` combinations for status indicators
- `Divider` for visual separation
- `TextButton` for expandable content triggers

**Spacing & Typography**:
- Follow Material3 token system (`MaterialTheme.spacing`, `MaterialTheme.typography`)
- 16dp horizontal padding for content
- 8dp vertical spacing between cards
- 24dp spacing around hero image content

**Resources**:
- [Material Design 3 Components](https://m3.material.io/components)
- [Android Compose Material3 Documentation](https://developer.android.com/develop/ui/compose/designsystems/material3)

---

### 7. Aspect Ratio Handling for Non-Standard Devices

**Decision**: Use relative height calculation (maxHeight * 0.33f) instead of fixed fraction

**Rationale**:
- Adapts to all screen sizes and aspect ratios
- Works on foldables, tablets, and phones
- Maintains "approximately 1/3" requirement from specification
- Prevents layout issues on extreme aspect ratios

**Implementation**:
```kotlin
BoxWithConstraints {
    val imageHeight = maxHeight * 0.33f

    Image(
        modifier = Modifier
            .fillMaxWidth()
            .height(imageHeight)
    )
}
```

**Edge Case Handling**:
- Very tall screens (21:9+): Image still looks proportional
- Very wide screens (tablets landscape): 1/3 height remains reasonable
- Small screens: Minimum height enforced (120.dp) to ensure readability

**Alternatives Considered**:
- Fixed height (e.g., 300.dp): Rejected - Doesn't scale across devices
- fillMaxWidth().aspectRatio(3f): Rejected - Doesn't satisfy "1/3 of screen height" spec
- Different layouts per screen size: Rejected - Over-engineering, YAGNI violation

---

### 8. Android Lifecycle Considerations

**Decision**: Implement proper ViewModel state preservation and lifecycle awareness

**Required Implementations**:
- ViewModel survives configuration changes (rotation, multi-window)
- No network calls means no cancellation handling needed
- Image loading with Glide handles lifecycle automatically
- StateFlow collection in LaunchedEffect with lifecycle awareness

**State Preservation**:
```kotlin
@HiltViewModel
class LineDetailsViewModel @Inject constructor(
    private val repository: LineStatusRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val lineId: String = checkNotNull(savedStateHandle["lineId"])

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        loadLineDetails()
    }

    private fun loadLineDetails() {
        viewModelScope.launch {
            repository.getLineDetails(lineId).collect { result ->
                _state.value = when (result) {
                    is NetworkResult.Loading -> UiState.Loading
                    is NetworkResult.Success -> UiState.Success(result.data)
                    is NetworkResult.Error -> UiState.Error(result.message)
                }
            }
        }
    }
}
```

**Lifecycle Best Practices Applied**:
- Use `viewModelScope` for coroutines (auto-cancellation)
- StateFlow for UI state (survives configuration changes)
- No Activity/Fragment references in ViewModel
- Glide integration uses `LocalContext` in Composable (lifecycle-aware)

---

## Summary of Key Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Jetpack Compose | BOM 2025.12.01 | UI framework |
| Material Design 3 | Latest (BOM) | Design system |
| Compose Navigation | 2.9.6 | Screen navigation |
| Shared Element API | compose-animation | Element transitions |
| Room | 2.8.4 | Local data cache |
| Hilt | 2.57.2 | Dependency injection |
| Glide Compose | 1.0.0-beta08 | Image loading |
| Kotlin Coroutines | 1.10.2 | Async operations |
| StateFlow | stdlib | State management |

---

## Outstanding Questions

**None** - All technical unknowns have been resolved through research and best practices analysis. The feature can proceed to design and implementation phases.

---

## Implementation Readiness

✅ **Technology Stack**: Fully defined
✅ **Architecture Pattern**: MVVM with offline-first repository
✅ **UI Framework**: Jetpack Compose with Material3
✅ **Animation Approach**: Shared Element Transitions
✅ **Data Access**: Room database (existing infrastructure)
✅ **Image Strategy**: WebP drawables with fallback generation
✅ **Lifecycle Handling**: ViewModel + StateFlow pattern

**Status**: Ready for Phase 1 (Design & Contracts)
