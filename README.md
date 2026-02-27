# Memory Battle - Gamified Flashcard Learning App

A gamified flashcard learning app built with Kotlin and XML layouts for Android. Battle through quiz questions to earn XP, level up, and maintain your daily streak!

## Features

### Version 1.0

#### 🏠 Home Screen
- Display user level and XP progress
- Show current daily streak
- Beautiful dark gaming theme
- Quick access to battles and stats

#### ⚔️ Battle Screen
- Answer 10 multiple-choice questions per battle
- 15-second countdown timer per question
- Immediate feedback (Correct/Incorrect)
- Visual answer highlighting
- Difficulty-based XP rewards:
  - Easy: 10 XP
  - Medium: 20 XP
  - Hard: 40 XP

#### 🎯 Battle Results
- Total XP earned display
- Accuracy percentage
- Correct/Incorrect count
- Streak bonus for consecutive correct answers (every 3)
- Level up notifications

#### 📊 Stats Screen
- Total XP earned
- Current level and progress to next level
- Total battles completed
- Overall accuracy percentage
- Total correct and incorrect answers

#### 🎮 Game Mechanics
- **XP System**: Earn XP based on question difficulty
- **Leveling**: Level up every 500 XP
- **Streaks**: Daily streak tracking to encourage consistent play
- **Streak Bonus**: +25 XP for every 3 correct answers in a row

## Architecture

### MVVM (Model-View-ViewModel)
- **Model**: Room Database entities and repositories
- **View**: XML layouts and Activities
- **ViewModel**: LiveData-based ViewModels for each screen

### Technology Stack
- **Language**: Kotlin
- **UI**: XML Layouts (Material Design)
- **Database**: Room Database
- **Async**: Kotlin Coroutines + Flow
- **Architecture Components**: ViewModel, LiveData
- **Network** (Future): Retrofit for OpenAI API integration

## Project Structure

```
app/src/main/java/com/example/revizo/
├── data/
│   ├── entity/
│   │   ├── Flashcard.kt          # Flashcard entity
│   │   └── UserStats.kt          # User statistics entity
│   ├── dao/
│   │   ├── FlashcardDao.kt       # Flashcard database operations
│   │   └── UserStatsDao.kt       # User stats database operations
│   ├── database/
│   │   └── AppDatabase.kt        # Room database with sample data
│   └── repository/
│       └── GameRepository.kt     # Business logic layer
├── ui/
│   ├── home/
│   │   ├── HomeActivity.kt       # Home screen
│   │   └── HomeViewModel.kt      # Home screen logic
│   ├── battle/
│   │   ├── BattleActivity.kt     # Battle screen
│   │   ├── BattleViewModel.kt    # Battle logic with timer
│   │   └── BattleResultActivity.kt # Battle results
│   └── stats/
│       ├── StatsActivity.kt      # Statistics screen
│       └── StatsViewModel.kt     # Stats logic
└── MainActivity.kt               # Entry point

app/src/main/res/
├── layout/
│   ├── activity_home.xml         # Home screen layout
│   ├── activity_battle.xml       # Battle screen layout
│   ├── activity_battle_result.xml # Results layout
│   └── activity_stats.xml        # Stats screen layout
├── values/
│   ├── colors.xml                # Dark gaming theme colors
│   ├── strings.xml               # All app strings
│   └── themes.xml                # Material 3 dark theme
└── drawable/
    ├── progress_bar_xp.xml       # XP progress bar design
    └── bg_difficulty_badge.xml   # Difficulty badge background
```

## Database Schema

### Flashcard Entity
```kotlin
- id: Int (Primary Key, Auto-generated)
- subject: String
- question: String
- correctAnswer: String
- optionA, optionB, optionC, optionD: String
- difficulty: String (EASY/MEDIUM/HARD)
- correctCount: Int
- wrongCount: Int
- lastSeenTimestamp: Long
```

### UserStats Entity
```kotlin
- id: Int (Primary Key = 1)
- totalXp: Int
- currentLevel: Int
- dailyStreak: Int
- lastPlayedDate: Long
- totalCorrect: Int
- totalIncorrect: Int
- totalBattles: Int
```

## Sample Data

The app comes pre-loaded with 20 flashcards covering:
- Science (Biology, Physics, Chemistry)
- Mathematics
- History
- Geography
- Literature
- Technology
- General Knowledge

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project folder

3. **Sync Gradle**
   - Wait for Gradle sync to complete
   - Dependencies will be downloaded automatically

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" or press Shift+F10

## Minimum Requirements

- Android SDK 24 (Android 7.0 Nougat) or higher
- Target SDK 36
- Kotlin 1.9.0
- Gradle 9.0.1

## Dependencies

```kotlin
// Core
androidx.core:core-ktx:1.17.0
androidx.appcompat:appcompat:1.7.1
com.google.android.material:material:1.13.0

// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// Lifecycle Components
androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0
androidx.lifecycle:lifecycle-livedata-ktx:2.8.0

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

// Retrofit (for future AI integration)
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0
```

## Future Enhancements (Version 2.0)

- [ ] OpenAI API integration for AI-generated questions
- [ ] Custom flashcard creation
- [ ] Multiple battle modes (Time Attack, Endless)
- [ ] Achievements and badges
- [ ] Leaderboards
- [ ] Social features (Share progress)
- [ ] Spaced repetition algorithm
- [ ] Custom study sets
- [ ] Dark/Light theme toggle
- [ ] Audio/Sound effects
- [ ] Animations and transitions

## Color Scheme (Dark Gaming Theme)

- **Background**: `#121212`
- **Surface**: `#1E1E1E`
- **Card**: `#2A2A2A`
- **Primary Purple**: `#BB86FC`
- **Primary Blue**: `#03DAC6`
- **Accent Gold**: `#FFD700`
- **Correct Green**: `#4CAF50`
- **Incorrect Red**: `#F44336`
- **Warning Orange**: `#FF9800`

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the MIT License.

## Contact

For questions or feedback, please open an issue on GitHub.

---

**Happy Learning! 🎮📚**

