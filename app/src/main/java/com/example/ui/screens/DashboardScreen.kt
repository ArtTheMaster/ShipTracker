package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlatformType
import com.example.data.model.UserRole
import com.example.ui.components.PlatformBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsNavy
import com.example.ui.theme.LogisticsOrange
import com.example.ui.theme.LogisticsTeal
import com.example.ui.viewmodel.ShipTrackerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ShipTrackerViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToDispatch: () -> Unit,
    onNavigateToReturns: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToShipments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentAuditLogs.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Welcome & Role Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = LogisticsNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GJandAsher Outbound Logistics",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Operations Dashboard",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = if (currentUser.role == UserRole.OWNER_ADMIN) LogisticsOrange else LogisticsTeal,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentUser.role.displayName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Outbound Workflow: Scan → Validate → Prepare → Dispatch → In-Transit → Delivered / Returned",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Primary Logistics KPIs Grid
        item {
            Text(
                text = "Outbound Key Metrics",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Total Scanned",
                    value = "${analytics.totalShipments}",
                    subtitle = "${analytics.scannedCount} pending pack",
                    icon = Icons.Default.QrCodeScanner,
                    accentColor = LogisticsBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToShipments
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "Prepared (Ready)",
                    value = "${analytics.preparedCount}",
                    subtitle = "Awaiting handover",
                    icon = Icons.Default.LocalShipping,
                    accentColor = LogisticsOrange,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToDispatch
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "In-Transit",
                    value = "${analytics.inTransitCount}",
                    subtitle = "${analytics.deliveredCount} delivered",
                    icon = Icons.Default.CheckCircle,
                    accentColor = LogisticsTeal,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToShipments
                )
                Spacer(modifier = Modifier.width(10.dp))
                StatCard(
                    title = "Return Rate (RTS)",
                    value = "${"%.1f".format(analytics.returnRatePercent)}%",
                    subtitle = "${analytics.returnedCount} returned parcels",
                    icon = Icons.Default.AssignmentReturn,
                    accentColor = if (analytics.returnRatePercent > 5.0) Color(0xFFDC2626) else Color(0xFF059669),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReturns
                )
            }

            // Admin Only Financial KPI
            if (currentUser.role == UserRole.OWNER_ADMIN) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Outbound Declared Value",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₱${"%,.2f".format(analytics.totalDeclaredValue)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            color = LogisticsOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "OWNER ONLY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LogisticsOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Workflow Actions
        item {
            Text(
                text = "Logistics Quick Actions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onNavigateToScan,
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_action_scan")
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Parcel", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onNavigateToDispatch,
                    colors = ButtonDefaults.buttonColors(containerColor = LogisticsOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_action_dispatch")
                ) {
                    Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dispatch Batch", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onNavigateToReturns,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Return", fontSize = 13.sp, color = Color(0xFFDC2626))
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedButton(
                    onClick = onNavigateToReports,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reports & CSV", fontSize = 13.sp)
                }
            }
        }

        // Platform Breakdown Visualizer (Module 9 Analytics)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Platform Distribution (Multi-Channel)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val total = if (analytics.totalShipments > 0) analytics.totalShipments else 1

                    PlatformType.entries.forEach { platform ->
                        val count = analytics.platformCounts[platform] ?: 0
                        val percent = (count.toFloat() / total.toFloat()) * 100f
                        val color = Color(platform.badgeColorHex)

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = platform.displayName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(text = "$count (${"%.0f".format(percent)}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                                        .height(8.dp)
                                        .background(color, RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Audit / Operational Logs
        item {
            Text(
                text = "Recent Operational Activity",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (recentLogs.isEmpty()) {
            item {
                Text(text = "No operational logs yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(recentLogs.take(5)) { log ->
                val df = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(LogisticsTeal, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.action,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = df.format(Date(log.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = log.details,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "By ${log.performedBy}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
