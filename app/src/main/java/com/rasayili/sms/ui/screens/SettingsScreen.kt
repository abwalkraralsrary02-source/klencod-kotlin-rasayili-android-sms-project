
package com.rasayili.sms.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
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
    var darkThemeEnabled by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showAbout -> showAbout = false
            showAppearance -> showAppearance = false
            else -> onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            showAbout -> "حول التطبيق"
                            showAppearance -> "المظهر"
                            else -> "الإعدادات"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when {
                                showAbout -> showAbout = false
                                showAppearance -> showAppearance = false
                                else -> onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                showAbout -> {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        "رسائلي",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Text(
                        "تطبيق الرسائل النصية SMS",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    HorizontalDivider()

                    Text("المطوّر: علي عبدالاله")
                    Text("حقوق النشر © 2026 علي عبدالاله")
                    Text("الإصدار: 1.0")
                }

                showAppearance -> {
                    Text(
                        "تخصيص مظهر التطبيق",
                        fontWeight = FontWeight.Bold
                    )

                    SettingsRow(
                        icon = {
                            Icon(Icons.Default.Palette, contentDescription = null)
                        },
                        title = "المظهر الداكن",
                        subtitle = "تغيير ألوان التطبيق إلى الداكن",
                        trailing = {
                            Switch(
                                checked = darkThemeEnabled,
                                onCheckedChange = { darkThemeEnabled = it }
                            )
                        }
                    )

                    Text(
                        "ملاحظة: هذا الخيار تجريبي حاليًا، ولا يغيّر ألوان التطبيق بالكامل بعد.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                else -> {
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
                        subtitle = "تشغيل أو إيقاف هذا الخيار داخل الصفحة",
                        trailing = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it }
                            )
                        }
                    )

                    HorizontalDivider()

                    SettingsRow(
                        icon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        },
                        title = "تأكيد القراءة",
                        subtitle = "يحتاج إلى نظام يدعم إيصالات القراءة",
                        trailing = {
                            Switch(
                                checked = readReceiptsEnabled,
                                onCheckedChange = { readReceiptsEnabled = it }
                            )
                        }
                    )

                    HorizontalDivider()

                    SettingsRow(
                        icon = {
                            Icon(Icons.Default.Palette, contentDescription = null)
                        },
                        title = "المظهر والألوان",
                        subtitle = "تخصيص مظهر التطبيق",
                        trailing = {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        },
                        onClick = { showAppearance = true }
                    )

                    HorizontalDivider()

                    Text(
                        "الرسائل والبيانات",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    SettingsRow(
                        icon = {
                            Icon(Icons.Default.Storage, contentDescription = null)
                        },
                        title = "الوسائط والتخزين",
                        subtitle = "خيارات التخزين ستُضاف لاحقًا",
                        trailing = {}
                    )

                    HorizontalDivider()

                    SettingsRow(
                        icon = {
                            Icon(Icons.Default.Backup, contentDescription = null)
                        },
                        title = "النسخ الاحتياطي",
                        subtitle = "ميزة قيد التطوير",
                        trailing = {}
                    )

                    HorizontalDivider()

                    SettingsRow(
                        icon = {
                            Icon(Icons.Default.Security, contentDescription = null)
                        },
                        title = "الخصوصية",
                        subtitle = "معلومات حول بيانات الرسائل",
                        trailing = {}
                    )

                    HorizontalDivider()

                    SettingsRow(
                        icon = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        },
                        title = "حول التطبيق",
                        subtitle = "المطوّر وحقوق النشر",
                        trailing = {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        },
                        onClick = { showAbout = true }
                    )
                }
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        trailing()
    }
}