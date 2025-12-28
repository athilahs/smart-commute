# Quickstart Guide: Line Details Screen

**Feature**: 002-line-details-screen
**For**: Developers implementing the Line Details Screen
**Date**: 2025-12-28

## Overview

This guide provides a step-by-step walkthrough for implementing the Line Details Screen feature in the SmartCommute Android app. Follow these steps in order to ensure proper integration with existing infrastructure.

---

## Prerequisites

Before starting implementation:

- [ ] Spec.md reviewed and understood
- [ ] Research.md reviewed for technical decisions
- [ ] Data-model.md reviewed for entity definitions
- [ ] Contracts reviewed for interface requirements
- [ ] Android Studio project opened and syncing
- [ ] Existing Tube Status Screen functionality understood

---

## Step 1: Database Schema Migration (30 minutes)

### 1.1 Update Database Version

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/data/local/LineStatusDatabase.kt`

```kotlin
@Database(
    entities = [
        LineStatusEntity::class,
        LineDetailsEntity::class,      // NEW
        DisruptionEntity::class,        // NEW
        ClosureEntity::class,           // NEW
        CrowdingEntity::class          // NEW
    ],
    version = 2,  // CHANGED from 1
    exportSchema = false
)
abstract class LineStatusDatabase : RoomDatabase() {
    abstract fun lineStatusDao(): LineStatusDao
    abstract fun lineDetailsDao(): LineDetailsDao  // NEW
}
```

### 1.2 Create New Entities

**Location**: `app/src/main/java/com/smartcommute/feature/linedetails/data/local/entity/`

Create 4 new entity files (see data-model.md for full code):
- `LineDetailsEntity.kt`
- `DisruptionEntity.kt`
- `ClosureEntity.kt`
- `CrowdingEntity.kt`

### 1.3 Create Migration

**File**: `app/src/main/java/com/smartcommute/core/di/DatabaseModule.kt`

Add migration constant and update Room.databaseBuilder():

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // See contracts/repository-contract.md for full SQL
    }
}

@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): LineStatusDatabase {
    return Room.databaseBuilder(
        context,
        LineStatusDatabase::class.java,
        "line_status_db"
    )
    .addMigrations(MIGRATION_1_2)  // ADD THIS
    .fallbackToDestructiveMigration()
    .build()
}
```

**Test**: Run app, verify no crashes, check database version in Device File Explorer

---

## Step 2: Create DAO (20 minutes)

### 2.1 Create LineDetailsDao Interface

**File**: `app/src/main/java/com/smartcommute/feature/linedetails/data/local/dao/LineDetailsDao.kt`

```kotlin
@Dao
interface LineDetailsDao {
    @Transaction
    suspend fun getCompleteLineDetails(lineId: String): CompleteLineDetails? {
        val details = getLineDetails(lineId) ?: return null
        return CompleteLineDetails(
            details = details,
            disruptions = getDisruptions(lineId),
            closures = getClosures(lineId),
            crowding = getCrowding(lineId)
        )
    }

    @Query("SELECT * FROM line_details WHERE lineId = :lineId")
    suspend fun getLineDetails(lineId: String): LineDetailsEntity?

    @Query("SELECT * FROM disruptions WHERE lineId = :lineId ORDER BY severity DESC")
    suspend fun getDisruptions(lineId: String): List<DisruptionEntity>

    @Query("SELECT * FROM closures WHERE lineId = :lineId ORDER BY startDate ASC")
    suspend fun getClosures(lineId: String): List<ClosureEntity>

    @Query("SELECT * FROM crowding WHERE lineId = :lineId")
    suspend fun getCrowding(lineId: String): CrowdingEntity?

    // Write operations for Tube Status Screen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineDetails(lineDetails: LineDetailsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisruptions(disruptions: List<DisruptionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosures(closures: List<ClosureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrowding(crowding: CrowdingEntity)
}

data class CompleteLineDetails(
    val details: LineDetailsEntity,
    val disruptions: List<DisruptionEntity>,
    val closures: List<ClosureEntity>,
    val crowding: CrowdingEntity?
)
```

### 2.2 Update Database Class

Add DAO abstract method to `LineStatusDatabase.kt`:

```kotlin
abstract fun lineDetailsDao(): LineDetailsDao
```

**Test**: Build project, verify no compilation errors

---

## Step 3: Create Domain Models (30 minutes)

### 3.1 Create Domain Model Classes

**Location**: `app/src/main/java/com/smartcommute/feature/linedetails/domain/model/`

