package com.rasayili.sms.ui.screens

import android.provider.ContactsContract
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rasayili.sms.data.model.Conversation
import com.rasayili.sms.ui.viewmodel.SmsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val MoodBlue = Color(0xFF3068C8)
private val MoodBackground = Color(0xFF303030)
private val MoodCard = Color(0xFF202020)
private val MoodBorder = Color(0xFF626262)
private val MoodText = Color(0xFFF5F5F5)
private val MoodSubText = Color(0xFFBDBDBD)

private data class ConversationGroup(
    val key: String,
    val title: String,
    val conversations: List<Conversation>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SmsViewModel,
    onNavigateToThread: (Long, String) -> Unit,
    onNavigateToNewMessage: () -> Unit,
    onNavigateToContact: (String) -> Unit,
    onRequestDefaultRole: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val conversations by viewModel.conversations.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var contacts by remember {
        mutableStateOf<List<Pair<String, String>>>(emptyList())
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            val list = mutableListOf<Pair<String, String>>()

            try {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                )?.use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    )
                    val numberIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )

                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameIndex) ?: continue
                        val number = cursor.getString(numberIndex) ?: continue
                        list.add(name to number)
                    }
                }
            } catch (_: Exception) {
            }

            contacts = list.distinctBy { it.second }
        }
    }

    val filteredConversations = conversations
        .filter {
            it.contactName.contains(searchText, ignoreCase = true) ||
                it.address.contains(searchText, ignoreCase = true) ||
                it.lastMessage.contains(searchText, ignoreCase = true)
        }
        .sortedByDescending { it.timestamp }

    val groupedConversations = filteredConversations
        .groupBy {
            SimpleDateFormat(
                "yyyyMMdd",
                Locale.getDefault()
            ).format(Date(it.timestamp))
        }
        .map { (key, group) ->
            ConversationGroup(
                key = key,
                title = getConversationDayTitle(group.first().timestamp),
                conversations = group
            )
        }

    Scaffold(
        containerColor = MoodBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "رسائلي",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            searchVisible = !searchVisible
                            if (!searchVisible) searchText = ""
                        }
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MoodBlue,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewMessage,
                containerColor = Color.White,
                contentColor = MoodBlue,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "رسالة جديدة"
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MoodCard,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = searchVisible,
                    onClick = {
                        searchVisible = !searchVisible
                        if (!searchVisible) searchText = ""
                    },
                    icon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "بحث"
                        )
                    },
                    label = { Text("بحث") },
                    colors = moodNavigationColors()
                )

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    label = { Text("المحادثات") },
                    colors = moodNavigationColors()
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    label = { Text("جهات الاتصال") },
                    colors = moodNavigationColors()
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onNavigateToSettings()
                    },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null
                        )
                    },
                    label = { Text("المزيد") },
                    colors = moodNavigationColors()
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MoodBackground)
        ) {
            if (searchVisible) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = {
                        Text(
                            "ابحث عن اسم أو رقم...",
                            color = MoodSubText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MoodSubText
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MoodText,
                        unfocusedTextColor = MoodText,
                        focusedBorderColor = MoodBlue,
                        unfocusedBorderColor = MoodBorder,
                        cursorColor = MoodBlue
                    )
                )
            }

            when (selectedTab) {
                0 -> {
                    if (groupedConversations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchText.isBlank()) {
                                    "لا توجد محادثات حتى الآن"
                                } else {
                                    "لا توجد نتائج للبحث"
                                },
                                color = MoodSubText
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                end = 12.dp,
                                top = 8.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            groupedConversations.forEach { group ->
                                item(key = "day_${group.key}") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                start = 8.dp,
                                                top = 8.dp,
                                                bottom = 2.dp
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "◷",
                                            color = MoodSubText,
                                            fontSize = 18.sp
                                        )

                                        Spacer(
                                            modifier = Modifier.width(8.dp)
                                        )

                                        Text(
                                            text = group.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                item(key = "group_${group.key}") {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MoodCard,
                                        shape = RoundedCornerShape(13.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MoodBorder
                                        )
                                    ) {
                                        Column {
                                            group.conversations.forEachIndexed {
                                                    index, conversation ->
                                                ConversationRow(
                                                    conversation = conversation,
                                                    onClick = {
                                                        onNavigateToThread(
                                                            conversation.threadId,
                                                            conversation.address
                                                        )
                                                    }
                                                )

                                                if (
                                                    index < group.conversations.lastIndex
                                                ) {
                                                    HorizontalDivider(
                                                        color = MoodBorder,
                                                        thickness = 1.dp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val filteredContacts = contacts.filter {
                        it.first.contains(searchText, ignoreCase = true) ||
                            it.second.contains(searchText, ignoreCase = true)
                    }

                    if (filteredContacts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "لا توجد جهات اتصال",
                                color = MoodSubText
                            )
                        }
                    } else {
                        LazyColumn {
                            items(
                                items = filteredContacts,
                                key = { it.second }
                            ) { contact ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onNavigateToContact(contact.second)
                                        }
                                        .padding(
                                            horizontal = 16.dp,
                                            vertical = 12.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ContactAvatar(contact.first)

                                    Spacer(
                                        modifier = Modifier.width(12.dp)
                                    )

                                    Column {
                                        Text(
                                            contact.first,
                                            color = MoodText,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            contact.second,
                                            color = MoodSubText,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                HorizontalDivider(color = MoodBorder)
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    "الإعدادات",
                                    color = MoodText
                                )
                            },
                            supportingContent = {
                                Text(
                                    "إعدادات تطبيق رسائلي",
                                    color = MoodSubText
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MoodBlue
                                )
                            },
                            colors = moodListItemColors(),
                            modifier = Modifier.clickable {
                                onNavigateToSettings()
                            }
                        )

                        HorizontalDivider(color = MoodBorder)

                        ListItem(
                            headlineContent = {
                                Text(
                                    "تعيين كتطبيق الرسائل الافتراضي",
                                    color = MoodText
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MoodBlue
                                )
                            },
                            colors = moodListItemColors(),
                            modifier = Modifier.clickable {
                                onRequestDefaultRole()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            conversation.contactName.ifBlank {
                conversation.address
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = conversation.contactName.ifBlank {
                    conversation.address
                },
                color = MoodText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = conversation.lastMessage.ifBlank {
                    conversation.address
                },
                color = MoodSubText,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = DateFormat.format(
                "h:mm a",
                conversation.timestamp
            ).toString(),
            color = MoodSubText,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun ContactAvatar(name: String) {
    val avatarColors = listOf(
        Color(0xFF8BCDBD),
        Color(0xFFF2A0A0),
        Color(0xFFF0C879),
        Color(0xFFB8B5E8),
        Color(0xFF9CC7EE)
    )

    val initial = name.firstOrNull()?.toString() ?: "#"
    val colorIndex = (name.hashCode() and 0x7FFFFFFF) % avatarColors.size

    Surface(
        modifier = Modifier.size(46.dp),
        shape = CircleShape,
        color = Color(0xFFF3F3F3),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            avatarColors[colorIndex]
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color(0xFF222222),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun moodNavigationColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = Color.White,
        indicatorColor = MoodBlue,
        unselectedIconColor = MoodSubText,
        unselectedTextColor = MoodSubText
    )

@Composable
private fun moodListItemColors() =
    ListItemDefaults.colors(
        containerColor = MoodBackground,
        headlineColor = MoodText,
        supportingColor = MoodSubText
    )

private fun getConversationDayTitle(timestamp: Long): String {
    val messageCalendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }

    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    fun sameDay(first: Calendar, second: Calendar): Boolean {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }

    return when {
        sameDay(messageCalendar, today) -> "اليوم"
        sameDay(messageCalendar, yesterday) -> "أمس"
        else -> SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}