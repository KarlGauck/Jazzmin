package org.example.brains

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.example.chatmodel.ChatHistory
import org.example.chatmodel.ChatModel

/**
 * This class manages history and memory, personality etc.
 */
object InteractionManager {
    val SYSTEM_PROMPT = "Act as if you are a human being with personality but be helpful and kind. Answer questions as short and on point as possible"
    val history = ChatHistory(SystemMessage(SYSTEM_PROMPT), mutableListOf())

    fun query (scope: CoroutineScope, message: String): Flow<String?> {
        val flow = ChatModel.message(scope, history, message)
        history.messages += UserMessage(message)

        return flow
    }

    fun response (response: String) {
        history.messages += AiMessage(response)
    }

}