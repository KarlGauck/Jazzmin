package org.example

import com.harium.hci.espeak.Espeak
import com.harium.hci.espeak.Voice
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.example.chatmodel.ChatModel
import org.example.voice.recognition.VoiceRecognition
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import javax.sound.sampled.*
import kotlin.concurrent.thread



fun messageOllama(model: OllamaStreamingChatModel, message: String, speak: Espeak) {

}

fun main(): Unit = runBlocking {
    val voice = Voice()
    voice.name = "en-us"
    voice.amplitude = 100
    voice.pitch = 30
    voice.speed = 170
    voice.setVariant(false, 3)

    val speak = Espeak(voice)

    /*
    ChatModel.message(message).takeWhile { it != null }.collect { token ->
        println("lel $token")
    }
    */

    VoiceRecognition.listen().collect(::println)

}