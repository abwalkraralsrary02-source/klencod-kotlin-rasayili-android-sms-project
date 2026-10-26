package com.rasayili.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rasayili.sms.data.model.SmsMessage
import com.rasayili.sms.ui.viewmodel.SmsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    threadId: Long,
    address: String,
    viewModel: SmsViewModel,
    onNavigateBack: () -> Unit
) {
    val messages by viewModel.currentThreadMessages.collectAsState()
    var inputText by remember(threadId) { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(threadId) {
        viewModel.loadMessagesForThread(threadId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = address, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), state = listState) {
                items(messages, key = { it.id }) { message ->
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (message.isSent) Alignment.CenterEnd else Alignment.CenterStart) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (message.isSent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(text = message.body, color = if (message.isSent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextField(value = inputText, onValueChange = { inputText = it }, placeholder = { Text("اكتب رسالة SMS...") }, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendSms(address, inputText)
                            inputText = ""
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال")
                    }
                }
            }
        }
    }
}