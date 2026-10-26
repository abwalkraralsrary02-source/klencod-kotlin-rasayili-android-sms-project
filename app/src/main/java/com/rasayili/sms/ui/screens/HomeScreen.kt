
package com.rasayili.sms.ui.screens

import android.provider.ContactsContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rasayili.sms.data.model.Conversation
import com.rasayili.sms.ui.viewmodel.SmsViewModel

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

    val filteredConversations = conversations.filter {
        it.contactName.contains(searchText, ignoreCase = true) ||
            it.address.contains(searchText, ignoreCase = true) ||
            it.lastMessage.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "رسائلي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "الإعدادات"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNewMessage) {
                Icon(Icons.Default.Add, contentDescription = "رسالة جديدة")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    label = { Text("المحادثات") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    label = { Text("جهات الاتصال") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onNavigateToSettings()
                    },
                    icon = {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    },
                    label = { Text("الإعدادات") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("ابحث هنا...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true
            )

            when (selectedTab) {
                0 -> {
                    if (filteredConversations.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد محادثات حتى الآن")
                        }
                    } else {
                        LazyColumn {
                            items(
                                items = filteredConversations,
                                key = { it.threadId }
                            ) { conversation ->
                                ConversationRow(
                                    conversation = conversation,
                                    onClick = {
                                        onNavigateToThread(
                                            conversation.threadId,
                                            conversation.address
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    val filteredContacts = contacts.filter {
                        it.first.contains(searchText, ignoreCase = true) ||
                            it.second.contains(searchText, ignoreCase = true)
                    }
                    LazyColumn {
                        items(filteredContacts) { contact ->
                            ListItem(
                                headlineContent = { Text(contact.first) },
                                supportingContent = { Text(contact.second) },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onNavigateToContact(contact.second)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }

                else -> {
                    ListItem(
                        headlineContent = { Text("الإعدادات") },
                        supportingContent = {
                            Text("إعدادات تطبيق رسائلي")
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.clickable {
                            onNavigateToSettings()
                        }
                    )
                    ListItem(
                        headlineContent = {
                            Text("تعيين كتطبيق الرسائل الافتراضي")
                        },
                        modifier = Modifier.clickable {
                            onRequestDefaultRole()
                        }
                    )
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
    ListItem(
        headlineContent = {
            Text(
                text = conversation.contactName.ifBlank {
                    conversation.address
                },
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(text = conversation.lastMessage, maxLines = 1)
        },
        leadingContent = {
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = conversation.contactName.firstOrNull()
                            ?.uppercase() ?: "#",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        trailingContent = {
            Text(
                text = android.text.format.DateFormat.format(
                    "dd/MM",
                    conversation.timestamp
                ).toString(),
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}
