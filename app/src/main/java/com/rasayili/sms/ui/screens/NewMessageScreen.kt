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
    onNavigateBack: () -> Unit,
    onMessageSent: (Long, String) -> Unit
) {
    var recipient by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("رسالة خلوية جديدة", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("إلى (رقم الهاتف):") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = messageText, onValueChange = { messageText = it }, label = { Text("نص الرسالة:") }, modifier = Modifier.fillMaxWidth().weight(1f))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (recipient.isNotBlank() && messageText.isNotBlank()) {
                        viewModel.sendSms(recipient.trim(), messageText.trim())
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إرسال")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            }
        }
    }
}
