package org.example.voice.recognition

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.example.chatmodel.ChatModel
import org.testcontainers.shaded.org.apache.commons.lang3.mutable.Mutable
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

object VoiceRecognition {

    val SAMPLE_RATE = 16_000f //DONT CHANGE, THIS IS VOSK-SPECIFIC
    val VOICE_MODEL_PATH = "vosk_models/vosk-model-de-0.21"

    val vosk_model = Model(VOICE_MODEL_PATH)
    val recognizer = Recognizer(vosk_model, SAMPLE_RATE)

    init {
        LibVosk.setLogLevel(LogLevel.WARNINGS)
    }

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

    fun jsonresultToMessage(json: String): String = json.split("\"").getOrElse(3) {""}

    suspend fun listen(scope: CoroutineScope): MutableSharedFlow<String> {
        val mic = buildAudioInputStream()

        var nbytes: Int
        val bytes = ByteArray(4096)

        var last = ""
        val audioFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)

        scope.launch {
            while (true) {
                nbytes = mic?.read(bytes, 0, bytes.size) ?: break
                if (nbytes < 0)
                    break

                if (nbytes > 0) {
                    if (recognizer.acceptWaveForm(bytes, nbytes)) {
                        if (last != recognizer.result)
                            audioFlow.emit(jsonresultToMessage(last))
                    } else {
                        last = recognizer.partialResult
                    }
                }
                delay(20)
            }
        }

        return audioFlow
    }

}