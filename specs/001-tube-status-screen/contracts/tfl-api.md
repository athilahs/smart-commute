# TfL API Contract: Line Status

**Feature**: 001-tube-status-screen
**Date**: 2025-12-24
**Purpose**: Document Transport for London (TfL) Unified API integration contract for fetching tube line statuses

## API Endpoint

**Base URL**: `https://api.tfl.gov.uk`

**Endpoint**: `GET /Line/Mode/{mode}/Status`

**Full URL (for tube mode)**: `https://api.tfl.gov.uk/Line/Mode/tube/Status`

## Authentication

**Method**: API Key (Query Parameter)

**Parameter**: `app_key`

**Example**: `GET https://api.tfl.gov.uk/Line/Mode/tube/Status?app_key={YOUR_API_KEY}`

**Obtaining API Key**:
1. Register at TfL Developer Portal: https://api-portal.tfl.gov.uk/
2. Create application and obtain `app_key`
3. Store in `local.properties`: `TFL_API_KEY=your_key_here`
4. Inject via BuildConfig in `build.gradle.kts`

**Rate Limits**:
- Free Tier: 500 requests per minute
- Usage Pattern: Manual refresh only (no automatic polling) → well within limits
- Exponential backoff on rate limit errors (429 status code)

## Request

### HTTP Method
`GET`

### Headers
```
Accept: application/json
User-Agent: SmartCommute-Android/1.0
```

### Query Parameters
| Parameter | Required | Type | Description |
|-----------|----------|------|-------------|
| app_key | Yes | String | TfL API key for authentication |

### Example Request (Retrofit Interface)
```kotlin
interface TflApiService {
    @GET("Line/Mode/tube/Status")
    suspend fun getLineStatus(
        @Query("app_key") apiKey: String
    ): List<LineStatusResponseDto>
}
```

## Response

### Success Response (200 OK)

**Content-Type**: `application/json`

**Body**: Array of line status objects

```json
[
  {
    "id": "bakerloo",
    "name": "Bakerloo",
    "modeName": "tube",
    "created": "2025-12-24T10:00:00Z",
    "modified": "2025-12-24T10:15:00Z",
    "lineStatuses": [
      {
        "id": 0,
        "statusSeverity": 10,
        "statusSeverityDescription": "Good Service",
        "reason": null,
        "created": "0001-01-01T00:00:00",
        "validityPeriods": []
      }
    ],
    "routeSections": [],
    "serviceTypes": [
      {
        "name": "Regular",
        "uri": "/Line/Route?ids=Bakerloo&serviceTypes=Regular"
      }
    ]
  },
  {
    "id": "central",
    "name": "Central",
    "modeName": "tube",
    "created": "2025-12-24T10:00:00Z",
    "modified": "2025-12-24T10:15:00Z",
    "lineStatuses": [
      {
        "id": 0,
        "statusSeverity": 6,
        "statusSeverityDescription": "Minor Delays",
        "reason": "Minor delays due to a signal failure at Oxford Circus",
        "created": "0001-01-01T00:00:00",
        "validityPeriods": []
      }
    ],
    "routeSections": [],
    "serviceTypes": [
      {
        "name": "Regular",
        "uri": "/Line/Route?ids=Central&serviceTypes=Regular"
      }
    ]
  }
]
```

### Response Fields

#### Top-Level Line Object
| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | String | No | Unique line identifier (e.g., "bakerloo", "central") |
| name | String | No | Human-readable line name (e.g., "Bakerloo", "Central") |
| modeName | String | No | Transport mode (always "tube" for underground) |
| created | String | No | ISO 8601 timestamp of line creation |
| modified | String | No | ISO 8601 timestamp of last modification |
| lineStatuses | Array | No | Array of status objects (typically contains 1 item) |
| routeSections | Array | No | Route sections (not used in this feature) |
| serviceTypes | Array | No | Service type metadata (not used in this feature) |

