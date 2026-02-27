# Memory Battle - Comprehensive Implementation Guide

## Current Status
The following has been implemented:
1. ✅ Enhanced database entities (Flashcard with SM-2 fields, UserStats, Deck, Tag, FlashcardTagCrossRef, BattleHistory)
2. ✅ All DAOs (FlashcardDao, UserStatsDao, DeckDao, TagDao, FlashcardTagDao, BattleHistoryDao)
3. ✅ Utility classes (SpacedRepetitionScheduler, XPCalculator, DateUtils)
4. ✅ EnhancedGameRepository
5. ✅ AppDatabase updated to version 2 with all entities

## Build Issues to Resolve

### 1. Fix Gradle Build (KAPT vs KSP)
The project is currently failing due to KSP/KAPT annotation processing issues with Room.

**File: `app/build.gradle.kts`**
Current plugins section should use `kapt` instead of `ksp`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)  // Changed from ksp
}
```

In dependencies, change:
```kotlin
// Room
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
kapt(libs.androidx.room.compiler)  // Changed from ksp
```

### 2. Update libs.versions.toml
Ensure `kotlin-kapt` plugin is defined:
```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlinVersion" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlinVersion" }
```

### 3. Add Missing Dependencies
Add to `app/build.gradle.kts`:
```kotlin
dependencies {
    // ...existing dependencies...
    
    // WorkManager for notifications
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Testing
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
}
```

## Remaining Implementation Steps

### Phase 1: Fix Database and Build
1. Clean project: `./gradlew clean`
2. Delete `app/build` directory
3. Invalidate caches in Android Studio
4. Rebuild project

### Phase 2: Create ViewModels

#### Enhanced HomeViewModel
**File: `app/src/main/java/com/example/revizo/ui/home/EnhancedHomeViewModel.kt`**
```kotlin
package com.example.revizo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revizo.data.entity.Deck
import com.example.revizo.data.entity.UserStats
import com.example.revizo.data.repository.EnhancedGameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val userStats: UserStats? = null,
    val dueCardCount: Int = 0,
    val isDailyChallengeAvailable: Boolean = true,
    val decks: List<Deck> = emptyList(),
    val isLoading: Boolean = true
)

