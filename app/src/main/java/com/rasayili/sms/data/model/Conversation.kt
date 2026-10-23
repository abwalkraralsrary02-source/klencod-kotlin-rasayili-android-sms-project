
package com.rasayili.sms.data.model

data class Conversation(
    val threadId: Long,
    val address: String,
    val contactName: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val messageCount: Int = 0
)