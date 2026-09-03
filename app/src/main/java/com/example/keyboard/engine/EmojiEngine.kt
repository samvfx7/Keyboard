package com.example.keyboard.engine

data class EmojiItem(val symbol: String, val category: String, val keywords: List<String>)

class EmojiEngine {

    val emojiDatabase = listOf(
        // Smileys & Emotion
        EmojiItem("😂", "Smileys", listOf("hilarious", "lol", "laugh", "funny", "haha", "crying")),
        EmojiItem("😊", "Smileys", listOf("happy", "smile", "joy", "good", "nice", "content")),
        EmojiItem("😍", "Smileys", listOf("love", "heart", "beautiful", "gorgeous", "cute", "adore")),
        EmojiItem("😎", "Smileys", listOf("cool", "sunglasses", "awesome", "chill", "boss")),
        EmojiItem("😭", "Smileys", listOf("cry", "sad", "tear", "sobbing", "upset")),
        EmojiItem("🔥", "Smileys", listOf("fire", "lit", "hot", "flame", "trend")),
        EmojiItem("❤️", "Smileys", listOf("love", "heart", "red", "care", "like")),
        EmojiItem("👍", "Smileys", listOf("yes", "thumb", "good", "agree", "ok", "cool")),
        EmojiItem("🎉", "Smileys", listOf("party", "celebrate", "congrats", "birthday", "cheers")),
        EmojiItem("🤔", "Smileys", listOf("think", "wonder", "hmm", "curious", "question")),

        // Animals & Nature
        EmojiItem("🐶", "Animals", listOf("dog", "puppy", "pet", "bark")),
        EmojiItem("🐱", "Animals", listOf("cat", "kitten", "pet", "meow")),
        EmojiItem("🦁", "Animals", listOf("lion", "king", "roar", "wild")),
        EmojiItem("🦄", "Animals", listOf("unicorn", "magic", "fantasy")),
        EmojiItem("🌸", "Animals", listOf("flower", "spring", "cherry", "blossom")),

        // Food & Drink
        EmojiItem("☕", "Food", listOf("coffee", "morning", "tea", "espresso", "drink", "cafe")),
        EmojiItem("🍕", "Food", listOf("pizza", "cheese", "food", "dinner", "snack")),
        EmojiItem("🍔", "Food", listOf("burger", "hamburger", "fastfood", "lunch")),
        EmojiItem("🌮", "Food", listOf("taco", "mexican", "food")),
        EmojiItem("🍺", "Food", listOf("beer", "cheers", "drink", "bar", "pub")),

        // Activities & Travel
        EmojiItem("⚽", "Activities", listOf("soccer", "football", "ball", "game", "sport")),
        EmojiItem("🏀", "Activities", listOf("basketball", "hoop", "sport")),
        EmojiItem("🚀", "Activities", listOf("rocket", "space", "fast", "launch", "moon")),
        EmojiItem("✈️", "Activities", listOf("airplane", "flight", "travel", "trip", "vacation")),
        EmojiItem("🚗", "Activities", listOf("car", "drive", "ride", "auto")),

        // Objects & Symbols
        EmojiItem("📱", "Objects", listOf("phone", "mobile", "cell", "app")),
        EmojiItem("💻", "Objects", listOf("laptop", "computer", "code", "work")),
        EmojiItem("🔒", "Objects", listOf("lock", "privacy", "secure", "key", "password")),
        EmojiItem("⭐", "Symbols", listOf("star", "gold", "top", "favorite")),
        EmojiItem("✅", "Symbols", listOf("check", "done", "correct", "yes"))
    )

    /**
     * Contextual emoji predictor. Returns matching emoji symbols based on typed words or sentences.
     */
    fun predictEmojiForText(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.lowercase().split("\\s+".toRegex())
        val matched = mutableSetOf<String>()

        for (word in words.takeLast(3)) {
            val cleanWord = word.replace("[^a-zA-Z]".toRegex(), "")
            if (cleanWord.length >= 3) {
                emojiDatabase.forEach { emoji ->
                    if (emoji.keywords.any { it.startsWith(cleanWord) || cleanWord.startsWith(it) }) {
                        matched.add(emoji.symbol)
                    }
                }
            }
        }
        return matched.take(4)
    }

    fun getEmojisByCategory(category: String): List<String> {
        return emojiDatabase.filter { it.category.equals(category, ignoreCase = true) }.map { it.symbol }
    }

    fun searchEmojis(query: String): List<String> {
        if (query.isBlank()) return emojiDatabase.map { it.symbol }.distinct().take(30)
        val qLower = query.lowercase().trim()
        return emojiDatabase
            .filter { emoji -> emoji.keywords.any { it.contains(qLower) } }
            .map { it.symbol }
            .distinct()
    }
}
