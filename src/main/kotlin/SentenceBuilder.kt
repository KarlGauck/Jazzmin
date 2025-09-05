package org.example

class SentenceBuilder {

    private var phrase = ""
    private var delimiters = arrayOf('.', '?', '!')

    private fun String.containsEndOfSentence(): Boolean = this.any { delimiters.contains(it) }

    fun addTokenUntilSentence(token: String): Boolean {
        phrase += token
        return token.containsEndOfSentence()
    }

    fun popSentence(): String {
        val sentence = phrase
        phrase = ""
        return sentence
    }

}