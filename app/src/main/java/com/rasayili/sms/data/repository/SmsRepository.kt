package com.rasayili.sms.data.repository

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import com.rasayili.sms.data.model.Conversation
import com.rasayili.sms.data.model.SmsMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsRepository(private val context: Context) {

    suspend fun sendSms(
        destinationAddress: String,
        messageText: String,
        subscriptionId: Int? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (subscriptionId != null && subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    context.getSystemService(SmsManager::class.java).createForSubscriptionId(subscriptionId)
                } else {
                    context.getSystemService(SmsManager::class.java)
                }
            } else {
                @Suppress("DEPRECATION")
                if (subscriptionId != null && subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                } else {
                    SmsManager.getDefault()
                }
            }

            val sentIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent("com.rasayili.sms.SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )

            val deliveryIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent("com.rasayili.sms.SMS_DELIVERED"),
                PendingIntent.FLAG_IMMUTABLE
            )

            val parts = smsManager.divideMessage(messageText)
            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent>().apply { repeat(parts.size) { add(sentIntent) } }
                val deliveryIntents = ArrayList<PendingIntent>().apply { repeat(parts.size) { add(deliveryIntent) } }
                smsManager.sendMultipartTextMessage(
                    destinationAddress,
                    null,
                    parts,
                    sentIntents,
                    deliveryIntents
                )
            } else {
                smsManager.sendTextMessage(
                    destinationAddress,
                    null,
                    messageText,
                    sentIntent,
                    deliveryIntent
                )
            }

            saveSentMessageToSystemProvider(destinationAddress, messageText)
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun saveSentMessageToSystemProvider(address: String, body: String) {
        try {
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, address)
                put(Telephony.Sms.BODY, body)
                put(Telephony.Sms.DATE, System.currentTimeMillis())
                put(Telephony.Sms.READ, 1)
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
            }
            context.contentResolver.insert(Telephony.Sms.Sent.CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getConversations(): List<Conversation> = withContext(Dispatchers.IO) {
        val conversationList = mutableListOf<Conversation>()
        val uri = Telephony.Sms.Conversations.CONTENT_URI

        val projection = arrayOf(
            Telephony.Sms.Conversations.THREAD_ID,
            Telephony.Sms.Conversations.SNIPPET,
            Telephony.Sms.Conversations.MESSAGE_COUNT
        )

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "date DESC LIMIT 50"
            )

            cursor?.use { c ->
                val threadIdCol = c.getColumnIndex(Telephony.Sms.Conversations.THREAD_ID)
                val snippetCol = c.getColumnIndex(Telephony.Sms.Conversations.SNIPPET)
                val countCol = c.getColumnIndex(Telephony.Sms.Conversations.MESSAGE_COUNT)

                while (c.moveToNext()) {
                    val threadId = c.getLong(threadIdCol)
                    val snippet = c.getString(snippetCol) ?: ""
                    val count = c.getInt(countCol)
                    val details = getThreadDetails(threadId)

                    conversationList.add(
                        Conversation(
                            threadId = threadId,
                            address = details.address,
                            contactName = details.contactName ?: details.address,
                            lastMessage = snippet,
                            timestamp = details.date,
                            unreadCount = details.unreadCount,
                            messageCount = count
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        conversationList
    }

    suspend fun getMessagesForThread(threadId: Long): List<SmsMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<SmsMessage>()
        val uri = Telephony.Sms.CONTENT_URI
        val selection = "${Telephony.Sms.THREAD_ID} = ?"
        val selectionArgs = arrayOf(threadId.toString())

        try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE,
                    Telephony.Sms.READ
                ),
                selection,
                selectionArgs,
                "date ASC"
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndex(Telephony.Sms._ID)
                val addressCol = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyCol = c.getColumnIndex(Telephony.Sms.BODY)
                val dateCol = c.getColumnIndex(Telephony.Sms.DATE)
                val typeCol = c.getColumnIndex(Telephony.Sms.TYPE)
                val readCol = c.getColumnIndex(Telephony.Sms.READ)

                while (c.moveToNext()) {
                    messages.add(
                        SmsMessage(
                            id = c.getLong(idCol),
                            threadId = threadId,
                            address = c.getString(addressCol) ?: "",
                            body = c.getString(bodyCol) ?: "",
                            timestamp = c.getLong(dateCol),
                            isSent = c.getInt(typeCol) == Telephony.Sms.MESSAGE_TYPE_SENT,
                            isRead = c.getInt(readCol) == 1
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        messages
    }

    private data class ThreadDetails(
        val address: String,
        val contactName: String?,
        val date: Long,
        val unreadCount: Int
    )

    private fun getThreadDetails(threadId: Long): ThreadDetails {
        var address = ""
        var date = System.currentTimeMillis()
        val unreadCount = 0

        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.DATE, Telephony.Sms.READ),
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "date DESC LIMIT 1"
        )

        cursor?.use { c ->
            if (c.moveToFirst()) {
                address = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: ""
                date = c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
            }
        }

        return ThreadDetails(address, null, date, unreadCount)
    }
}