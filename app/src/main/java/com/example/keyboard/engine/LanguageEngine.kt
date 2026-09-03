package com.example.keyboard.engine

data class DictWord(val word: String, val frequency: Int, val language: String = "en")

class LanguageEngine {

    // Pre-populated local dictionaries for offline privacy
    private val englishWords = listOf(
        DictWord("the", 100), DictWord("be", 95), DictWord("to", 90), DictWord("of", 88),
        DictWord("and", 85), DictWord("a", 84), DictWord("in", 82), DictWord("that", 80),
        DictWord("have", 78), DictWord("I", 77), DictWord("it", 75), DictWord("for", 74),
        DictWord("not", 72), DictWord("on", 70), DictWord("with", 68), DictWord("he", 66),
        DictWord("as", 65), DictWord("you", 64), DictWord("do", 62), DictWord("at", 60),
        DictWord("this", 58), DictWord("but", 56), DictWord("his", 55), DictWord("by", 54),
        DictWord("from", 52), DictWord("they", 50), DictWord("we", 48), DictWord("say", 46),
        DictWord("her", 45), DictWord("she", 44), DictWord("or", 42), DictWord("an", 40),
        DictWord("will", 38), DictWord("my", 36), DictWord("one", 35), DictWord("all", 34),
        DictWord("would", 32), DictWord("there", 30), DictWord("their", 28), DictWord("what", 26),
        DictWord("so", 25), DictWord("up", 24), DictWord("out", 23), DictWord("if", 22),
        DictWord("about", 21), DictWord("who", 20), DictWord("get", 19), DictWord("which", 18),
        DictWord("go", 17), DictWord("me", 16), DictWord("when", 15), DictWord("make", 14),
        DictWord("can", 13), DictWord("like", 12), DictWord("time", 11), DictWord("no", 10),
        DictWord("just", 9), DictWord("him", 8), DictWord("know", 7), DictWord("take", 6),
        DictWord("people", 5), DictWord("into", 5), DictWord("year", 5), DictWord("your", 5),
        DictWord("good", 5), DictWord("some", 5), DictWord("could", 5), DictWord("them", 5),
        DictWord("see", 5), DictWord("other", 5), DictWord("than", 5), DictWord("then", 5),
        DictWord("now", 5), DictWord("look", 5), DictWord("only", 5), DictWord("come", 5),
        DictWord("its", 5), DictWord("over", 5), DictWord("think", 5), DictWord("also", 5),
        DictWord("back", 5), DictWord("after", 5), DictWord("use", 5), DictWord("two", 5),
        DictWord("how", 5), DictWord("our", 5), DictWord("work", 5), DictWord("first", 5),
        DictWord("well", 5), DictWord("way", 5), DictWord("even", 5), DictWord("new", 5),
        DictWord("want", 5), DictWord("because", 5), DictWord("any", 5), DictWord("these", 5),
        DictWord("give", 5), DictWord("day", 5), DictWord("most", 5), DictWord("us", 5),
        DictWord("hello", 30), DictWord("thanks", 28), DictWord("keyboard", 25), DictWord("privacy", 25),
        DictWord("adaptive", 22), DictWord("intelligence", 20), DictWord("screen", 18), DictWord("message", 18),
        DictWord("today", 18), DictWord("tomorrow", 18), DictWord("yesterday", 15), DictWord("meeting", 15),
        DictWord("please", 20), DictWord("awesome", 15), DictWord("hilarious", 15), DictWord("don't", 25),
        DictWord("can't", 25), DictWord("won't", 22), DictWord("I'm", 30), DictWord("I'll", 22),
        DictWord("you're", 22), DictWord("they're", 20), DictWord("we're", 20), DictWord("that's", 22)
    )

    private val spanishWords = listOf(
        DictWord("de", 100, "es"), DictWord("la", 95, "es"), DictWord("que", 90, "es"), DictWord("el", 88, "es"),
        DictWord("en", 85, "es"), DictWord("y", 84, "es"), DictWord("a", 82, "es"), DictWord("los", 80, "es"),
        DictWord("del", 78, "es"), DictWord("se", 77, "es"), DictWord("las", 75, "es"), DictWord("por", 74, "es"),
        DictWord("un", 72, "es"), DictWord("para", 70, "es"), DictWord("con", 68, "es"), DictWord("no", 66, "es"),
        DictWord("una", 65, "es"), DictWord("su", 64, "es"), DictWord("al", 62, "es"), DictWord("lo", 60, "es"),
        DictWord("como", 58, "es"), DictWord("más", 56, "es"), DictWord("pero", 55, "es"), DictWord("sus", 54, "es"),
        DictWord("hola", 35, "es"), DictWord("gracias", 32, "es"), DictWord("buenos", 25, "es"), DictWord("días", 25, "es")
    )

    private val frenchWords = listOf(
        DictWord("de", 100, "fr"), DictWord("la", 95, "fr"), DictWord("le", 90, "fr"), DictWord("et", 88, "fr"),
        DictWord("les", 85, "fr"), DictWord("des", 84, "fr"), DictWord("en", 82, "fr"), DictWord("un", 80, "fr"),
        DictWord("du", 78, "fr"), DictWord("une", 77, "fr"), DictWord("est", 75, "fr"), DictWord("pour", 74, "fr"),
        DictWord("bonjour", 35, "fr"), DictWord("merci", 32, "fr"), DictWord("oui", 28, "fr"), DictWord("non", 28, "fr")
    )

    private val germanWords = listOf(
        DictWord("der", 100, "de"), DictWord("die", 95, "de"), DictWord("und", 90, "de"), DictWord("in", 88, "de"),
        DictWord("den", 85, "de"), DictWord("von", 84, "de"), DictWord("zu", 82, "de"), DictWord("das", 80, "de"),
        DictWord("mit", 78, "de"), DictWord("sich", 77, "de"), DictWord("des", 75, "de"), DictWord("auf", 74, "de"),
        DictWord("hallo", 35, "de"), DictWord("danke", 32, "de"), DictWord("bitte", 28, "de"), DictWord("gut", 28, "de")
    )

    private val contractionMap = mapOf(
        "dont" to "don't",
        "cant" to "can't",
        "wont" to "won't",
        "im" to "I'm",
        "ill" to "I'll",
        "youre" to "you're",
        "theyre" to "they're",
        "were" to "we're",
        "thats" to "that's",
        "isnt" to "isn't",
        "arent" to "aren't",
        "couldnt" to "couldn't",
        "wouldnt" to "wouldn't",
        "shouldnt" to "shouldn't",
        "whats" to "what's",
        "heres" to "here's",
        "theres" to "there's"
    )

    fun getDictionaryForLanguage(lang: String, mixedMode: Boolean = true): List<DictWord> {
        return when {
            mixedMode -> englishWords + spanishWords + frenchWords + germanWords
            lang == "es" -> spanishWords
            lang == "fr" -> frenchWords
            lang == "de" -> germanWords
            else -> englishWords
        }
    }

    fun getContraction(input: String): String? {
        return contractionMap[input.lowercase()]
    }

    fun searchPrefix(prefix: String, lang: String, mixedMode: Boolean = true, limit: Int = 10): List<DictWord> {
        if (prefix.isBlank()) return emptyList()
        val dict = getDictionaryForLanguage(lang, mixedMode)
        val pLower = prefix.lowercase()
        return dict
            .filter { it.word.lowercase().startsWith(pLower) }
            .sortedByDescending { it.frequency }
            .take(limit)
    }
}
