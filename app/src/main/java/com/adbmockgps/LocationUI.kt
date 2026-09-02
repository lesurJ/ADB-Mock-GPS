package com.adbmockgps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun LocationScreen(
    hasLocationPermissions: Boolean,
    hasNotificationPermission: Boolean,
    isMockLocationApp: Boolean,
    lastBroadcastInfo: LastBroadcastInfo?,
    buildTagVersion: String,
    onGrantLocationPermissions: () -> Unit,
    onGrantNotificationPermission: () -> Unit,
    onOpenDeveloperOptions: () -> Unit,
    onInitializeLocation: (Capital) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "ADB Mock GPS",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            StatusCard(
                hasLocationPermissions,
                hasNotificationPermission,
                isMockLocationApp,
                onGrantLocationPermissions,
                onGrantNotificationPermission,
                onOpenDeveloperOptions
            )
            LastReceivedDataCard(lastBroadcastInfo)
            InitializeLocationCard(onInitializeLocation)
            AdbCommandCard()
        }
        Text(
            text = "Build: $buildTagVersion",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            color = Color.DarkGray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun StatusCard(
    hasLocationPermissions: Boolean,
    hasNotificationPermission: Boolean,
    isMockLocationApp: Boolean,
    onGrantLocationPermissions: () -> Unit,
    onGrantNotificationPermission: () -> Unit,
    onOpenDeveloperOptions: () -> Unit
) {
    var showDeveloperOptionsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🔐️ Location Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
            StatusRow("Location Permissions", hasLocationPermissions)
            Spacer(Modifier.height(12.dp))
            StatusRow("Notification Permissions", hasNotificationPermission)
            Spacer(Modifier.height(12.dp))
            StatusRow("Selected as Mock Location App", isMockLocationApp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Make sure to select this app as 'Mock Location App' in the developer settings.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            if (!hasLocationPermissions){
                Spacer(Modifier.height(12.dp))
                Button(onClick = onGrantLocationPermissions) {
                    Text("Grant Location Permission")
                }
            }

            if (!hasNotificationPermission){
                Spacer(Modifier.height(12.dp))
                Button(onClick = onGrantNotificationPermission) {
                    Text("Grant Notification Permission")
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = { showDeveloperOptionsDialog = true }) {
                Text("Select in Developer Options")
            }
        }
    }

    if (showDeveloperOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showDeveloperOptionsDialog = false },
            title = { Text("Enable Mock Location") },
            text = { Text("To enable mock locations, please select this app in Developer Options > Select mock location app.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeveloperOptionsDialog = false
                    onOpenDeveloperOptions()
                }) {
                    Text("Open Developer Options")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeveloperOptionsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusRow(label: String, isGranted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = if (isGranted) "✅ Granted" else "❌ Denied",
            color = if (isGranted) Color.Green else Color.Red,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun LastReceivedDataCard(lastBroadcastInfo: LastBroadcastInfo?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🛰️ Last Received Broadcast",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            if (lastBroadcastInfo == null) {
                Text(
                    text = "Waiting for first broadcast...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                InfoRow("Latitude:", String.format(Locale.US, "%.6f", lastBroadcastInfo.latitude))
                InfoRow("Longitude:", String.format(Locale.US, "%.6f", lastBroadcastInfo.longitude))
                InfoRow("Altitude:", lastBroadcastInfo.altitude?.let { String.format(Locale.US, "%.1f m", it) } ?: "N/A")
                InfoRow("Time:", lastBroadcastInfo.timestamp)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Cyan,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AdbCommandCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📱 ADB Commands",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Type the following command in a CLI to set/get the mocked location.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Set: adb shell am broadcast -a com.adbmockgps.SET_LOCATION --es lat \"40.7\" --es lon \"-74.0\" [--es alt \"100.34\"] -f 0x01000000",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Get: adb shell am broadcast -a com.adbmockgps.GET_LOCATION -f 0x01000000",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitializeLocationCard(onInitializeLocation: (Capital) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Capital?>(null) }
    val options = EuropeanCapitals.CAPITALS

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌍 Initialize Location",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Initialize location at pre-defined coordinates.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    readOnly = true,
                    value = selected?.let { "${it.city}, ${it.country}" } ?: "Select a capital",
                    onValueChange = {},
                    label = { Text("Capital") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { capital ->
                        DropdownMenuItem(
                            text = { Text("${capital.city}, ${capital.country}") },
                            onClick = {
                                selected = capital
                                expanded = false
                                onInitializeLocation(capital)
                            }
                        )
                    }
                }
            }
        }
    }
}
