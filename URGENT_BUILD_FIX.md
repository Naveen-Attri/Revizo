# URGENT BUILD FIXES

## Problem
The project is failing to build due to KSP annotation processor issues with Room.

## Solution - Apply These Changes Immediately

### 1. Update `app/build.gradle.kts`

Replace the entire file content with:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.revizo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.revizo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

### 2. Clean and Rebuild

Run these commands in Android Studio Terminal:

```bash
.\gradlew.bat clean
```

Then delete these directories manually:
- `app/build`
- `build`
- `.gradle` (in project root)

Then rebuild:
```bash
.\gradlew.bat build
```

### 3. If Still Failing

The AppDatabase file might be too large. Simplify the getSampleFlashcards() function by keeping only the first 10 cards for now.

Edit `app/src/main/java/com/example/revizo/data/database/AppDatabase.kt`:

Find the `getSampleFlashcards()` function (around line 150) and replace it with this simplified version:

```kotlin
private fun getSampleFlashcards(): List<Flashcard> {
    val currentTime = System.currentTimeMillis()
    return listOf(
        Flashcard(
            id = 1, deckId = 1, subject = "DSA",
            question = "What is the time complexity of binary search?",
            correctAnswer = "O(log n)", optionA = "O(n)", optionB = "O(log n)", 
            optionC = "O(n²)", optionD = "O(1)", difficulty = "MEDIUM",
            nextReviewTimestamp = currentTime
        ),
        Flashcard(
            id = 2, deckId = 1, subject = "DSA",
            question = "Which data structure uses LIFO?",
            correctAnswer = "Stack", optionA = "Queue", optionB = "Array", 
            optionC = "Stack", optionD = "Tree", difficulty = "EASY",
            nextReviewTimestamp = currentTime
        ),
        Flashcard(
            id = 3, deckId = 2, subject = "Math",
            question = "What is 7 × 8?",
            correctAnswer = "56", optionA = "54", optionB = "56", 
            optionC = "63", optionD = "64", difficulty = "EASY",
            nextReviewTimestamp = currentTime
        ),
        Flashcard(
            id = 4, deckId = 2, subject = "Math",
            question = "What is π to 2 decimal places?",
            correctAnswer = "3.14", optionA = "3.12", optionB = "3.14", 
            optionC = "3.16", optionD = "3.18", difficulty = "EASY",
            nextReviewTimestamp = currentTime
        ),
        Flashcard(
            id = 5, deckId = 3, subject = "Science",
            question = "What is the chemical symbol for water?",
            correctAnswer = "H2O", optionA = "O2", optionB = "H2O", 
            optionC = "CO2", optionD = "NaCl", difficulty = "EASY",
            nextReviewTimestamp = currentTime
        )
    )
}
```

### 4. After Build Success

Once the build succeeds:
1. Add more flashcards incrementally
2. Implement the UI screens one by one
3. Add WorkManager notifications
4. Polish the UI

## Testing the Build

After fixing, verify with:
```bash
.\gradlew.bat test
```

All unit tests should pass.

## Common Errors and Fixes

### Error: "Cannot add extension with name 'kotlin'"
**Fix**: Remove `ksp` plugin, use `kapt` instead

### Error: "unexpected jvm signature V"
**Fix**: All Room DAO suspend functions should NOT return Int/Long for updates. Return Unit instead.

### Error: "Dao class must be annotated with @Dao"
**Fix**: Ensure all DAO interfaces have `@Dao` annotation (already fixed in our files)

### Error: Database migration
**Fix**: We're using `fallbackToDestructiveMigration()` for development. Remove before production.

## Next Steps After Build Works

1. ✅ Test database initialization
2. ✅ Run unit tests
3. ⬜ Create enhanced ViewModels
4. ⬜ Update UI layouts
5. ⬜ Add WorkManager
6. ⬜ Implement settings screen
7. ⬜ Add animations
8. ⬜ Final polish

## Need Help?

Check `IMPLEMENTATION_GUIDE.md` for complete details on all remaining features.

