package com.rasayili.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rasayili.sms.ui.viewmodel.SmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(
    viewModel: SmsViewModel,
    initialRecipient: String = "",
    onNavigateBack: () -> Unit,
    onMessageSent: (Long, String) -> Unit
) {
    var recipient by remember(initialRecipient) {
        mutableStateOf(initialRecipient)
    }
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "رسالة خلوية جديدة",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("رقم الهاتف") },
                placeholder = { Text("أدخل رقم المستلم") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("نص الرسالة") },
                placeholder = { Text("اكتب رسالتك هنا...") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val number = recipient.trim()
                    val body = messageText.trim()

                    if (number.isNotEmpty() && body.isNotEmpty()) {
                        viewModel.sendSms(number, body)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = recipient.isNotBlank() &&
                          messageText.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("إرسال الرسالة", fontSize = 16.sp)
            }
        }
    }
}