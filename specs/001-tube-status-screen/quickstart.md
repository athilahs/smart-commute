# Quickstart Guide: Tube Status Screen

**Feature**: 001-tube-status-screen
**Date**: 2025-12-24
**Purpose**: Step-by-step guide for setting up and implementing the London Underground line status monitoring feature

## Prerequisites

### Required Tools
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 or later
- **Android SDK**: API 26 (Android 8.0) minimum, API 34 (Android 14) target
- **Kotlin**: 1.9+ (specified in project)
- **Gradle**: 8.2+ (via wrapper)

### Required Accounts
- **TfL Developer Account**: Register at https://api-portal.tfl.gov.uk/ to obtain API key

## Step 1: Project Setup

### 1.1 Initialize Android Project

If starting fresh (skip if project exists):

```bash
# Create new Android project via Android Studio
# - Template: Empty Compose Activity
# - Name: SmartCommute
# - Package: com.smartcommute
# - Min SDK: API 26
# - Build configuration language: Kotlin DSL
```

### 1.2 Configure Gradle Version Catalog

Create/update `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "1.9.21"
compose = "1.5.4"
compose-bom = "2024.01.00"
hilt = "2.50"
retrofit = "2.9.0"
room = "2.6.1"
glide = "4.16.0"
coroutines = "1.7.3"
lifecycle = "2.7.0"
navigation = "2.7.6"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-runtime = { group = "androidx.compose.runtime", name = "runtime" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.1.0" }

# Retrofit
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version = "4.12.0" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version = "4.12.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

# Glide
glide = { group = "com.github.bumptech.glide", name = "glide", version.ref = "glide" }
glide-compose = { group = "com.github.bumptech.glide", name = "compose", version = "1.0.0-beta01" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Lifecycle
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Core Android
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.12.0" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version = "1.8.2" }

[plugins]
android-application = { id = "com.android.application", version = "8.2.1" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "1.9.21-1.0.15" }
```

### 1.3 Configure App-Level `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.smartcommute"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smartcommute"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Load API key from local.properties
        val properties = org.jetbrains.kotlin.konan.properties.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }
        buildConfigField("String", "TFL_API_KEY", "\"${properties.getProperty("TFL_API_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    debugImplementation(libs.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Glide
    implementation(libs.glide)
    implementation(libs.glide.compose)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.navigation.compose)

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}
```

### 1.4 Configure TfL API Key

Create `local.properties` in project root (gitignored by default):

```properties
# local.properties
TFL_API_KEY=your_api_key_here
```

**To obtain TfL API key**:
1. Go to https://api-portal.tfl.gov.uk/
2. Sign up / Log in
3. Create new app
4. Copy `app_key` value
5. Paste into `local.properties`

## Step 2: Create Base Application Class

### 2.1 Create `SmartCommuteApplication.kt`

**File**: `app/src/main/java/com/smartcommute/SmartCommuteApplication.kt`

```kotlin
package com.smartcommute

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartCommuteApplication : Application()
```

### 2.2 Update `AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".SmartCommuteApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SmartCommute"
        tools:targetApi="31">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SmartCommute">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

## Step 3: Implement Core Infrastructure

Follow implementation order defined in `data-model.md` and file structure from `plan.md`:

### 3.1 Domain Layer (Models)
1. Create `ServiceStatus.kt` with `StatusType` enum
2. Create `UndergroundLine.kt` domain model

### 3.2 Data Layer - DTOs
1. Create `LineStatusDto.kt`
2. Create `LineStatusResponseDto.kt`

### 3.3 Data Layer - Database
1. Create `LineStatusEntity.kt` with Room annotations
2. Create `LineStatusDao.kt` interface
3. Create `LineStatusDatabase.kt` abstract class

### 3.4 Data Layer - Network
1. Create `TflApiService.kt` Retrofit interface (use contract from `contracts/tfl-api.md`)
2. Create `LineStatusMapper.kt` for DTO ↔ Domain ↔ Entity conversions

### 3.5 Data Layer - Repository
1. Create `LineStatusRepository.kt` interface
2. Create `LineStatusRepositoryImpl.kt` implementation

### 3.6 Dependency Injection Modules
1. Create `NetworkModule.kt` (Retrofit, OkHttp setup)
2. Create `DatabaseModule.kt` (Room database setup)
3. Create `AppModule.kt` (Repository binding)

## Step 4: Implement UI Layer

### 4.1 UI State
1. Create `LineStatusUiState.kt` sealed class (see `data-model.md`)

### 4.2 ViewModel
1. Create `LineStatusViewModel.kt`:
   - Inject `LineStatusRepository`
   - Expose `StateFlow<LineStatusUiState>`
   - Implement `refreshStatus()` function for manual refresh

### 4.3 UI Components
1. Create Material 3 theme files in `core/ui/theme/`:
   - `Color.kt` (tube line colors)
   - `Theme.kt` (M3 theme with dark mode)
   - `Type.kt` (typography)

2. Create reusable components in `feature/linestatus/ui/components/`:
   - `LineStatusItem.kt` (single list item composable)
   - `StatusIndicator.kt` (colored status icon/badge)
   - `LoadingStateOverlay.kt` (full-screen spinner)

3. Create `LineStatusScreen.kt`:
   - Scaffold with TopAppBar
   - LazyColumn with line items
   - Pull-to-refresh integration
   - State handling (Loading, Success, Error)

### 4.4 Navigation
1. Create `NavigationScreen.kt` sealed class
2. Create `AppNavigation.kt` with NavHost
3. Create `MainScreen.kt` with BottomNavigation