#### LineStatus Object (within `lineStatuses` array)
| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | Integer | No | Status ID (internal TfL identifier) |
| statusSeverity | Integer | No | Numeric severity code (see Status Severity Mapping below) |
| statusSeverityDescription | String | No | Human-readable status text |
| reason | String | Yes | Optional detailed explanation of disruption |
| created | String | No | Timestamp (often default value) |
| validityPeriods | Array | No | Time periods for status validity (not used) |

### Status Severity Mapping

TfL API uses numeric `statusSeverity` codes. Mapping to app's `StatusType`:

| statusSeverity | statusSeverityDescription | App StatusType | Display Name | Severity |
|----------------|---------------------------|----------------|--------------|----------|
| 10 | Good Service | GOOD_SERVICE | Good Service | 0 |
| 9 | Minor Delays | MINOR_DELAYS | Minor Delays | 1 |
| 8 | Major Delays | MAJOR_DELAYS | Major Delays | 2 |
| 7 | Severe Delays | SEVERE_DELAYS | Severe Delays | 3 |
| 6 | Part Closure | PART_CLOSURE | Part Closure | 4 |
| 5 | Planned Closure | PLANNED_CLOSURE | Planned Closure | 4 |
| 4 | Closure | CLOSURE | Closure | 5 |
| Other | (any other text) | SERVICE_DISRUPTION | Service Disruption | 2 |

**Note**: If TfL returns an unrecognized `statusSeverityDescription`, map to `SERVICE_DISRUPTION` (per spec clarification).

### Error Responses

#### 400 Bad Request
Invalid parameters (e.g., invalid mode)
```json
{
  "timestampUtc": "2025-12-24T10:00:00Z",
  "exceptionType": "ValidationException",
  "httpStatusCode": 400,
  "httpStatus": "BadRequest",
  "relativeUri": "/Line/Mode/invalid/Status",
  "message": "Invalid mode: invalid"
}
```

**Handling**: Log error, show user "Unable to fetch status" error message with retry button (FR-022)

#### 401 Unauthorized
Invalid or missing API key
```json
{
  "timestampUtc": "2025-12-24T10:00:00Z",
  "exceptionType": "UnauthorizedException",
  "httpStatusCode": 401,
  "httpStatus": "Unauthorized",
  "message": "Invalid API key"
}
```

**Handling**: Critical error - cannot proceed. Show "Configuration error" message to user, log details for debugging.

#### 429 Too Many Requests
Rate limit exceeded
```json
{
  "timestampUtc": "2025-12-24T10:00:00Z",
  "exceptionType": "TooManyRequestsException",
  "httpStatusCode": 429,
  "httpStatus": "TooManyRequests",
  "message": "Rate limit exceeded"
}
```

**Handling**: Exponential backoff retry. Show cached data with "Service temporarily unavailable" banner (FR-021).

#### 500 Internal Server Error
TfL API internal issue
```json
{
  "timestampUtc": "2025-12-24T10:00:00Z",
  "exceptionType": "InternalServerErrorException",
  "httpStatusCode": 500,
  "httpStatus": "InternalServerError",
  "message": "Internal server error"
}
```

**Handling**: Show cached data if available with "Service temporarily unavailable" banner (FR-021). If no cache, show error message with retry (FR-023).

#### Network Timeout / No Connection
No HTTP response (IOException)

**Handling**: Show cached data with "No connection" banner (FR-018, FR-019). Automatically retry in background when connection restored (FR-020).

## Data Transfer Objects (Kotlin)

### Request (Retrofit Interface)
```kotlin
interface TflApiService {
    @GET("Line/Mode/tube/Status")
    suspend fun getLineStatus(
        @Query("app_key") apiKey: String
    ): List<LineStatusResponseDto>
}
```

### Response DTOs
```kotlin
data class LineStatusResponseDto(
    val id: String,
    val name: String,
    val modeName: String,
    val created: String,
    val modified: String,
    val lineStatuses: List<LineStatusDto>,
    val routeSections: List<Any> = emptyList(),  // Unused, accept any
    val serviceTypes: List<Any> = emptyList()    // Unused, accept any
)

data class LineStatusDto(
    val id: Int,
    val statusSeverity: Int,
    val statusSeverityDescription: String,
    val reason: String?,
    val created: String,
    val validityPeriods: List<Any> = emptyList()  // Unused
)
```

