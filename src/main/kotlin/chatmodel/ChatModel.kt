package org.example.chatmodel

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
        TINYDOLPHIN("tinydolphin")
    }

    val model: OllamaStreamingChatModel = OllamaStreamingChatModel.builder()
        .baseUrl("http://127.0.0.1:11434")
        .temperature(1.0)
        .modelName(MODEL.DEEPSEEK.handle)
        .think(false)
        .build()

    fun message(scope: CoroutineScope, message: String): Flow<String?> {
        val messageFlow = MutableSharedFlow<String?>(extraBufferCapacity = 64)
        val queue = mutableListOf<String?>()

        val job = scope.launch {
            model.chat(listOf(
                SystemMessage("Answer in one flowing text without special formatting characters and be as concise and short as possible"),
                UserMessage(message)
            ), object: StreamingChatResponseHandler {
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