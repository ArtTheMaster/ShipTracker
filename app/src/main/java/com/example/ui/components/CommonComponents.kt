package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourierType
import com.example.data.model.PlatformType
import com.example.data.model.ShipmentStatus
import com.example.data.model.SyncStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.ui.theme.LogisticsNavy
import com.example.ui.theme.PlatformLazada
import com.example.ui.theme.PlatformShopee
import com.example.ui.theme.PlatformTikTok
import com.example.ui.theme.StatusCancelled
import com.example.ui.theme.StatusDelivered
import com.example.ui.theme.StatusDispatched
import com.example.ui.theme.StatusInTransit
import com.example.ui.theme.StatusPrepared
import com.example.ui.theme.StatusReturned
import com.example.ui.theme.StatusScanned

@Composable
fun StatusBadge(status: ShipmentStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        ShipmentStatus.SCANNED -> StatusScanned.copy(alpha = 0.15f) to StatusScanned
        ShipmentStatus.PREPARED -> StatusPrepared.copy(alpha = 0.15f) to StatusPrepared
        ShipmentStatus.DISPATCHED -> StatusDispatched.copy(alpha = 0.15f) to StatusDispatched
        ShipmentStatus.IN_TRANSIT -> StatusInTransit.copy(alpha = 0.15f) to StatusInTransit
        ShipmentStatus.DELIVERED -> StatusDelivered.copy(alpha = 0.15f) to StatusDelivered
        ShipmentStatus.RETURNED -> StatusReturned.copy(alpha = 0.15f) to StatusReturned
        ShipmentStatus.CANCELLED -> StatusCancelled.copy(alpha = 0.15f) to StatusCancelled
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PlatformBadge(platform: PlatformType, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (platform) {
        PlatformType.SHOPEE -> PlatformShopee to Color.White
        PlatformType.LAZADA -> PlatformLazada to Color.White
        PlatformType.TIKTOK_SHOP -> PlatformTikTok to Color.White
        PlatformType.OTHER -> Color(0xFF475569) to Color.White
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = platform.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CourierBadge(courier: CourierType, modifier: Modifier = Modifier) {
    val color = Color(courier.brandColorHex)
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Text(
            text = courier.displayName,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SyncStatusBadge(status: SyncStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        SyncStatus.SYNCED -> "Cloud Synced" to Color(0xFF059669)
        SyncStatus.PENDING_SYNC -> "Pending Sync" to Color(0xFFD97706)
        SyncStatus.CONFLICT -> "Sync Conflict" to Color(0xFFDC2626)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BarcodeDrawerView(
    trackingNumber: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val chars = trackingNumber.toCharArray()
                val totalBars = 45
                val barWidth = canvasWidth / (totalBars * 1.4f)
                var currentX = 4f

                // Generate pseudo barcode bars deterministic on tracking number
                for (i in 0 until totalBars) {
                    val charVal = if (chars.isNotEmpty()) chars[i % chars.size].code else i
                    val isBlack = (charVal + i * 3) % 2 == 0 || i % 5 == 0
                    val isThick = (charVal + i) % 3 == 0

                    if (isBlack) {
                        val width = if (isThick) barWidth * 1.8f else barWidth
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(currentX, 0f),
                            size = Size(width, canvasHeight)
                        )
                    }
                    currentX += if (isThick) barWidth * 2.2f else barWidth * 1.4f
                    if (currentX > canvasWidth - 10f) break
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "* $trackingNumber *",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun TopAppHeader(
    currentUser: UserAccount,
    isOnline: Boolean,
    pendingSyncCount: Int,
    onToggleOnline: () -> Unit,
    onRoleSwitchClick: () -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LogisticsNavy,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: App Title and Role Tag
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ShipTracker",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (currentUser.role == UserRole.OWNER_ADMIN) Color(0xFFF97316) else Color(0xFF0D9488),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (currentUser.role == UserRole.OWNER_ADMIN) "ADMIN" else "STAFF",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = currentUser.fullName,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            // Right: Connectivity Indicator + Switch Role Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Online/Offline Pill Button
                Surface(
                    color = if (isOnline) Color(0xFF065F46) else Color(0xFF991B1B),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clickable { onToggleOnline() }
                        .testTag("network_status_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = if (isOnline) "Online" else "Offline",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOnline) "ONLINE" else "OFFLINE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Pending sync icon if any
                if (pendingSyncCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = Color(0xFFEF4444)) {
                                Text("$pendingSyncCount", color = Color.White, fontSize = 9.sp)
                            }
                        },
                        modifier = Modifier.clickable { onSyncClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Pending Sync",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Switch Role / User profile button
                IconButton(
                    onClick = onRoleSwitchClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .testTag("switch_user_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Role",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