## Retry Strategy

### Exponential Backoff
- Initial retry delay: 2 seconds
- Max retry attempts: 3
- Backoff multiplier: 2x
- Max delay: 16 seconds

### Retry Conditions
- Retry: Network timeout, 429 (rate limit), 500/502/503/504 (server errors)
- Don't retry: 400 (bad request), 401 (unauthorized), 404 (not found)

### Implementation (OkHttp Interceptor)
```kotlin
class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var tryCount = 0
        val maxRetries = 3

        while (!response.isSuccessful && tryCount < maxRetries) {
            if (response.code in listOf(429, 500, 502, 503, 504)) {
                tryCount++
                val delayMillis = (2000L * (1 shl tryCount)).coerceAtMost(16000L)
                Thread.sleep(delayMillis)
                response.close()
                response = chain.proceed(request)
            } else {
                break  // Don't retry on other errors
            }
        }
        return response
    }
}
```

## Caching Strategy

- **Cache Location**: Room database (see data-model.md)
- **Cache Duration**: No expiration (user refreshes manually)
- **Cache Key**: Line ID (primary key)
- **Update Strategy**: Replace all rows on successful fetch
- **Fallback**: Always serve cached data if API fails and cache exists

## Testing Considerations

**Note**: Per constitution, no automated tests are created. Manual verification procedures:

### Manual Test Scenarios
1. **First Launch (No Cache)**:
   - Verify loading spinner displays
   - Wait for API response
   - Verify all 11 lines displayed with correct statuses

2. **Offline Mode**:
   - Turn off device network
   - Launch app
   - Verify cached data displays with "No connection" banner
   - Verify "Last updated" timestamp shows

3. **API Error Handling**:
   - Use invalid API key → Verify error message
   - Simulate 500 error → Verify cached data + "Service temporarily unavailable" banner

4. **Manual Refresh**:
   - Pull down on list → Verify small loading indicator at top
   - Wait for response → Verify updated data displays
   - Verify timestamp updates

5. **Unknown Status**:
   - (If TfL returns unexpected status) → Verify maps to "Service Disruption" with warning icon

## Example Integration Code

### Retrofit Setup (NetworkModule.kt)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.tfl.gov.uk/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTflApiService(retrofit: Retrofit): TflApiService {
        return retrofit.create(TflApiService::class.java)
    }
}
```

### Repository Usage
```kotlin
class LineStatusRepositoryImpl @Inject constructor(
    private val tflApiService: TflApiService,
    private val lineStatusDao: LineStatusDao
) : LineStatusRepository {

    override suspend fun fetchLineStatus(): Flow<NetworkResult<List<UndergroundLine>>> = flow {
        emit(NetworkResult.Loading)

        try {
            val apiKey = BuildConfig.TFL_API_KEY
            val response = tflApiService.getLineStatus(apiKey)
            val domainModels = response.map { it.toDomainModel() }

            // Cache to database
            val entities = domainModels.map { it.toEntity(System.currentTimeMillis()) }
            lineStatusDao.insertAll(entities)

            emit(NetworkResult.Success(domainModels))
        } catch (e: IOException) {
            // Network error → Try cached data
            val cachedData = lineStatusDao.getAllLineStatuses().firstOrNull()
            if (cachedData != null) {
                val domainModels = cachedData.map { it.toDomainModel() }
                emit(NetworkResult.Success(domainModels, isOffline = true))
            } else {
                emit(NetworkResult.Error(e))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e))
        }
    }
}
```

## API Documentation Reference

**Official TfL API Documentation**: https://api.tfl.gov.uk/

**Line Status Endpoint Documentation**: https://api.tfl.gov.uk/swagger/ui/index.html#!/Line/Line_StatusByMode

**Rate Limits & Terms**: https://api-portal.tfl.gov.uk/

This contract covers all requirements for FR-006 (TfL API integration), FR-020 (retry button), FR-021/FR-022/FR-023 (error handling), and supports offline-first architecture (FR-017, FR-018, FR-019).
