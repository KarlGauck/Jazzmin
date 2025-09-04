package org.example.voice.synthesis

object Voice {

    fun speak(message: String) {
        val command = arrayOf("bin/python", "tts.py", message)
        println("start")
        val runtime = Runtime.getRuntime().exec(command)
        runtime.waitFor()
        println("finished")
    }

}