Create 5 files (see data-model.md for full code):
- `UndergroundLineDetails.kt` - Main domain model
- `Disruption.kt` - Disruption data class
- `Closure.kt` - Closure data class
- `Crowding.kt` - Crowding data class + enum
- `StatusSeverity.kt` - Status enum (if not already exists)

**Test**: Build project, verify domain models compile

---

## Step 4: Create Data Mappers (45 minutes)

### 4.1 Create Mapper Class

**File**: `app/src/main/java/com/smartcommute/feature/linedetails/data/mapper/LineDetailsMapper.kt`

```kotlin
class LineDetailsMapper @Inject constructor() {
    fun toDomain(completeDetails: CompleteLineDetails): UndergroundLineDetails {
        // See contracts/repository-contract.md for full implementation
    }

    private fun DisruptionEntity.toDomain(): Disruption {
        // Parse JSON, convert timestamps, map severity
    }

    private fun ClosureEntity.toDomain(): Closure {
        // Parse JSON, convert timestamps, handle optionals
    }

    private fun CrowdingEntity.toDomain(): Crowding {
        // Map level code to enum
    }

    private fun parseHexColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: IllegalArgumentException) {
            Color.Gray
        }
    }

    private fun parseJsonArray(json: String): List<String> {
        return try {
            Json.decodeFromString<List<String>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
```

**Test**: Write sample mapper test or manually verify with sample data

---

## Step 5: Create Repository (30 minutes)

### 5.1 Create Repository Interface

**File**: `app/src/main/java/com/smartcommute/feature/linedetails/domain/repository/LineDetailsRepository.kt`

```kotlin
interface LineDetailsRepository {
    fun getLineDetails(lineId: String): Flow<NetworkResult<UndergroundLineDetails>>
}
```

### 5.2 Create Repository Implementation

**File**: `app/src/main/java/com/smartcommute/feature/linedetails/data/repository/LineDetailsRepositoryImpl.kt`

```kotlin
@Singleton
class LineDetailsRepositoryImpl @Inject constructor(
    private val dao: LineDetailsDao,
    private val mapper: LineDetailsMapper
) : LineDetailsRepository {

    override fun getLineDetails(lineId: String): Flow<NetworkResult<UndergroundLineDetails>> = flow {
        emit(NetworkResult.Loading)

        try {
            val completeDetails = dao.getCompleteLineDetails(lineId)

            if (completeDetails != null) {
                val domainModel = mapper.toDomain(completeDetails)
                emit(NetworkResult.Success(data = domainModel, isFromCache = true))
            } else {
                emit(NetworkResult.Error(
                    message = "Line details not available offline. Please refresh from tube status screen."
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(
                message = "Unable to load line details: ${e.localizedMessage}",
                exception = e
            ))
        }
    }.flowOn(Dispatchers.IO)
}
```

### 5.3 Create Hilt Module

**File**: `app/src/main/java/com/smartcommute/core/di/LineDetailsModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class LineDetailsModule {

    @Binds
    @Singleton
    abstract fun bindLineDetailsRepository(
        impl: LineDetailsRepositoryImpl
    ): LineDetailsRepository
}
```

**Test**: Build project, verify Hilt generates code without errors

---

## Step 6: Add High-Resolution Images (20 minutes)

### 6.1 Generate/Obtain Station Images

Create 11 station images (1440x960px WebP format) for London Underground lines:
- Bakerloo, Central, Circle, District, Elizabeth, Hammersmith & City
- Jubilee, Metropolitan, Northern, Piccadilly, Victoria, Waterloo & City

**Privacy**: Ensure people in images are blurred/anonymized (per FR-011)

### 6.2 Add to Resources

**Location**: `app/src/main/res/drawable-nodpi/`

**Naming**:
- `line_header_bakerloo.webp`
- `line_header_central.webp`
- etc.

### 6.3 Create Placeholder Generator (Optional)

**File**: `app/src/main/java/com/smartcommute/feature/linedetails/ui/util/PlaceholderImageGenerator.kt`

```kotlin
object PlaceholderImageGenerator {
    fun getHeaderImageResource(lineId: String, brandColor: String): String {
        val resourceName = "line_header_${lineId.lowercase()}"
        // Check if resource exists, otherwise return "placeholder"
        return resourceName
    }
}
```

**Test**: Verify images are visible in Android Studio resource preview

---

## Step 7: Create ViewModel (30 minutes)

### 7.1 Define UI State

**File**: `app/src/main/java/com/smartcommute/feature/linedetails/ui/LineDetailsUiState.kt`

