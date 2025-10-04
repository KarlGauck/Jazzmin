package org.example.chatmodel

import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage

data class ChatHistory (val systemMessage: SystemMessage, var messages: MutableList<ChatMessage>)