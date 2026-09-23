package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PlatformType
import com.example.data.model.ShipmentEntity
import com.example.data.model.ShipmentStatus
import com.example.data.model.UserRole
import com.example.ui.components.BarcodeDrawerView
import com.example.ui.components.CourierBadge
import com.example.ui.components.PlatformBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusBadge
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsOrange
import com.example.ui.viewmodel.ShipTrackerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShipmentsScreen(
    viewModel: ShipTrackerViewModel,
    onOpenStatusDialog: (ShipmentEntity) -> Unit,
    onOpenReturnDialog: (ShipmentEntity) -> Unit,
    onOpenDetailSheet: (ShipmentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val shipments by viewModel.filteredShipments.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedPlatform by viewModel.selectedPlatformFilter.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search tracking #, recipient, SKU...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shipments_search_bar"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { viewModel.selectedStatusFilter.value = null },
                label = { Text("All Statuses") }
            )

            ShipmentStatus.entries.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = {
                        viewModel.selectedStatusFilter.value = if (selectedStatus == status) null else status
                    },
                    label = { Text(status.displayName) }
                )
            }
        }

        // Platform Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedPlatform == null,
                onClick = { viewModel.selectedPlatformFilter.value = null },
                label = { Text("All Platforms") }
            )

            PlatformType.entries.forEach { platform ->
                FilterChip(
                    selected = selectedPlatform == platform,
                    onClick = {
                        viewModel.selectedPlatformFilter.value = if (selectedPlatform == platform) null else platform
                    },
                    label = { Text(platform.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Results Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${shipments.size} Shipments Found",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            if (currentUser.role == UserRole.STAFF_PACKER) {
                Text(
                    text = "Staff Mode (Restricted Delete)",
                    fontSize = 11.sp,
                    color = LogisticsOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Shipments List
        if (shipments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No parcels matching current filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(shipments, key = { it.trackingNumber }) { item ->
                    ShipmentCard(
                        shipment = item,
                        currentUserRole = currentUser.role,
                        onUpdateStatus = { onOpenStatusDialog(item) },
                        onLogReturn = { onOpenReturnDialog(item) },
                        onViewDetail = { onOpenDetailSheet(item) },
                        onDelete = { viewModel.deleteShipment(item.trackingNumber) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun ShipmentCard(
    shipment: ShipmentEntity,
    currentUserRole: UserRole,
    onUpdateStatus: () -> Unit,
    onLogReturn: () -> Unit,
    onViewDetail: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val df = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetail() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Badges and Sync indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformBadge(platform = shipment.platform)
                    CourierBadge(courier = shipment.courier)
                }
                StatusBadge(status = shipment.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barcode Drawer Preview (visual realism)
            BarcodeDrawerView(trackingNumber = shipment.trackingNumber)

            Spacer(modifier = Modifier.height(8.dp))

            // Content details
            Text(
                text = shipment.itemsSummary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recipient: ${shipment.recipientName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${shipment.weightKg} kg • ₱${shipment.declaredValue}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Address: ${shipment.recipientAddress}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scanned: ${df.format(Date(shipment.scannedAt))}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                SyncStatusBadge(status = shipment.syncStatus)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Workflow Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onUpdateStatus,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_update_status")
                ) {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Transition", fontSize = 11.sp)
                }

                if (shipment.status == ShipmentStatus.IN_TRANSIT || shipment.status == ShipmentStatus.DELIVERED) {
                    OutlinedButton(
                        onClick = onLogReturn,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log RTS", fontSize = 11.sp, color = Color(0xFFDC2626))
                    }
                }

                // Delete Button (Admin allowed, Staff restricted)
                if (currentUserRole == UserRole.OWNER_ADMIN) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