```kotlin
sealed interface LineDetailsUiState {
    data object Loading : LineDetailsUiState

    data class Success(
        val lineDetails: UndergroundLineDetails,
        val expandedDisruptions: Set<Long> = emptySet(),
        val expandedClosures: Set<Long> = emptySet()
    ) : LineDetailsUiState

    data class Error(
        val message: String
    ) : LineDetailsUiState
}
```

### 7.2 Create ViewModel

**File**: `app/src/main/java/com/smartcommute/feature/linedetails/ui/LineDetailsViewModel.kt`

```kotlin
@HiltViewModel
class LineDetailsViewModel @Inject constructor(
    private val repository: LineDetailsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lineId: String = checkNotNull(savedStateHandle["lineId"])

    private val _uiState = MutableStateFlow<LineDetailsUiState>(LineDetailsUiState.Loading)
    val uiState: StateFlow<LineDetailsUiState> = _uiState.asStateFlow()

    init {
        loadLineDetails()
    }

    private fun loadLineDetails() {
        viewModelScope.launch {
            repository.getLineDetails(lineId).collect { result ->
                _uiState.value = when (result) {
                    is NetworkResult.Loading -> LineDetailsUiState.Loading
                    is NetworkResult.Success -> LineDetailsUiState.Success(result.data)
                    is NetworkResult.Error -> LineDetailsUiState.Error(result.message)
                }
            }
        }
    }

    fun toggleDisruptionExpansion(disruptionId: Long) {
        val currentState = _uiState.value
        if (currentState is LineDetailsUiState.Success) {
            val expanded = currentState.expandedDisruptions.toMutableSet()
            if (disruptionId in expanded) {
                expanded.remove(disruptionId)
            } else {
                expanded.add(disruptionId)
            }
            _uiState.value = currentState.copy(expandedDisruptions = expanded)
        }
    }

    fun toggleClosureExpansion(closureId: Long) {
        // Similar to toggleDisruptionExpansion
    }
}
```

**Test**: Build project, verify ViewModel compiles

---

## Step 8: Update Navigation (20 minutes)

### 8.1 Add Navigation Route

**File**: `app/src/main/java/com/smartcommute/core/navigation/NavigationScreen.kt`

```kotlin
@Serializable
sealed class NavigationScreen {
    @Serializable
    data object LineStatus : NavigationScreen()

    @Serializable
    data class LineDetails(val lineId: String) : NavigationScreen()  // NEW

    // ... other screens
}
```

### 8.2 Add to Navigation Graph

**File**: `app/src/main/java/com/smartcommute/core/navigation/AppNavigation.kt`

```kotlin
fun NavGraphBuilder.appNavigation() {
    composable<NavigationScreen.LineStatus> {
        LineStatusScreen(
            onLineClick = { lineId ->
                // Navigate to details
                navController.navigate(NavigationScreen.LineDetails(lineId))
            }
        )
    }

    composable<NavigationScreen.LineDetails> { backStackEntry ->
        val route = backStackEntry.toRoute<NavigationScreen.LineDetails>()
        LineDetailsScreen(
            lineId = route.lineId,
            onBackClick = { navController.navigateUp() }
        )
    }
}
```

