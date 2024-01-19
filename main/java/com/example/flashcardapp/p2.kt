package com.example.flashcardapp

val sepCard = "|"
val sepTag = ","

data class TaggedFlashCard(val front: String, val back: String, val tags: List<String>) {
    // this function checks if the card is tagged with a tag
    fun isTagged(tag: String): Boolean {
        return tags.contains(tag)
    }

    // this function converts the flash card to the "front|back|tag1,tag2,.. string format
    fun fileFormat(): String {
        val tagString = tags.joinToString(separator = sepTag)
        return "$front$sepCard$back$sepCard$tagString"
    }
}

val card1 = TaggedFlashCard("What is the capital of Italy?", "Rome", listOf("country", "capital"))
val card2 = TaggedFlashCard("Who is the 11th president of the U.S?", "James Polk", listOf("president"))
val card3 = TaggedFlashCard("Which team won the Fifa World Cup in 2022?", "Argentina", listOf("soccer", "teams"))
val card4 = TaggedFlashCard("Who is the 16th president of the U.S?", "Abraham Lincoln", listOf("president"))

val listAll = listOf(
    card1,
    card2,
    card3,
    card4,
)

data class PerfectSquaresDeck(
    private val numOfCards: Int = 10,
    private val frontOrBack: Boolean = true,
    private val deck: List<Int> = List(numOfCards) { it + 1 },
) : IDeck {

    // override function to get current state of the deck
    override fun getState(): DeckState {
        return when {
            deck.isEmpty() -> DeckState.EXHAUSTED
            frontOrBack -> DeckState.QUESTION
            else -> DeckState.ANSWER
        }
    }

    // override function to get the text of the current card in deck
    override fun getText(): String? {
        return when (getState()) {
            DeckState.EXHAUSTED -> null
            DeckState.QUESTION -> "${deck[0]}^2 = ?"
            DeckState.ANSWER -> "${deck[0] * deck[0]}"
        }
    }

    // override function to get the size of the deck
    override fun getSize(): Int {
        return numOfCards
    }

    // override function to flip the deck to the opposite state (question to answer or answer to question)
    override fun flip(): IDeck {
        return when (getState() == DeckState.QUESTION) {
            true -> copy(frontOrBack = !frontOrBack)
            false -> this
        }
    }

    // override function to move to next card whether if answer is correct or not
    override fun next(correct: Boolean): IDeck {
        if (getState() != DeckState.ANSWER) {
            return this
        }
        val updatedDeck = when (correct) {
            false -> deck.drop(1) + deck[0]
            true -> deck.drop(1)
        }
        // creates a copy of the updated deck with frontOrBeck set to true (indicating the deck is in the front question state)
        return copy(deck = updatedDeck, frontOrBack = true)
    }
}

enum class DeckState {
    EXHAUSTED,
    QUESTION,
    ANSWER,
}

interface IDeck {
    // The state of the deck
    fun getState(): DeckState

    // The currently visible text
    // (or null if exhausted)
    fun getText(): String?

    // The number of question/answer pairs
    // (does not change when question are
    // cycled to the end of the deck)
    fun getSize(): Int

    // Shifts from question -> answer
    // (if not QUESTION state, returns the same IDeck)
    fun flip(): IDeck

    // Shifts from answer -> next question (or exhaustion);
    // if the current question was correct it is discarded,
    // otherwise cycled to the end of the deck
    // (if not ANSWER state, returns the same IDeck)
    fun next(correct: Boolean): IDeck
}

data class TFCListDeck(
    private val listFlashCards: List<TaggedFlashCard>,
    private val frontOrBack: Boolean = true,
    private val initialCardCount: Int = listFlashCards.size,
) : IDeck {
    // gets the current state of the deck
    override fun getState(): DeckState {
        return when {
            listFlashCards.isEmpty() -> DeckState.EXHAUSTED
            frontOrBack -> DeckState.QUESTION
            else -> DeckState.ANSWER
        }
    }

    // override function that gets the text of the current card in the deck
    override fun getText(): String? {
        return when (getState()) {
            DeckState.EXHAUSTED -> null
            DeckState.QUESTION -> listFlashCards[0].front
            DeckState.ANSWER -> listFlashCards[0].back
        }
    }

    // ovverride function that gets the size of the deck
    // Not the proper way to do it as it doesn't get remaining cards, but rather
    // the initial size of the deck
    override fun getSize(): Int = initialCardCount

    // override function that flips the deck to the opposite state
    // (question to answer or answer to question)
    override fun flip(): IDeck {
        return when (getState()) {
            DeckState.QUESTION -> copy(frontOrBack = false)
            else -> this
        }
    }

    // helper function that updates the list of the flash cards based on
    // the answer being correct or not
    private fun helperFunNext(correct: Boolean, list: List<TaggedFlashCard>): List<TaggedFlashCard> {
        return when (correct) {
            true -> list.drop(1)
            false -> list.drop(1) + list[0]
        }
    }

    // override function to move to the enxt card in the deck whether the answer was correct or not
    override fun next(correct: Boolean): IDeck {
        return when (getState()) {
            DeckState.ANSWER -> copy(listFlashCards = helperFunNext(correct, listFlashCards), frontOrBack = true)
            else -> this
        }
    }
}