class EnhancedHomeViewModel(
    private val repository: EnhancedGameRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.getUserStatsFlow().collect { stats ->
                _uiState.value = _uiState.value.copy(
                    userStats = stats,
                    isLoading = false
                )
            }
        }
        
        viewModelScope.launch {
            repository.getAllDecks().collect { decks ->
                _uiState.value = _uiState.value.copy(decks = decks)
            }
        }
        
        viewModelScope.launch {
            val dueCount = repository.getDueCardCount()
            val challengeAvailable = repository.isDailyChallengeAvailable()
            _uiState.value = _uiState.value.copy(
                dueCardCount = dueCount,
                isDailyChallengeAvailable = challengeAvailable
            )
        }
    }
    
    fun refreshData() {
        loadData()
    }
}
```

#### Enhanced BattleViewModel
Update `BattleViewModel.kt` to use SM-2 and enhanced repository.

### Phase 3: Create New Activities/Fragments

#### DeckManagementActivity
- List all decks
- Create/Edit/Delete deck
- View cards in deck
- RecyclerView with deck cards

#### StatsActivity
- Accuracy percentage
- XP history chart (last 30 days)
- Weakest tags
- Average response time
- Total battles played

#### SettingsActivity
- Toggle notifications
- Set battle length (5/10/15)
- Set daily target
- Reset progress (with confirmation dialog)
- Export/Import deck JSON

#### OnboardingActivity
- Welcome screen
- Select interests (decks)
- Set daily target
- Save to DataStore

### Phase 4: Implement WorkManager Notifications

**File: `app/src/main/java/com/example/revizo/worker/DailyReminderWorker.kt`**
```kotlin
package com.example.revizo.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.revizo.R
import com.example.revizo.data.database.AppDatabase
import com.example.revizo.ui.home.HomeActivity

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val userStats = database.userStatsDao().getUserStats()
        
        // Check if notifications are enabled
        if (userStats?.notificationsEnabled != true) {
            return Result.success()
        }
        
        // Check if there are due cards
        val dueCount = database.flashcardDao().getDueCardCount(System.currentTimeMillis())
        
        if (dueCount > 0) {
            showNotification(dueCount)
        }
        
        return Result.success()
    }
    
    private fun showNotification(dueCount: Int) {
        val channelId = "daily_reminder"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily study reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(applicationContext, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification) // TODO: Add icon
            .setContentTitle("Time to study!")
            .setContentText("You have $dueCount cards due for review")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(1, notification)
    }
}
```

Schedule in Application class or MainActivity:
```kotlin
val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
    1, TimeUnit.DAYS,
    15, TimeUnit.MINUTES // Flex interval
).build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "daily_reminder",
    ExistingPeriodicWorkPolicy.KEEP,
    dailyWorkRequest
)
```

### Phase 5: UI Enhancements

#### Colors.xml
**File: `app/src/main/res/values/colors.xml`**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Primary Colors -->
    <color name="primary">#6200EE</color>
    <color name="primary_dark">#3700B3</color>
    <color name="primary_light">#BB86FC</color>
    
    <!-- Accent Colors -->
    <color name="accent">#03DAC5</color>
    <color name="accent_dark">#018786</color>
    
    <!-- Game Colors -->
    <color name="correct_green">#4CAF50</color>
    <color name="incorrect_red">#F44336</color>
    <color name="warning_yellow">#FFC107</color>
    
    <!-- Difficulty Colors -->
    <color name="easy_color">#4CAF50</color>
    <color name="medium_color">#FF9800</color>
    <color name="hard_color">#F44336</color>
    
    <!-- Background Colors (Dark Theme) -->
    <color name="background">#121212</color>
    <color name="surface">#1E1E1E</color>
    <color name="surface_variant">#2C2C2C</color>
    
    <!-- Text Colors -->
    <color name="text_primary">#FFFFFF</color>
    <color name="text_secondary">#B3B3B3</color>
    <color name="text_disabled">#666666</color>
    
    <!-- XP Colors -->
    <color name="xp_bar_bg">#333333</color>
    <color name="xp_bar_fill">#FFD700</color>
    
    <!-- Streak Colors -->
    <color name="streak_fire">#FF5722</color>
</resources>
```

#### Themes.xml (Dark Theme)
**File: `app/src/main/res/values/themes.xml`**
```xml
<resources>
    <style name="Theme.Revizo" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryDark">@color/primary_dark</item>
        <item name="colorAccent">@color/accent</item>
        
        <item name="android:windowBackground">@color/background</item>
        <item name="android:colorBackground">@color/background</item>
        <item name="android:textColorPrimary">@color/text_primary</item>
        <item name="android:textColorSecondary">@color/text_secondary</item>
    </style>
    
    <style name="Theme.Revizo.NoActionBar">
        <item name="windowActionBar">false</item>
        <item name="windowNoTitle">true</item>
    </style>
    
    <style name="CardViewStyle">
        <item name="cardBackgroundColor">@color/surface</item>
        <item name="cardElevation">8dp</item>
        <item name="cardCornerRadius">16dp</item>
    </style>
    
    <style name="ButtonPrimary" parent="Widget.MaterialComponents.Button">
        <item name="backgroundTint">@color/primary</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textSize">16sp</item>
        <item name="android:padding">16dp</item>
        <item name="cornerRadius">8dp</item>
    </style>
</resources>
```

### Phase 6: Testing

