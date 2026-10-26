package com.rasayili.sms.data.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import com.rasayili.sms.data.model.Conversation
import com.rasayili.sms.data.model.SmsMessage

class SmsRepository(private val context: Context) {

    private val contactNames = mutableMapOf<String, String>()

    fun sendSms(
        phoneNumber: String,
        message: String,
        subscriptionId: Int =
            SubscriptionManager.getDefaultSmsSubscriptionId()
    ) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val sentIntent = PendingIntent.getBroadcast(
            context,
            100,
            Intent("com.rasayili.sms.SMS_SENT"),
            flags
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            context,
            101,
            Intent("com.rasayili.sms.SMS_DELIVERED"),
            flags
        )

        @Suppress("DEPRECATION")
        val smsManager: SmsManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
                    .createForSubscriptionId(subscriptionId)
            } else {
                SmsManager.getDefault()
            }

        val parts = smsManager.divideMessage(message)

        if (parts.size > 1) {
            val sentIntents = ArrayList<PendingIntent?>()
            val deliveredIntents = ArrayList<PendingIntent?>()

            repeat(parts.size) {
                sentIntents.add(sentIntent)
                deliveredIntents.add(deliveredIntent)
            }

            smsManager.sendMultipartTextMessage(
                phoneNumber,
                null,
                parts,
                sentIntents,
                deliveredIntents
            )
        } else {
            smsManager.sendTextMessage(
                phoneNumber,
                null,
                message,
                sentIntent,
                deliveredIntent
            )
        }
    }

    fun getConversations(): List<Conversation> {
        val conversations = mutableMapOf<Long, Conversation>()

        val projection = arrayOf(
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.READ
        )

        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC"
        )?.use { cursor ->

            val threadIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val addressIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val readIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (cursor.moveToNext()) {
                val threadId = cursor.getLong(threadIndex)
                val address = cursor.getString(addressIndex) ?: continue
                val body = cursor.getString(bodyIndex) ?: ""
                val timestamp = cursor.getLong(dateIndex)
                val isRead = cursor.getInt(readIndex) == 1

                if (!conversations.containsKey(threadId)) {
                    conversations[threadId] = Conversation(
                        threadId = threadId,
                        address = address,
                        contactName = getContactName(address),
                        lastMessage = body,
                        timestamp = timestamp,
                        unreadCount = if (isRead) 0 else 1,
                        messageCount = 1
                    )
                }
            }
        }

        return conversations.values.sortedByDescending {
            it.timestamp
        }
    }

    fun getMessagesForThread(threadId: Long): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )

        context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            "${Telephony.Sms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Telephony.Sms.DATE} ASC"
        )?.use { cursor ->

            val idIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
            val threadIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val addressIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIndex =
                cursor.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (cursor.moveToNext()) {
                val type = cursor.getInt(typeIndex)

                messages.add(
                    SmsMessage(
                        id = cursor.getLong(idIndex),
                        threadId = cursor.getLong(threadIndex),
                        address = cursor.getString(addressIndex) ?: "",
                        body = cursor.getString(bodyIndex) ?: "",
                        timestamp = cursor.getLong(dateIndex),
                        isSent = type == Telephony.Sms.MESSAGE_TYPE_SENT,
                        isRead = cursor.getInt(readIndex) == 1
                    )
                )
            }
        }

        return messages
    }

    private fun getContactName(phoneNumber: String): String {
        contactNames[phoneNumber]?.let { return it }

        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )

            context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(0)

                    if (!name.isNullOrBlank()) {
                        contactNames[phoneNumber] = name
                        return name
                    }
                }
            }
        } catch (_: SecurityException) {
            // إذن جهات الاتصال غير ممنوح
        } catch (_: Exception) {
            // تعذر البحث عن الاسم
        }

        return phoneNumber
    }
}