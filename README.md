# Memory Battle - Gamified Flashcard Learning App

A production-ready, offline-first Android learning app that uses spaced repetition (SM-2 algorithm) to help users master any subject through an engaging battle-style interface.

## 🎯 Features

### Core Functionality
- **SM-2 Spaced Repetition**: Scientifically-proven algorithm for optimal review timing
- **Deck Management**: Create, edit, and organize flashcards into decks
- **Tag System**: Multi-tag support for advanced organization and filtering
- **Battle Mode**: Gamified 10-question battles with XP rewards
- **Daily Challenge**: Special 5-card challenge with bonus XP (once per day)
- **Boss Rounds**: Hard questions with double XP after main battle

### Progression System
- **XP & Leveling**: Earn XP based on difficulty (Easy: 10, Medium: 20, Hard: 40)
- **Streak Bonuses**: 
  - 3 correct in a row → +10 XP
  - 5 correct in a row → +25 XP + Shield (ignore next wrong answer)
- **Level System**: 500 XP per level with animated progress
- **Daily Streaks**: Track consecutive days of studying

### Analytics & Stats
- **Accuracy Tracking**: Overall and per-deck accuracy percentages
- **Response Time**: Average time to answer questions
- **Weakest Tags**: Identify areas needing improvement
- **XP History**: 30-day progress visualization
- **Battle History**: Complete record of past sessions

### User Experience
- **Dark Theme**: Gaming-style UI optimized for extended study sessions
- **Animations**: 
  - Card flip transitions
  - Correct/incorrect feedback (green glow/shake)
  - Level-up confetti
  - Circular countdown timer
- **Accessibility**: Proper contentDescription, 48dp touch targets, readable fonts
- **Empty States**: Helpful CTAs when no cards are available

### Smart Features
- **Due Card System**: Only shows cards that need review
- **Practice Mode**: Study any deck even when no cards are due
- **Daily Notifications**: WorkManager reminds you when cards are due
- **Settings**:
  - Toggle notifications
  - Set battle length (5/10/15 questions)
  - Daily target configuration
  - Progress reset with confirmation
  - Export/Import decks as JSON

### Onboarding
- Welcome flow with interest selection
- Pre-populated with 40 high-quality flashcards across 5 subjects:
  - Data Structures & Algorithms
  - Mathematics
  - Science
  - Physics
  - Chemistry

## 🏗️ Architecture

### MVVM Pattern
```
View (Activity/Fragment) → ViewModel → Repository → DAO → Room Database
                              ↓
                         LiveData/Flow
```

### Tech Stack
- **Language**: Kotlin
- **UI**: XML Layouts (Material Design)
- **Architecture**: MVVM
- **Database**: Room (SQLite)
- **Async**: Coroutines + Flow
- **DI**: Manual (easily upgradeable to Hilt)
- **Background**: WorkManager
- **Testing**: JUnit, Room Testing, Coroutines Test

### Database Schema
```
┌─────────────┐
│ Flashcards  │ (40 sample cards with SM-2 fields)
├─────────────┤
│ deckId      │ → Decks
│ question    │
│ difficulty  │
│ easeFactor  │ (SM-2)
│ intervalDays│ (SM-2)
│ nextReview  │ (SM-2)
└─────────────┘

┌─────────────┐
│ UserStats   │ (XP, level, streaks, settings)
├─────────────┤
│ totalXp     │
│ currentLevel│
│ dailyStreak │
│ battleLength│
└─────────────┘

┌─────────────┐        ┌──────────────────┐
│ Decks       │        │ FlashcardTagRef  │
│             │        │ (Many-to-Many)   │
│ Tags        │        └──────────────────┘
│             │
│BattleHistory│
└─────────────┘
```

## 📊 SM-2 Algorithm

The app implements the SuperMemo 2 (SM-2) spaced repetition algorithm:

1. **Quality Rating**: 0-5 based on correctness and response time
2. **Interval Calculation**: 
   - First review: 1 day
   - Second review: 6 days
   - Subsequent: Previous interval × 2.5
3. **Ease Factor**: Adjusts based on performance (minimum 1.3)
4. **Reset on Failure**: Wrong answers reset to beginning