### 8.3 Update Tube Status Screen

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/ui/LineStatusScreen.kt`

Add `onLineClick` parameter and pass to LineStatusItem:

```kotlin
@Composable
fun LineStatusScreen(
    onLineClick: (String) -> Unit = {}  // NEW
) {
    // ... existing code ...

    LazyColumn {
        items(lines) { line ->
            LineStatusItem(
                line = line,
                onClick = { onLineClick(line.id) }  // NEW
            )
        }
    }
}
```

**Test**: Build and run, verify navigation parameter passing (even if screen not yet implemented)

---

## Step 9: Create UI Components (2-3 hours)

### 9.1 Create Main Screen Composable

**File**: `app/src/main/java/com/smartcommute/feature/linedetails/ui/LineDetailsScreen.kt`

```kotlin
@Composable
fun LineDetailsScreen(
    lineId: String,
    onBackClick: () -> Unit,
    viewModel: LineDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Line Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is LineDetailsUiState.Loading -> LoadingState(Modifier.padding(padding))
            is LineDetailsUiState.Success -> SuccessContent(
                lineDetails = state.lineDetails,
                expandedDisruptions = state.expandedDisruptions,
                expandedClosures = state.expandedClosures,
                onDisruptionExpand = viewModel::toggleDisruptionExpansion,
                onClosureExpand = viewModel::toggleClosureExpansion,
                modifier = Modifier.padding(padding)
            )
            is LineDetailsUiState.Error -> ErrorState(
                message = state.message,
                onBackClick = onBackClick,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
```

### 9.2 Create Header Component

**File**: `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/LineDetailsHeader.kt`

```kotlin
@Composable
fun LineDetailsHeader(
    lineDetails: UndergroundLineDetails,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val imageHeight = maxHeight * 0.33f

        // Background image
        GlideImage(
            model = getDrawableId(lineDetails.headerImageRes),
            contentDescription = "${lineDetails.lineName} station",
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight),
            contentScale = ContentScale.Crop
        )

        // Gradient scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight * 0.5f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        // Line icon (bottom-right)
        Icon(
            painter = painterResource(getLineIcon(lineDetails.lineId)),
            contentDescription = null,
            tint = lineDetails.brandColor,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .sharedElement(/* shared element key */)
        )

        // Line name and status (bottom-left)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = lineDetails.lineName,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.sharedElement(/* shared element key */)
            )
            Text(
                text = lineDetails.statusType,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.sharedElement(/* shared element key */)
            )
        }
    }
}
```

### 9.3 Create Body Content Components

**Location**: `app/src/main/java/com/smartcommute/feature/linedetails/ui/components/`

Create files:
- `DisruptionCard.kt` - Shows disruption with expand/collapse
- `ClosureCard.kt` - Shows closure with expand/collapse
- `CrowdingCard.kt` - Shows crowding indicator
- `StatusSummaryCard.kt` - Shows status overview
- `EmptyStateCard.kt` - Shows "No information available"

**Each card should**:
- Use Material3 Card component
- Follow 16dp horizontal padding
- Include proper elevation and colors
- Support expand/collapse for long text (per FR-014)
- Show "Information not available" for missing data (per FR-013)

### 9.4 Implement Shared Element Transitions

Update header components to include `Modifier.sharedElement()`:

```kotlin
Text(
    text = lineDetails.lineName,
    modifier = Modifier.sharedElement(
        state = rememberSharedContentState(key = "line_name_${lineDetails.lineId}"),
        animatedVisibilityScope = animatedVisibilityScope
    )
)
```

Also update LineStatusItem in Tube Status Screen with matching keys.

**Test**: Run app, click a line, verify screen navigates (even if UI incomplete)

---

## Step 10: Implement UI State Handling (30 minutes)

### 10.1 Loading State

```kotlin
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
```

### 10.2 Error State

```kotlin
@Composable
fun ErrorState(
    message: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBackClick) {
            Text("Go Back")
        }
    }
}
```

**Test**: Manually trigger error state by passing invalid lineId, verify error UI displays

---

## Step 11: Polish & Testing (1-2 hours)

### 11.1 Manual Test Checklist

- [ ] App launches without crashes
- [ ] Database migration runs successfully
- [ ] Navigate from Tube Status to Line Details
- [ ] Header image displays correctly
- [ ] Line icon, name, status visible over image
- [ ] Text is readable with gradient scrim (4.5:1 contrast)
- [ ] Disruptions display with expand/collapse
- [ ] Closures display with expand/collapse
- [ ] Crowding indicator shows correct level
- [ ] "Information not available" shows for missing fields
- [ ] Shared element transitions animate smoothly (<400ms)
- [ ] Back button returns to Tube Status Screen
- [ ] Screen works in portrait and landscape
- [ ] Dark mode supported
- [ ] No ANR during data loading
- [ ] Screen loads in <2 seconds

### 11.2 Edge Case Testing

- [ ] Test with lineId not in database → Error state
- [ ] Test with missing crowding data → Shows placeholder
- [ ] Test with no disruptions → Shows "No current disruptions"
- [ ] Test with very long disruption text → Truncates with "Read more"
- [ ] Test device rotation → State preserved
- [ ] Test on different screen sizes → Layout adapts

### 11.3 Performance Verification

- [ ] Profile screen load time (<2s requirement)
- [ ] Check animation frame rate (should be 60fps)
- [ ] Verify no memory leaks (ViewModel cleanup)
- [ ] Check image loading performance (Glide caching)

---

## Step 12: Integration with Tube Status Screen (Prerequisite)

**Note**: This step updates the existing Tube Status Screen to populate line details cache. This should be implemented BEFORE Line Details Screen can function properly.

### 12.1 Extend TFL API Service

**File**: `app/src/main/java/com/smartcommute/core/network/TflApiService.kt`

Add endpoint for detailed line information (if not already present):

```kotlin
@GET("Line/{lineId}/Disruption")
suspend fun getLineDisruptions(
    @Path("lineId") lineId: String,
    @Query("app_key") apiKey: String = BuildConfig.TFL_API_KEY
): Response<List<DisruptionDto>>

