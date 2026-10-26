package com.rasayili.sms

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.rasayili.sms.ui.navigation.AppNavGraph
import com.rasayili.sms.ui.theme.RasayiliTheme
import com.rasayili.sms.ui.viewmodel.SmsViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val smsViewModel: SmsViewModel by viewModels()

    private val defaultSmsRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val isDefault = isDefaultSmsApp(this)
        smsViewModel.onDefaultSmsAppStatusChanged(isDefault)

        if (isDefault && hasRequiredPermissions(this)) {
            smsViewModel.loadConversations()
        }
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasPermissions = hasRequiredPermissions(this)
        smsViewModel.onPermissionsResult(hasPermissions)

        if (hasPermissions) {
            smsViewModel.loadConversations()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = resources.configuration
        val locale = Locale("ar")
        Locale.setDefault(locale)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        createConfigurationContext(config)

        checkAndRequestDefaultSmsRole()

        setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                RasayiliTheme(darkTheme = isSystemInDarkTheme()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

                        AppNavGraph(
                            navController = navController,
                            viewModel = smsViewModel,
                            onRequestPermissions = {
                                requestSmsPermissions()
                            },
                            onRequestDefaultRole = {
                                promptDefaultSmsApp()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val isDefault = isDefaultSmsApp(this)
        val hasPermissions = hasRequiredPermissions(this)

        smsViewModel.onDefaultSmsAppStatusChanged(isDefault)
        smsViewModel.checkPermissions(hasPermissions)

        if (hasPermissions) {
            smsViewModel.loadConversations()
        }
    }

    fun isDefaultSmsApp(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
        }
    }

    fun promptDefaultSmsApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)

            if (
                roleManager != null &&
                roleManager.isRoleAvailable(RoleManager.ROLE_SMS)
            ) {
                val intent =
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                defaultSmsRoleLauncher.launch(intent)
            }
        } else {
            val intent =
                Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                    putExtra(
                        Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        packageName
                    )
                }

            defaultSmsRoleLauncher.launch(intent)
        }
    }

    private fun checkAndRequestDefaultSmsRole() {
        smsViewModel.onDefaultSmsAppStatusChanged(isDefaultSmsApp(this))
    }

    private fun hasRequiredPermissions(context: Context): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestSmsPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionsLauncher.launch(permissions.toTypedArray())
    }
}