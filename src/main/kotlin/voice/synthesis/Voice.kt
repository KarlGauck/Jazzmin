package org.example.voice.synthesis

object Voice {

    fun speak(message: String) {
        val command = arrayOf("bin/python", "src/main/kotlin/voice/synthesis/tts.py", message)
        val runtime = Runtime.getRuntime().exec(command)
        runtime.waitFor()
    }

}