#### SM-2 Scheduler Tests
**File: `app/src/test/java/com/example/revizo/SpacedRepetitionSchedulerTest.kt`**
```kotlin
package com.example.revizo

import com.example.revizo.data.entity.Flashcard
import com.example.revizo.util.SpacedRepetitionScheduler
import org.junit.Assert.*
import org.junit.Test

class SpacedRepetitionSchedulerTest {
    
    @Test
    fun `test perfect recall increases interval`() {
        val card = Flashcard(
            id = 1, deckId = 1, subject = "Test",
            question = "Q", correctAnswer = "A",
            optionA = "A", optionB = "B", optionC = "C", optionD = "D",
            difficulty = "MEDIUM"
        )
        
        val updated = SpacedRepetitionScheduler.updateCard(card, quality = 5)
        
        assertEquals(1, updated.repetitionCount)
        assertEquals(1, updated.intervalDays)
        assertTrue(updated.nextReviewTimestamp > System.currentTimeMillis())
    }
    
    @Test
    fun `test incorrect answer resets card`() {
        val card = Flashcard(
            id = 1, deckId = 1, subject = "Test",
            question = "Q", correctAnswer = "A",
            optionA = "A", optionB = "B", optionC = "C", optionD = "D",
            difficulty = "MEDIUM",
            repetitionCount = 5,
            intervalDays = 30
        )
        
        val updated = SpacedRepetitionScheduler.updateCard(card, quality = 2)
        
        assertEquals(0, updated.repetitionCount)
        assertEquals(0, updated.intervalDays)
    }
    
    @Test
    fun `test ease factor minimum threshold`() {
        var card = Flashcard(
            id = 1, deckId = 1, subject = "Test",
            question = "Q", correctAnswer = "A",
            optionA = "A", optionB = "B", optionC = "C", optionD = "D",
            difficulty = "MEDIUM",
            easeFactor = 1.4
        )
        
        // Multiple poor ratings should not drop ease below 1.3
        repeat(10) {
            card = SpacedRepetitionScheduler.updateCard(card, quality = 3)
        }
        
        assertTrue(card.easeFactor >= 1.3)
    }
}
```

#### Repository Tests
**File: `app/src/test/java/com/example/revizo/EnhancedGameRepositoryTest.kt`**
```kotlin
package com.example.revizo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.revizo.data.database.AppDatabase
import com.example.revizo.data.entity.Flashcard
import com.example.revizo.data.repository.EnhancedGameRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class EnhancedGameRepositoryTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: AppDatabase
    private lateinit var repository: EnhancedGameRepository
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        repository = EnhancedGameRepository(
            database.flashcardDao(),
            database.userStatsDao(),
            database.deckDao(),
            database.tagDao(),
            database.flashcardTagDao(),
            database.battleHistoryDao()
        )
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun `test getDueFlashcards returns only due cards`() = runBlocking {
        // Insert cards with different due times
        val dueCard = Flashcard(
            id = 1, deckId = 1, subject = "Test",
            question = "Q1", correctAnswer = "A",
            optionA = "A", optionB = "B", optionC = "C", optionD = "D",
            difficulty = "EASY",
            nextReviewTimestamp = System.currentTimeMillis() - 1000
        )
        
        val futureCard = Flashcard(
            id = 2, deckId = 1, subject = "Test",
            question = "Q2", correctAnswer = "A",
            optionA = "A", optionB = "B", optionC = "C", optionD = "D",
            difficulty = "EASY",
            nextReviewTimestamp = System.currentTimeMillis() + 100000
        )
        
        database.flashcardDao().insertFlashcards(listOf(dueCard, futureCard))
        
        val dueCards = repository.getDueFlashcards(10)
        
        assertEquals(1, dueCards.size)
        assertEquals(dueCard.id, dueCards[0].id)
    }
}
```

### Phase 7: GitHub Actions CI

**File: `.github/workflows/android.yml`**
```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'adopt'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run unit tests
      run: ./gradlew test
    
    - name: Upload test reports
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-reports
        path: app/build/reports/tests/
```

## Summary

This implementation guide covers all remaining steps to complete the Memory Battle app. The key priorities are:

1. **Fix build issues** - Switch from KSP to KAPT
2. **Complete ViewModels** - Enhanced versions with proper state management
3. **Create UI screens** - Deck management, Stats, Settings, Onboarding
4. **Implement WorkManager** - Daily notifications
5. **Add tests** - Unit tests for scheduler and repository
6. **Polish UI** - Dark theme, animations, accessibility

All database entities, DAOs, utilities, and repository are ready. Once the build issues are resolved, the app can be assembled and the remaining UI components can be added incrementally.

