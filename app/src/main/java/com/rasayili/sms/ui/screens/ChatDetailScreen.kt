package com.rasayili.sms.ui.screens

import androidx.compose.foundation.background
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
import com.rasayili.sms.ui.viewmodel.SmsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ChatBackground = Color(0xFF10151D)
private val SentBubble = Color(0xFF183B59)
private val ReceivedBubble = Color(0xFF242B35)
private val MessageBlue = Color(0xFF55B7FF)
private val SecondaryText = Color(0xFFB5C0CC)

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

    LaunchedEffect(threadId, messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = ChatBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = address,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "رسائل SMS",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1769AA),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ChatBackground)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = 16.dp
                )
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->

                    val messageDate = remember(message.timestamp) {
                        SimpleDateFormat(
                            "yyyyMMdd",
                            Locale.getDefault()
                        ).format(Date(message.timestamp))
                    }

                    val previousDate = messages
                        .takeWhile { it.id != message.id }
                        .lastOrNull()
                        ?.timestamp
                        ?.let {
                            SimpleDateFormat(
                                "yyyyMMdd",
                                Locale.getDefault()
                            ).format(Date(it))
                        }

                    if (previousDate != messageDate) {
                        Text(
                            text = SimpleDateFormat(
                                "dd MMMM yyyy",
                                Locale.getDefault()
                            ).format(Date(message.timestamp)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            color = SecondaryText,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        contentAlignment = if (message.isSent) {
                            Alignment.CenterEnd
                        } else {
                            Alignment.CenterStart
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (message.isSent) 16.dp else 4.dp,
                                bottomEnd = if (message.isSent) 4.dp else 16.dp
                            ),
                            color = if (message.isSent) {
                                SentBubble
                            } else {
                                ReceivedBubble
                            },
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = 9.dp,
                                    bottom = 6.dp
                                )
                            ) {
                                Text(
                                    text = message.body,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                Text(
                                    text = SimpleDateFormat(
                                        "hh:mm a",
                                        Locale.getDefault()
                                    ).format(Date(message.timestamp)),
                                    color = if (message.isSent) {
                                        MessageBlue
                                    } else {
                                        SecondaryText
                                    },
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                color = Color(0xFF1A222D),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "اكتب رسالة SMS...",
                                color = SecondaryText
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF293341),
                            unfocusedContainerColor = Color(0xFF293341),
                            focusedBorderColor = MessageBlue,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = MessageBlue
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val messageToSend = inputText.trim()
                                viewModel.sendSms(address, messageToSend)
                                inputText = ""

                                scope.launch {
                                    val lastIndex = messages.lastIndex
                                    if (lastIndex >= 0) {
                                        listState.animateScrollToItem(lastIndex)
                                    }
                                }
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MessageBlue,
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال",
                            tint = Color(0xFF10151D)
                        )
                    }
                }
            }
        }
    }
}