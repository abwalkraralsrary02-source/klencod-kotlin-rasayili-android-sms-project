
package com.rasayili.sms.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var showAbout by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var readReceiptsEnabled by remember { mutableStateOf(false) }

    BackHandler {
        if (showAbout) {
            showAbout = false
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (showAbout) "حول التطبيق" else "الإعدادات",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showAbout) showAbout = false
                            else onNavigateBack()
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showAbout) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "رسائلي",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "تطبيق لإدارة الرسائل النصية SMS",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                HorizontalDivider()

                Text("المطوّر: علي عبدالاله")
                Text("حقوق النشر © 2026 علي عبدالاله")
                Text("الإصدار: 1.0")
            } else {
                Text(
                    "عام",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                SettingsRow(
                    icon = {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    },
                    title = "الإشعارات",
                    subtitle = "تشغيل خيار الإشعارات داخل صفحة الإعدادات",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                            }
                        )
                    }
                )

                HorizontalDivider()

                SettingsRow(
                    icon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                    },
                    title = "تأكيد القراءة",
                    subtitle = "يتطلب نظام مراسلة يدعم إيصالات القراءة",
                    trailing = {
                        Switch(
                            checked = readReceiptsEnabled,
                            onCheckedChange = {
                                readReceiptsEnabled = it
                            }
                        )
                    }
                )

                HorizontalDivider()

                SettingsRow(
                    icon = {
                        Icon(Icons.Default.Palette, contentDescription = null)
                    },
                    title = "المظهر",
                    subtitle = "يتبع التطبيق حاليًا مظهر النظام",
                    trailing = {}
                )

                HorizontalDivider()

                Text(
                    "التطبيق",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                SettingsRow(
                    icon = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    },
                    title = "حول التطبيق",
                    subtitle = "اسم المطوّر وحقوق النشر",
                    trailing = {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    },
                    onClick = { showAbout = true }
                )

                SettingsRow(
                    icon = {
                        Icon(Icons.Default.Security, contentDescription = null)
                    },
                    title = "الخصوصية",
                    subtitle = "رسائلك النصية وبيانات جهازك",
                    trailing = {}
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        trailing()
    }
}