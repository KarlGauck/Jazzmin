package org.example.chatmodel

import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object ChatModel {

    enum class MODEL
        (val handle: String)
    {
        DEEPSEEK("deepseek-r1:7b"),
        TINYDOLPHIN("tinydolphin")
    }

    val MODEL_TYPE = MODEL.DEEPSEEK
    val OLLAMA_URL = "http://127.0.0.1:11434"
    val TEMPERATURE = 1.0
    val THINKING = false

    val SYSTEM_PROMPT = "Answer in one flowing text without special formatting characters and be as concise and short as possible"

    val model: OllamaStreamingChatModel = OllamaStreamingChatModel.builder()
        .baseUrl(OLLAMA_URL)
        .temperature(TEMPERATURE)
        .modelName(MODEL_TYPE.handle)
        .think(THINKING)
        .build()

    fun message(message: String): Flow<String?> {
        val messageFlow = flow <String?> {
            val queue = mutableListOf<String?>()

            val response = model.chat(listOf(
                SystemMessage(SYSTEM_PROMPT),
                UserMessage(message)
            ), object: StreamingChatResponseHandler {
                override fun onPartialResponse(p0: String?) {
                    if (p0 == null)
                        return
                    queue.addLast(p0)
                }

                override fun onCompleteResponse(p0: ChatResponse?) {
                    queue.addLast(null)
                }

                override fun onError(p0: Throwable?) {
                    queue.addLast(null)
                }
            })

            while (true) {
                delay(20)
                if (queue.isEmpty())
                    continue

                while (queue.isNotEmpty()) {
                    val token = queue.removeFirst()
                    emit(token)
                }
            }
        }

        return messageFlow
    }

}