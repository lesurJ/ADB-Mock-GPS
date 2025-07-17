package com.adbmockgps

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocationScreen(
    arePermissionsGranted: Boolean,
    isMockAppSelected: Boolean,
    lastBroadcastInfo: LastBroadcastInfo?,
    onGrantPermissions: () -> Unit,
    onSetMockApp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
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

            StatusCard(arePermissionsGranted, isMockAppSelected, onGrantPermissions, onSetMockApp)
            LastReceivedDataCard(lastBroadcastInfo)
            AdbCommandCard()
        }
    }
}

@Composable
fun StatusCard(
    permissionsGranted: Boolean,
    isMockAppSelected: Boolean,
    onGrantPermissions: () -> Unit,
    onSetMockApp: () -> Unit
) {
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
            StatusRow("Location Permissions", permissionsGranted)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Make sure to select this app as 'Mock Location App' in the developer settings.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            if (!permissionsGranted){
                Spacer(Modifier.height(16.dp))
                Button(onClick = onGrantPermissions) {
                    Text("Grant Permissions")
                }
            }

            if (!isMockAppSelected) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onSetMockApp) {
                    Text("Select in Developer Options")
                }
            }
        }
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

@SuppressLint("DefaultLocale")
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
                InfoRow("Latitude:", String.format("%.6f", lastBroadcastInfo.latitude))
                InfoRow("Longitude:", String.format("%.6f", lastBroadcastInfo.longitude))
                InfoRow("Altitude:", lastBroadcastInfo.altitude?.let { String.format("%.1f m", it) } ?: "N/A")
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