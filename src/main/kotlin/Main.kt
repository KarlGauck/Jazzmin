package org.example

import com.harium.hci.espeak.Espeak
import com.harium.hci.espeak.Voice
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.util.concurrent.TimeUnit
import javax.sound.sampled.*
import kotlin.concurrent.thread

fun buildAudioInputStream(): TargetDataLine? {
    val audioFormat = AudioFormat(16000f, 16, 1, true, false)
    val info = DataLine.Info(TargetDataLine::class.java, audioFormat)
    if (!AudioSystem.isLineSupported(info)) {
        println("Line is not supported!")
        return null
    }
    val microphone = AudioSystem.getLine(info) as TargetDataLine
    microphone.open(audioFormat)
    microphone.start()
    return microphone
}

fun messageOllama(model: OllamaStreamingChatModel, message: String, speak: Espeak) {
    val response = model.chat(listOf(
        SystemMessage("Answer in one flowing text without special formatting characters and be as concise and short as possible"),
        UserMessage(message)
    ), object: StreamingChatResponseHandler {
        override fun onPartialResponse(p0: String?) {
            if (p0?.isEmpty() ?: return)
                return
            println(p0)
        }

        override fun onCompleteResponse(p0: ChatResponse?) {
            println(p0)
            //speak.speak(p0?.aiMessage()?.text())
            val command = arrayOf("./bin/python", "./tts.py", p0?.aiMessage()?.text() ?: "")
            val proc = Runtime.getRuntime().exec(command)
            val exit = proc.waitFor()
            println(exit)
        }

        override fun onError(p0: Throwable?) {

        }
    })
}


fun main()** {

    val MODEL = /*"tinydolphin" */ "deepseek-r1:7b"

    val model = OllamaStreamingChatModel.builder()
        .baseUrl("http://127.0.0.1:11434")
        .temperature(1.0)
        .modelName(MODEL)
        .think(false)
        .build()

    val voice = Voice()
    voice.name = "en-us"
    voice.amplitude = 100
    voice.pitch = 30
    voice.speed = 170
    voice.setVariant(false, 3)

    val speak = Espeak(voice)

    LibVosk.setLogLevel(LogLevel.DEBUG)
    val vosk_model = Model("vosk_models/vosk-model-small-en-us-0.15")
    val recognizer = Recognizer(vosk_model, 16000f)

    println("vosk initialized")

    var nbytes: Int


    val mic = buildAudioInputStream()// AudioSystem.getAudioInputStream(BufferedInputStream(FileInputStream("test.wav")))
    thread (start = true) {
        val bytes = ByteArray(4096)

        var last = ""
        while (true) {
            nbytes = mic?.read(bytes, 0, bytes.size) ?: break
            if (nbytes < 0)
                break

            if (nbytes > 0)
            if (recognizer.acceptWaveForm(bytes, nbytes)) {
                if (last != recognizer.result) {
                    val message = last.split("\"")[3]
                    if (message.isNotEmpty())
                        messageOllama(model, message, speak)
                    last = recognizer.result
                }
            } else {
                last = recognizer.partialResult
            }
        }
    }

}