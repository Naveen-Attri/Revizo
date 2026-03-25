# CV Points – Memory Battle Android App

Four quantified highlights from this project, suitable for a CV or resume.

---

- **Implemented the SM-2 Spaced Repetition Algorithm** in Kotlin to dynamically schedule flashcard reviews across 40+ pre-loaded cards in 5 subject decks. The algorithm adjusts each card's ease factor (bounded between 1.3 and 2.5) and interval multiplier after every answer, ensuring cards are shown at the optimal moment for long-term retention. The implementation was validated with 13 dedicated unit tests covering interval progression, ease-factor edge cases, and reset logic, achieving full coverage of the core scheduling logic.

- **Architected a production-ready Android app using MVVM and Android Jetpack**, integrating Room (SQLite, 6-table schema), ViewModel, LiveData, and Kotlin Coroutines to achieve clean separation of concerns and fully asynchronous, non-blocking data access. The repository pattern centralises all business logic—answer processing, streak validation, and XP calculation—in a single layer, reducing UI-to-database coupling and making every database operation lifecycle-safe across 4 distinct Activities.

- **Engineered a gamified XP and progression system** that awards 10–40 XP per card based on difficulty (Easy / Medium / Hard), applies streak bonuses of +10 XP for 3 consecutive correct answers and +25 XP for 5 consecutive correct answers, and doubles rewards during Boss Rounds (2× XP multiplier). The level-up logic (500 XP per level) and all bonus calculations are covered by 10+ unit tests, and every session's XP history is persisted in a BattleHistory table so users can track improvement over time.

- **Built a real-time Battle Mode with coroutine-driven countdown timers and reactive state management**, delivering a complete question-answer flow within a 15-second per-question limit. `BattleState`—a single LiveData-backed data class tracking current question index, time remaining, answer correctness, consecutive streak, XP earned, and completion status—drives the entire UI reactively, while session metrics (accuracy, average response time, total XP, deck identifier) are written to the database at battle end, enabling a detailed post-battle analytics screen.
