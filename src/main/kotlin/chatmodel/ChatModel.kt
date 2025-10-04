package org.example.chatmodel

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

object ChatModel {

    enum class MODEL
        (val handle: String)
    {
        DEEPSEEK("deepseek-r1:7b"),
        TINYDOLPHIN("tinydolphin"),
        PHI3("phi3:3.8b-mini-4k-instruct-q4_0")
    }

    val MODEL_TYPE = MODEL.PHI3
    val OLLAMA_URL = "http://127.0.0.1:11434"
    val TEMPERATURE = 1.0
    val THINKING = false

    val model: OllamaStreamingChatModel = OllamaStreamingChatModel.builder()
        .baseUrl(OLLAMA_URL)
        .temperature(TEMPERATURE)
        .modelName(MODEL_TYPE.handle)
        .think(THINKING)
        .build()

    fun message(scope: CoroutineScope, chatHistory: ChatHistory, message: String): Flow<String?> {
        val messageFlow = MutableSharedFlow<String?>(extraBufferCapacity = 64)
        val queue = mutableListOf<String?>()

        val job = scope.launch {

            val messages = mutableListOf<ChatMessage>(chatHistory.systemMessage)
            messages.addAll(chatHistory.messages)

            for (message in messages) {
                if (message is SystemMessage)
                    println("System: ${message.text()}")
                if (message is UserMessage)
                    println("User: ${message.singleText()}")
                if (message is AiMessage)
                    println("Ai: ${message.text()}")
            }

            model.chat(
                messages
            , object: StreamingChatResponseHandler {
                override fun onPartialResponse(p0: String?) {
                    if (p0 == null)
                        return
                    queue.addLast(p0)
                }

                override fun onCompleteResponse(p0: ChatResponse?) {
                    queue.addLast(null)
                    coroutineContext[Job]?.cancel()
                }

                override fun onError(p0: Throwable?) {
                }
            })
        }


        scope.launch {
            outer@while (true) {
                delay(20)
                if (queue.isEmpty())
                    continue

                while (queue.isNotEmpty()) {
                    val token = queue.removeFirst()
                    messageFlow.emit(token)
                    if (token == null)
                        break@outer
                }
            }
        }

        return messageFlow
    }

}