@GET("Line/{lineId}/Route/Sequence/{direction}")
suspend fun getLineRoute(
    @Path("lineId") lineId: String,
    @Path("direction") direction: String = "inbound",
    @Query("app_key") apiKey: String = BuildConfig.TFL_API_KEY
): Response<RouteDto>
```

### 12.2 Extend Repository

**File**: `app/src/main/java/com/smartcommute/feature/linestatus/data/LineStatusRepositoryImpl.kt`

Add methods to cache detailed line information:

```kotlin
override suspend fun refreshLineStatuses(): NetworkResult<List<UndergroundLine>> {
    // Existing code to fetch line statuses...

    // NEW: For each line, fetch and cache detailed information
    lines.forEach { line ->
        fetchAndCacheLineDetails(line.id)
    }

    return NetworkResult.Success(lines)
}

private suspend fun fetchAndCacheLineDetails(lineId: String) {
    try {
        // Fetch disruptions
        val disruptionsResponse = apiService.getLineDisruptions(lineId)
        if (disruptionsResponse.isSuccessful) {
            val disruptions = disruptionsResponse.body()?.map { it.toEntity(lineId) } ?: emptyList()
            lineDetailsDao.deleteDisruptions(lineId)  // Clear old data
            lineDetailsDao.insertDisruptions(disruptions)
        }

        // Fetch closures (may be embedded in disruptions or separate endpoint)
        // ...

        // Cache main line details entity
        val lineDetailsEntity = createLineDetailsEntity(lineId)
        lineDetailsDao.insertLineDetails(lineDetailsEntity)

    } catch (e: Exception) {
        // Log error but don't fail entire refresh
        Log.e("LineStatus", "Failed to cache details for $lineId", e)
    }
}
```

**Test**: Trigger refresh from Tube Status Screen, verify database tables populated

---

## Completion Checklist

- [ ] All 12 steps completed
- [ ] Code compiles without errors
- [ ] App runs without crashes
- [ ] Line Details Screen displays data correctly
- [ ] All FR-001 through FR-014 requirements met
- [ ] All SC-001 through SC-006 success criteria achieved
- [ ] Manual testing checklist passed
- [ ] Edge cases handled gracefully
- [ ] Performance requirements met (<2s load, <400ms animations)
- [ ] Code follows Android constitution standards (MVVM, Hilt, Compose)
- [ ] No network requests made in Line Details Screen (FR-007 verified)

---

## Troubleshooting

### Issue: Database migration fails

**Solution**: Clear app data and reinstall, or add fallbackToDestructiveMigration() temporarily

### Issue: Images not displaying

**Solution**: Check resource names match lineId (lowercase), verify WebP format supported on device API level

### Issue: Shared element transitions not working

**Solution**: Verify both source and destination composables use matching shared element keys, check animatedVisibilityScope is provided

### Issue: Navigation passing wrong lineId

**Solution**: Debug navigation arguments in LineDetailsViewModel, verify SavedStateHandle contains "lineId" key

### Issue: UI state not preserved on rotation

**Solution**: Verify ViewModel uses StateFlow (not mutableStateOf), check ViewModel is not recreated

### Issue: App ANR during data loading

**Solution**: Verify repository uses `flowOn(Dispatchers.IO)`, check database queries are not on main thread

---

## Next Steps After Implementation

1. Run full manual test suite
2. Test on multiple devices (different screen sizes, API levels)
3. Profile performance with Android Profiler
4. Verify WCAG accessibility compliance (TalkBack, color contrast)
5. Update CLAUDE.md with any new dependencies or patterns
6. Create pull request with complete implementation
7. Demo feature to stakeholders

---

## Resources

- **Spec**: `/specs/002-line-details-screen/spec.md`
- **Research**: `/specs/002-line-details-screen/research.md`
- **Data Model**: `/specs/002-line-details-screen/data-model.md`
- **Contracts**: `/specs/002-line-details-screen/contracts/`
- **Android Compose Docs**: https://developer.android.com/develop/ui/compose
- **Material 3 Guidelines**: https://m3.material.io/
- **TFL API Docs**: https://api.tfl.gov.uk/

---

**Estimated Implementation Time**: 8-12 hours for experienced Android developer

**Complexity Level**: Medium (extends existing patterns, some new UI patterns for shared elements)

Good luck with implementation! 🚇