### Quality Ratings
- **5**: Perfect recall (fast)
- **4**: Correct with hesitation
- **3**: Correct but required effort
- **2**: Wrong but close
- **1**: Wrong with partial knowledge
- **0**: Complete guess

## 🧪 Testing

### Unit Tests
- `SpacedRepetitionSchedulerTest`: 13 test cases covering:
  - Interval progression
  - Ease factor adjustments
  - Reset logic
  - Quality calculation
  - Edge cases

- `XPCalculatorTest`: 10+ test cases for:
  - XP calculation by difficulty
  - Streak bonuses
  - Level progression
  - Accuracy calculations

- `DateUtilsTest`: Time and date utilities

### Running Tests
```bash
./gradlew test
```

### CI/CD
GitHub Actions workflow automatically runs tests on every push/PR.

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11
- Android SDK 24+ (target: 36)

### Building
```bash
# Clone the repository
git clone https://github.com/yourusername/memory-battle.git

# Open in Android Studio
# Let Gradle sync

# Run on device/emulator
./gradlew installDebug

# Or use Android Studio Run button
```

### First Launch
1. App pre-populates with 40 flashcards across 5 decks
2. Choose battle length in settings (default: 10)
3. Start your first battle!
4. Cards are due immediately - complete reviews to start spacing

## 📱 Screens

### Home Screen
- XP progress bar with current level
- Due cards counter
- Daily streak indicator
- "Start Battle" button
- "Daily Challenge" button (if available)
- "View Stats" button
- Deck list

### Battle Screen
- Question card with 4 options
- Circular 15-second countdown
- Correct/incorrect animations
- Streak counter
- Progress indicator (X/10)
- Boss round after 10 questions

### Stats Screen
- Total XP and level
- Accuracy percentage
- Total battles
- Average response time
- XP history chart (30 days)
- Weakest tags list

### Deck Management
- Create new deck
- Edit deck (name, color, description)
- View cards in deck
- Delete deck (with confirmation)

### Settings
- Notifications toggle
- Battle length (5/10/15)
- Daily target
- Reset progress
- Export/Import decks

## 🎨 Theming

Dark theme with game-like aesthetics:
- **Primary**: #6200EE (Purple)
- **Accent**: #03DAC5 (Teal)
- **Correct**: #4CAF50 (Green)
- **Incorrect**: #F44336 (Red)
- **Background**: #121212 (Dark)

## 📦 Project Structure
```
app/src/main/
├── java/com/example/revizo/
│   ├── data/
│   │   ├── dao/           # Room DAOs
│   │   ├── database/      # AppDatabase + sample data
│   │   ├── entity/        # Room entities
│   │   └── repository/    # Business logic layer
│   ├── ui/
│   │   ├── home/          # Home screen
│   │   ├── battle/        # Battle screen
│   │   ├── stats/         # Stats screen
│   │   ├── deck/          # Deck management
│   │   ├── settings/      # Settings screen
│   │   └── onboarding/    # First-run experience
│   ├── util/              # Utilities (SM-2, XP, Date)
│   └── worker/            # WorkManager for notifications
├── res/
│   ├── layout/            # XML layouts
│   ├── values/            # Colors, strings, themes
│   └── drawable/          # Icons and graphics
└── AndroidManifest.xml

app/src/test/
└── java/com/example/revizo/  # Unit tests
```

## 🔄 Future Enhancements (Not Implemented)

- AI-generated flashcards via OpenAI API
- Cloud sync with Firebase
- Multiplayer battles
- Leaderboards
- Achievement system
- Custom card templates
- Image support in cards
- Audio pronunciation
- Widget for quick study

## 📄 License

MIT License - See LICENSE file for details

## 👥 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 🐛 Known Issues

- None currently

## 💡 Tips for Users

1. **Review daily**: The SM-2 algorithm works best with consistent daily reviews
2. **Use tags**: Tag your weak areas to track improvement
3. **Daily challenge**: Complete it every day for maximum XP
4. **Boss rounds**: Worth double XP, so take your time!
5. **Streaks**: Build up your streak for bonus XP on every answer

## 📞 Support

For issues, questions, or suggestions:
- Open a GitHub issue
- Email: support@memorybattle.com

---

**Built with ❤️ for learners everywhere**

