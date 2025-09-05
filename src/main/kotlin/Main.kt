package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.example.chatmodel.ChatModel
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
            ChatModel.message(scope, message).collect { token ->
                val newSentence = token?.let { sentenceBuilder.addTokenUntilSentence(token) } ?: false
                if (newSentence) {
                    val sentence = sentenceBuilder.popSentence()
                    talkingPhraseChannel.send(sentence)
                }
                if (token == null) {
                    val res = coroutineContext[Job]?.cancel()
                }
            }
        }
    }
}

fun main(): Unit = runBlocking {
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
