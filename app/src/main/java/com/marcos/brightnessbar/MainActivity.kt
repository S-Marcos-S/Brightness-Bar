package com.marcos.brightnessbar

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import androidx.compose.foundation.isSystemInDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            val colorScheme = if (isDarkTheme) {
                darkColorScheme(
                    primary = Color(0xFFBB86FC),
                    background = Color.Black,
                    surface = Color.Black,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            } else {
                lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionsScreen()
                }
            }
        }
    }
}

@Composable
fun PermissionsScreen() {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    
    var isWriteSettingsGranted by remember { mutableStateOf(Settings.System.canWrite(context)) }
    var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var isAppEnabled by remember { mutableStateOf(prefs.isEnabled) }

    // Listener para sincronizar estado com SharedPreferences
    DisposableEffect(Unit) {
        val listener = prefs.registerListener { key ->
            if (key == "is_enabled") {
                isAppEnabled = prefs.isEnabled
            }
        }
        onDispose {
            prefs.unregisterListener(listener)
        }
    }

    // Atualiza o estado quando o usuário volta para o app
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isWriteSettingsGranted = Settings.System.canWrite(context)
                isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
                isAppEnabled = prefs.isEnabled
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme()) Color(0xFF121212) else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isAppEnabled) "Ativado" else "Desativado",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Controle de brilho na barra",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = isAppEnabled,
                    onCheckedChange = {
                        isAppEnabled = it
                        prefs.isEnabled = it
                        // Notificar o sistema para atualizar o Bloco QS
                        android.service.quicksettings.TileService.requestListeningState(
                            context,
                            android.content.ComponentName(context, BrightnessQsTileService::class.java)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(id = R.string.permissions_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        PermissionItem(
            title = stringResource(id = R.string.write_settings_label),
            description = stringResource(id = R.string.write_settings_desc),
            isGranted = isWriteSettingsGranted,
            onGrantClick = {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionItem(
            title = stringResource(id = R.string.accessibility_label),
            description = stringResource(id = R.string.accessibility_desc),
            isGranted = isAccessibilityEnabled,
            onGrantClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            }
        )
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = description, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isGranted) {
                Text(
                    text = stringResource(id = R.string.granted_label),
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            } else {
                Button(
                    onClick = onGrantClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = stringResource(id = R.string.grant_button))
                }
            }
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val serviceId = "${context.packageName}/${BrightnessAccessibilityService::class.java.name}"
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    
    return enabledServices.any { it.id == serviceId } || Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )?.contains(context.packageName) == true
}