### 4.5 Main Activity
Update `MainActivity.kt`:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCommuteTheme {
                MainScreen()
            }
        }
    }
}
```

## Step 5: Add Tube Line Logo Assets

### 5.1 Source Logos
Download official TfL tube line logos from:
- TfL Brand Guidelines: https://tfl.gov.uk/corporate/about-tfl/what-we-do/corporate-and-social-responsibility/corporate-responsibility-and-our-brands
- Or use Material Icons / public SVG resources

### 5.2 Convert to Vector Drawables
1. Convert SVG to Android Vector Drawable (Android Studio: Right-click `res/drawable` → New → Vector Asset → Local file)
2. Name convention: `ic_line_{lineid}.xml`

**Required logos** (11 Underground lines):
- `ic_line_bakerloo.xml`
- `ic_line_central.xml`
- `ic_line_circle.xml`
- `ic_line_district.xml`
- `ic_line_hammersmith_city.xml`
- `ic_line_jubilee.xml`
- `ic_line_metropolitan.xml`
- `ic_line_northern.xml`
- `ic_line_piccadilly.xml`
- `ic_line_victoria.xml`
- `ic_line_waterloo_city.xml`

## Step 6: String Resources

Create `res/values/strings.xml`:

```xml
<resources>
    <string name="app_name">SmartCommute</string>
    <string name="screen_title_line_status">Line Status</string>
    <string name="status_good_service">Good Service</string>
    <string name="status_minor_delays">Minor Delays</string>
    <string name="status_major_delays">Major Delays</string>
    <string name="status_severe_delays">Severe Delays</string>
    <string name="status_closure">Closure</string>
    <string name="status_service_disruption">Service Disruption</string>
    <string name="banner_no_connection">No connection</string>
    <string name="banner_service_unavailable">Service temporarily unavailable</string>
    <string name="last_updated">Last updated: %s</string>
    <string name="error_no_data">Unable to load line status</string>
    <string name="button_retry">Retry</string>
    <string name="content_description_line_logo">%s line logo</string>
    <string name="content_description_status_indicator">%s status indicator</string>
</resources>
```

## Step 7: ProGuard Rules

Update `proguard-rules.pro` for release builds:

```proguard
# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.smartcommute.feature.linestatus.data.remote.dto.** { <fields>; }

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
```

## Step 8: Build and Run

### 8.1 Sync Gradle
```bash
# In Android Studio
File → Sync Project with Gradle Files
```

### 8.2 Build APK
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease
```

### 8.3 Run on Device/Emulator
```bash
# Install and launch
./gradlew installDebug
adb shell am start -n com.smartcommute/.MainActivity
```

## Step 9: Manual Verification

### Test Scenarios (No Automated Tests per Constitution)

1. **First Launch**:
   - ✅ Loading spinner displays
   - ✅ All 11 tube lines appear
   - ✅ Correct statuses from TfL API
   - ✅ Line logos display correctly

2. **Pull-to-Refresh**:
   - ✅ Pull down gesture triggers refresh
   - ✅ Small loading indicator at top
   - ✅ Data updates after API call
   - ✅ "Last updated" timestamp changes

3. **Offline Mode**:
   - Turn off WiFi/mobile data
   - ✅ Cached data displays
   - ✅ "No connection" banner shows
   - ✅ "Last updated" timestamp shows cached time
   - Turn on network
   - ✅ Auto-retry in background
   - ✅ Banner disappears when connection restored

4. **API Error Handling**:
   - Temporarily use invalid API key
   - ✅ Error message displays with retry button
   - ✅ If cache exists, shows cached data with banner

5. **Dark Mode**:
   - Toggle system dark mode
   - ✅ Theme switches correctly
   - ✅ Status colors remain visible in both modes

6. **Accessibility**:
   - Enable TalkBack
   - ✅ Line items announced as "{Line Name}: {Status}"
   - ✅ Refresh button has proper label

7. **Screen Rotation**:
   - Rotate device
   - ✅ State persists (doesn't reload)
   - ✅ UI adapts to landscape

## Step 10: Git Commit

```bash
# Stage all files
git add .

# Commit following Specify workflow convention
git commit -m "feat: implement tube status screen with TfL API integration

- Add MVVM architecture with Jetpack Compose UI
- Integrate TfL Unified API for line status data
- Implement Room caching for offline viewing
- Add pull-to-refresh and manual refresh button
- Support dark mode and accessibility features
- Handle offline and API error states gracefully

Implements FR-001 through FR-025 per spec.md
Complies with SmartCommute Constitution v1.0.0

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

## Troubleshooting

### Build Errors

**Error**: `Cannot find symbol: BuildConfig.TFL_API_KEY`
**Solution**: Ensure `local.properties` contains `TFL_API_KEY` and rebuild project

**Error**: Room schema export directory warnings
**Solution**: Add to `build.gradle.kts`:
```kotlin
room {
    schemaDirectory("$projectDir/schemas")
}
```

### Runtime Issues

**Issue**: API returns 401 Unauthorized
**Solution**: Verify API key is correct in `local.properties` and app is rebuilt

**Issue**: No internet connection but app shows loading forever
**Solution**: Check Repository implementation handles IOException and falls back to cache

**Issue**: Dark mode colors not working
**Solution**: Verify `values-night/themes.xml` exists with dark color scheme

## Next Steps

After completing this quickstart:

1. Run through all manual verification scenarios
2. Generate tasks via `/speckit.tasks` for detailed implementation checklist
3. Proceed with implementation following task order
4. Manual validation at each checkpoint (per user story priorities)

Reference documents:
- `spec.md` - Functional requirements and success criteria
- `data-model.md` - Entity schemas and validation rules
- `contracts/tfl-api.md` - TfL API integration details
- `research.md` - Technical decisions and rationale

For questions or clarifications, refer to the clarification session in `spec.md`.
