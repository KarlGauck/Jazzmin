package org.example

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.example.brains.InteractionManager
import org.example.chatmodel.ChatModel
import org.example.chatmodel.ChatModel.MODEL
import org.example.voice.recognition.VoiceRecognition
import org.example.voice.synthesis.Voice

suspend fun listen(scope: CoroutineScope, queryPhraseChannel: Channel<String>) {
    VoiceRecognition.listen(scope).collect { queryPhrase ->
        queryPhraseChannel.send(queryPhrase)
        delay(20)
    }
}

suspend fun messageChatty(scope: CoroutineScope, queryPhraseChannel: Channel<String>, talkingPhraseChannel: Channel<String>) {
    for (message in queryPhraseChannel) {
        if (message.isEmpty())
            continue
        val sentenceBuilder = SentenceBuilder()
        scope.launch {
            var totalResponse = ""
            InteractionManager.query(scope, message).collect { token ->
                val newSentence = token?.let { sentenceBuilder.addTokenUntilSentence(token) } ?: false
                if (newSentence) {
                    val sentence = sentenceBuilder.popSentence()
                    talkingPhraseChannel.send(sentence)
                }
                if (token == null) {
                    val res = coroutineContext[Job]?.cancel()
                    println("finished")
                    InteractionManager.response(totalResponse)
                } else {
                    totalResponse += token
                }
            }
        }
    }
}

fun main(): Unit = runBlocking {
    /*
    val MODEL_TYPE = MODEL.PHI3
    val OLLAMA_URL = "http://127.0.0.1:11434"
    val TEMPERATURE = 1.0
    val THINKING = false

    val model: OllamaChatModel = OllamaChatModel.builder()
        .baseUrl(OLLAMA_URL)
        .temperature(TEMPERATURE)
        .modelName(MODEL_TYPE.handle)
        .think(THINKING)
        .build()

    val response = model.chat(listOf(
        UserMessage("Hi, my name is Karl"),
        AiMessage("Hello Karl, how can I help you?"),
        UserMessage("Do you remember my name?")
    ))

    println(response.aiMessage().text())
    */

    val queryPhraseChannel = Channel<String>(Channel.BUFFERED)
    val talkingPhraseChannel = Channel<String>(Channel.BUFFERED)

    coroutineScope {
        launch {
            for (message in talkingPhraseChannel) {
                Voice.speak(message)
            }
        }

        launch {
            messageChatty(this, queryPhraseChannel, talkingPhraseChannel)
        }

        launch {
            listen(this, queryPhraseChannel)
        }
    }
}
