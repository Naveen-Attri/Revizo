package com.example.revizo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.revizo.data.dao.*
import com.example.revizo.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Flashcard::class,
        UserStats::class,
        Deck::class,
        Tag::class,
        FlashcardTagCrossRef::class,
        BattleHistory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flashcardDao(): FlashcardDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun deckDao(): DeckDao
    abstract fun tagDao(): TagDao
    abstract fun flashcardTagDao(): FlashcardTagDao
    abstract fun battleHistoryDao(): BattleHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "memory_battle_database"
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val flashcardDao = database.flashcardDao()
            val userStatsDao = database.userStatsDao()
            val deckDao = database.deckDao()
            val tagDao = database.tagDao()
            val flashcardTagDao = database.flashcardTagDao()

            // Initialize user stats
            userStatsDao.insertUserStats(UserStats())

            // Create default decks
            val decks = getDefaultDecks()
            deckDao.insertDecks(decks)

            // Create default tags
            val tags = getDefaultTags()
            tagDao.insertTags(tags)

            // Insert sample flashcards
            val flashcards = getSampleFlashcards()
            flashcardDao.insertFlashcards(flashcards)

            // Associate flashcards with tags
            val flashcardTagRefs = getFlashcardTagReferences()
            flashcardTagDao.insertFlashcardTags(flashcardTagRefs)
        }

        private fun getDefaultDecks(): List<Deck> {
            return listOf(
                Deck(id = 1, name = "Data Structures & Algorithms", description = "CS fundamentals", colorHex = "#6200EE", isDefault = true),
                Deck(id = 2, name = "Mathematics", description = "Math concepts", colorHex = "#03DAC5", isDefault = true),
                Deck(id = 3, name = "Science", description = "General science", colorHex = "#018786", isDefault = true),
                Deck(id = 4, name = "Physics", description = "Physics concepts", colorHex = "#B00020", isDefault = true),
                Deck(id = 5, name = "Chemistry", description = "Chemistry basics", colorHex = "#FF6F00", isDefault = true)
            )
        }

        private fun getDefaultTags(): List<Tag> {
            return listOf(
                Tag(id = 1, name = "arrays"),
                Tag(id = 2, name = "trees"),
                Tag(id = 3, name = "graphs"),
                Tag(id = 4, name = "sorting"),
                Tag(id = 5, name = "searching"),
                Tag(id = 6, name = "algebra"),
                Tag(id = 7, name = "geometry"),
                Tag(id = 8, name = "astronomy"),
                Tag(id = 9, name = "biology"),
                Tag(id = 10, name = "mechanics"),
                Tag(id = 11, name = "periodic-table"),
                Tag(id = 12, name = "reactions")
            )
        }

        private fun getFlashcardTagReferences(): List<FlashcardTagCrossRef> {
            return listOf(
                // Flashcard 1-5: DSA deck
                FlashcardTagCrossRef(1, 1), // arrays
                FlashcardTagCrossRef(1, 5), // searching
                FlashcardTagCrossRef(2, 2), // trees
                FlashcardTagCrossRef(3, 3), // graphs
                FlashcardTagCrossRef(4, 4), // sorting
                FlashcardTagCrossRef(5, 1), // arrays

                // Flashcard 6-10: Math deck
                FlashcardTagCrossRef(6, 6), // algebra
                FlashcardTagCrossRef(7, 7), // geometry
                FlashcardTagCrossRef(8, 6), // algebra
                FlashcardTagCrossRef(9, 7), // geometry
                FlashcardTagCrossRef(10, 6), // algebra

                // Flashcard 11-20: Science deck
                FlashcardTagCrossRef(11, 8), // astronomy
                FlashcardTagCrossRef(12, 11), // periodic-table
                FlashcardTagCrossRef(13, 9), // biology
                FlashcardTagCrossRef(14, 9), // biology
                FlashcardTagCrossRef(15, 11), // periodic-table

                // Flashcard 21-30: Physics deck
                FlashcardTagCrossRef(21, 10), // mechanics
                FlashcardTagCrossRef(22, 10), // mechanics

                // Flashcard 31-40: Chemistry deck
                FlashcardTagCrossRef(31, 11), // periodic-table
                FlashcardTagCrossRef(32, 12) // reactions
            )
        }

        private fun getSampleFlashcards(): List<Flashcard> {
            val currentTime = System.currentTimeMillis()
            return listOf(
                // Science - Easy
                Flashcard(
                    subject = "Science",
                    question = "What planet is known as the Red Planet?",
                    correctAnswer = "Mars",
                    optionA = "Mars",
                    optionB = "Venus",
                    optionC = "Jupiter",
                    optionD = "Saturn",
                    difficulty = "EASY"
                ),
                Flashcard(
                    subject = "Science",
                    question = "What is the chemical symbol for water?",
                    correctAnswer = "H2O",
                    optionA = "O2",
                    optionB = "H2O",
                    optionC = "CO2",
                    optionD = "NaCl",
                    difficulty = "EASY"
                ),
                Flashcard(
                    subject = "Science",
                    question = "Which gas do plants absorb during photosynthesis?",
                    correctAnswer = "Carbon Dioxide",
                    optionA = "Oxygen",
                    optionB = "Nitrogen",
                    optionC = "Carbon Dioxide",
                    optionD = "Hydrogen",
                    difficulty = "EASY"
                ),

                Flashcard(
                    subject = "Science",
                    question = "Which part of the cell contains genetic material?",
                    correctAnswer = "Nucleus",
                    optionA = "Mitochondria",
                    optionB = "Ribosome",
                    optionC = "Nucleus",
                    optionD = "Cytoplasm",
                    difficulty = "MEDIUM"
                ),

                Flashcard(
                    subject = "Science",
                    question = "Which planet is the largest in our solar system?",
                    correctAnswer = "Jupiter",
                    optionA = "Earth",
                    optionB = "Jupiter",
                    optionC = "Neptune",
                    optionD = "Saturn",
                    difficulty = "EASY"
                ),

                Flashcard(
                    subject = "Science",
                    question = "What is the atomic number of Oxygen?",
                    correctAnswer = "8",
                    optionA = "6",
                    optionB = "7",
                    optionC = "8",
                    optionD = "16",
                    difficulty = "MEDIUM"
                ),
                Flashcard(
                    subject = "Math",
                    question = "What is the value of x in 2x + 6 = 14?",
                    correctAnswer = "4",
                    optionA = "3",
                    optionB = "4",
                    optionC = "5",
                    optionD = "6",
                    difficulty = "EASY"
                ),

                Flashcard(
                    subject = "Math",
                    question = "What is the area of a circle formula?",
                    correctAnswer = "πr²",
                    optionA = "2πr",
                    optionB = "πr²",
                    optionC = "πd",
                    optionD = "r²",
                    difficulty = "MEDIUM"
                ),

                Flashcard(
                    subject = "Math",
                    question = "What is the sum of interior angles of a triangle?",
                    correctAnswer = "180°",
                    optionA = "90°",
                    optionB = "180°",
                    optionC = "270°",
                    optionD = "360°",
                    difficulty = "EASY"
                ),
                Flashcard(
                    subject = "Physics",
                    question = "What is the formula for Force?",
                    correctAnswer = "F = ma",
                    optionA = "F = ma",
                    optionB = "F = mv",
                    optionC = "F = m/a",
                    optionD = "F = v/a",
                    difficulty = "EASY"
                ),

                Flashcard(
                    subject = "Physics",
                    question = "What is the SI unit of velocity?",
                    correctAnswer = "m/s",
                    optionA = "m/s",
                    optionB = "km/h",
                    optionC = "m²",
                    optionD = "N",
                    difficulty = "MEDIUM"
                ),
                Flashcard(
                    subject = "Chemistry",
                    question = "Which element has the chemical symbol 'Na'?",
                    correctAnswer = "Sodium",
                    optionA = "Nitrogen",
                    optionB = "Sodium",
                    optionC = "Neon",
                    optionD = "Nickel",
                    difficulty = "EASY"
                ),

                Flashcard(
                    subject = "Chemistry",
                    question = "What type of reaction is combustion?",
                    correctAnswer = "Exothermic",
                    optionA = "Endothermic",
                    optionB = "Neutralization",
                    optionC = "Exothermic",
                    optionD = "Decomposition",
                    difficulty = "MEDIUM"
                ),
                Flashcard(
                    subject = "DSA",
                    question = "What is the worst-case time complexity of Binary Search?",
                    correctAnswer = "O(log n)",
                    optionA = "O(n)",
                    optionB = "O(log n)",
                    optionC = "O(n²)",
                    optionD = "O(1)",
                    difficulty = "MEDIUM"
                ),

                Flashcard(
                    subject = "DSA",
                    question = "Which sorting algorithm has average time complexity O(n log n)?",
                    correctAnswer = "Merge Sort",
                    optionA = "Bubble Sort",
                    optionB = "Selection Sort",
                    optionC = "Merge Sort",
                    optionD = "Insertion Sort",
                    difficulty = "HARD"
                ),

                Flashcard(
                    subject = "DSA",
                    question = "Which data structure uses FIFO principle?",
                    correctAnswer = "Queue",
                    optionA = "Stack",
                    optionB = "Queue",
                    optionC = "Tree",
                    optionD = "Graph",
                    difficulty = "EASY"
                )
            )
        }
    }
}

