package com.rasayili.sms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rasayili.sms.ui.viewmodel.SmsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ChatBackground = Color(0xFF303030)
private val ChatCard = Color(0xFF202020)
private val ChatBlue = Color(0xFF3068C8)
private val SentBubble = Color(0xFF183B59)
private val ReceivedBubble = Color(0xFF202020)
private val MessageBlue = Color(0xFF55B7FF)
private val SecondaryText = Color(0xFFBDBDBD)
private val BubbleBorder = Color(0xFF555555)

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = ChatBlue,
                                    modifier = Modifier.size(27.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = address,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "رسائل SMS",
                                fontSize = 12.sp,
                                color = Color(0xFFE0E9FF)
                            )
                        }
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
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "اتصال",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "المزيد",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChatBlue,
                    titleContentColor = Color.White
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
                    vertical = 14.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    val messageDay = remember(message.timestamp) {
                        SimpleDateFormat(
                            "yyyyMMdd",
                            Locale.getDefault()
                        ).format(Date(message.timestamp))
                    }

                    val previousMessage = messages
                        .takeWhile { it.id != message.id }
                        .lastOrNull()

                    val previousDay = previousMessage?.timestamp?.let {
                        SimpleDateFormat(
                            "yyyyMMdd",
                            Locale.getDefault()
                        ).format(Date(it))
                    }

                    if (previousDay != messageDay) {
                        val calendar = Calendar.getInstance()
                        val messageCalendar = Calendar.getInstance().apply {
                            timeInMillis = message.timestamp
                        }
                        val yesterday = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -1)
                        }

                        val dateLabel = when {
                            calendar.get(Calendar.YEAR) ==
                                messageCalendar.get(Calendar.YEAR) &&
                                calendar.get(Calendar.DAY_OF_YEAR) ==
                                messageCalendar.get(Calendar.DAY_OF_YEAR) ->
                                "اليوم"

                            yesterday.get(Calendar.YEAR) ==
                                messageCalendar.get(Calendar.YEAR) &&
                                yesterday.get(Calendar.DAY_OF_YEAR) ==
                                messageCalendar.get(Calendar.DAY_OF_YEAR) ->
                                "أمس"

                            else -> SimpleDateFormat(
                                "dd MMMM yyyy",
                                Locale.getDefault()
                            ).format(Date(message.timestamp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF454545)
                            ) {
                                Text(
                                    text = dateLabel,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isSent) {
                            Arrangement.End
                        } else {
                            Arrangement.Start
                        }
                    ) {
                        Surface(
                            modifier = Modifier.widthIn(max = 300.dp),
                            shape = RoundedCornerShape(
                                topStart = 17.dp,
                                topEnd = 17.dp,
                                bottomStart = if (message.isSent) 17.dp else 5.dp,
                                bottomEnd = if (message.isSent) 5.dp else 17.dp
                            ),
                            color = if (message.isSent) {
                                SentBubble
                            } else {
                                ReceivedBubble
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (message.isSent) {
                                    ChatBlue
                                } else {
                                    BubbleBorder
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 13.dp,
                                    vertical = 9.dp
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
                color = ChatCard,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 9.dp),
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF303944),
                            unfocusedContainerColor = Color(0xFF303944),
                            focusedBorderColor = MessageBlue,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = MessageBlue
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val textToSend = inputText.trim()
                                viewModel.sendSms(address, textToSend)
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
                                CircleShape
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