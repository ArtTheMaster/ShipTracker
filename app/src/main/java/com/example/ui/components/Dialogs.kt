package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourierType
import com.example.data.model.ItemCondition
import com.example.data.model.RefundStatus
import com.example.data.model.ReturnReason
import com.example.data.model.ShipmentEntity
import com.example.data.model.ShipmentStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DuplicateScanDialog(
    duplicate: ShipmentEntity,
    onDismiss: () -> Unit
) {
    val df = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateStr = df.format(Date(duplicate.scannedAt))

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFEE2E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Duplicate Warning",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "Duplicate Scan Detected!",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )
        },
        text = {
            Column {
                Text(
                    text = "Waybill #${duplicate.trackingNumber} has already been registered in the system.",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Current Status: ${duplicate.status.displayName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Platform: ${duplicate.platform.displayName} | ${duplicate.courier.displayName}", fontSize = 12.sp)
                        Text(text = "Original Scan: $dateStr", fontSize = 12.sp)
                        Text(text = "Operator: ${duplicate.scannedBy}", fontSize = 12.sp)
                        Text(text = "Recipient: ${duplicate.recipientName}", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "System Protection: Duplicate entry was blocked to prevent multiple dispatches and inventory discrepancies.",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                modifier = Modifier.testTag("dismiss_duplicate_dialog")
            ) {
                Text("Understood (Acknowledge)")
            }
        }
    )
}

@Composable
fun StateErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFEF3C7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "SOP Error",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "Logistics SOP Violation",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB45309)
            )
        },
        text = {
            Column {
                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Outbound Logistics Standard Workflow:\nScan → Validate → Prepare/Pack → Dispatch Handover → In-Transit → Delivered / Returned.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                modifier = Modifier.testTag("dismiss_error_dialog")
            ) {
                Text("Got it")
            }
        }
    )
}

@Composable
fun ChangeStatusDialog(
    shipment: ShipmentEntity,
    onDismiss: () -> Unit,
    onConfirm: (ShipmentStatus, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(shipment.status) }
    var notes by remember { mutableStateOf("") }

    // Helper to determine allowed transitions
    fun isTransitionAllowed(target: ShipmentStatus): Boolean {
        if (target == shipment.status) return true
        if (shipment.status.isTerminal) return false
        return when (shipment.status) {
            ShipmentStatus.SCANNED -> target == ShipmentStatus.PREPARED || target == ShipmentStatus.CANCELLED
            ShipmentStatus.PREPARED -> target == ShipmentStatus.DISPATCHED || target == ShipmentStatus.SCANNED || target == ShipmentStatus.CANCELLED
            ShipmentStatus.DISPATCHED -> target == ShipmentStatus.IN_TRANSIT || target == ShipmentStatus.CANCELLED
            ShipmentStatus.IN_TRANSIT -> target == ShipmentStatus.DELIVERED || target == ShipmentStatus.RETURNED || target == ShipmentStatus.CANCELLED
            else -> false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Update Status: #${shipment.trackingNumber}")
        },
        text = {
            Column {
                Text(
                    text = "Current Status: ${shipment.status.displayName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Select Next State:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                ShipmentStatus.entries.forEach { status ->
                    val allowed = isTransitionAllowed(status)
                    val isCurrent = status == shipment.status
                    val isSelected = status == selectedStatus

                    Surface(
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            allowed -> MaterialTheme.colorScheme.surface
                            else -> Color(0xFFF1F5F9)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable(enabled = allowed) {
                                selectedStatus = status
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(status = status)
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(Current)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (!allowed && !isCurrent) {
                                Text(
                                    text = "Invalid Jump",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Transition Note / Reason") },
                    placeholder = { Text("e.g. Packed in standard polymailer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus, notes) },
                enabled = selectedStatus != shipment.status && isTransitionAllowed(selectedStatus),
                modifier = Modifier.testTag("confirm_status_change")
            ) {
                Text("Confirm Transition")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBatchDialog(
    preparedShipments: List<ShipmentEntity>,
    onDismiss: () -> Unit,
    onConfirm: (CourierType, String, String, String, List<String>, String) -> Unit
) {
    var selectedCourier by remember { mutableStateOf(CourierType.SPX) }
    var courierExpanded by remember { mutableStateOf(false) }
    var driverName by remember { mutableStateOf("") }
    var driverPlate by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val selectedTracking = remember {
        mutableStateListOf<String>().apply {
            // Pre-select parcels matching the default courier
            addAll(preparedShipments.filter { it.courier == selectedCourier }.map { it.trackingNumber })
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Create Courier Dispatch Handover")
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(
                        text = "Module 5: Groups prepared parcels into a dispatch batch manifest upon rider handover.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Courier Dropdown
                    ExposedDropdownMenuBox(
                        expanded = courierExpanded,
                        onExpandedChange = { courierExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCourier.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Courier Partner") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courierExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = courierExpanded,
                            onDismissRequest = { courierExpanded = false }
                        ) {
                            CourierType.entries.forEach { courier ->
                                DropdownMenuItem(
                                    text = { Text(courier.displayName) },
                                    onClick = {
                                        selectedCourier = courier
                                        courierExpanded = false
                                        selectedTracking.clear()
                                        selectedTracking.addAll(preparedShipments.filter { it.courier == courier }.map { it.trackingNumber })
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = driverName,
                        onValueChange = { driverName = it },
                        label = { Text("Rider / Driver Name") },
                        placeholder = { Text("e.g. Kuya Alex Santos") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = driverPlate,
                            onValueChange = { driverPlate = it },
                            label = { Text("Vehicle Plate / ID") },
                            placeholder = { Text("ABC-1234") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = driverPhone,
                            onValueChange = { driverPhone = it },
                            label = { Text("Contact No.") },
                            placeholder = { Text("0917-XXX-XXXX") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Select Prepared Parcels for Handover (${selectedTracking.size} selected):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (preparedShipments.isEmpty()) {
                    item {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No prepared parcels ready for dispatch. Pack and change status to 'Prepared' first.",
                                fontSize = 12.sp,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                } else {
                    items(preparedShipments) { item ->
                        val isChecked = selectedTracking.contains(item.trackingNumber)
                        Surface(
                            color = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable {
                                    if (isChecked) selectedTracking.remove(item.trackingNumber)
                                    else selectedTracking.add(item.trackingNumber)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedTracking.add(item.trackingNumber)
                                        else selectedTracking.remove(item.trackingNumber)
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.trackingNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = "${item.platform.displayName} • ${item.weightKg} kg • ${item.recipientName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                CourierBadge(courier = item.courier)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Manifest Handover Notes") },
                        placeholder = { Text("e.g. Regular 4PM pickup, bags sealed") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedCourier,
                        driverName.ifBlank { "Assigned Courier Rider" },
                        driverPlate.ifBlank { "N/A" },
                        driverPhone.ifBlank { "N/A" },
                        selectedTracking.toList(),
                        notes
                    )
                },
                enabled = selectedTracking.isNotEmpty(),
                modifier = Modifier.testTag("confirm_create_batch")
            ) {
                Text("Confirm Handover (${selectedTracking.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogReturnDialog(
    initialShipment: ShipmentEntity?,
    allShipments: List<ShipmentEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, ReturnReason, ItemCondition, RefundStatus, Double, String) -> Unit
) {
    var trackingNumber by remember { mutableStateOf(initialShipment?.trackingNumber ?: "") }
    var returnReason by remember { mutableStateOf(ReturnReason.CUSTOMER_REJECTED_COD) }
    var itemCondition by remember { mutableStateOf(ItemCondition.PRISTINE_RESELLABLE) }
    var refundStatus by remember { mutableStateOf(RefundStatus.PENDING_VERIFICATION) }
    var claimAmount by remember { mutableStateOf(initialShipment?.declaredValue?.toString() ?: "0.0") }
    var notes by remember { mutableStateOf("") }

    var reasonExpanded by remember { mutableStateOf(false) }
    var conditionExpanded by remember { mutableStateOf(false) }
    var refundExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Log Returned Parcel (RTS)")
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text(
                        text = "Module 7: Handles returned shipments, evaluates item damage, and tracks refund / dispute statuses.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = trackingNumber,
                        onValueChange = { trackingNumber = it },
                        label = { Text("Waybill / Tracking Number") },
                        placeholder = { Text("e.g. SPXPH0492819283") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Return Reason Dropdown
                    ExposedDropdownMenuBox(
                        expanded = reasonExpanded,
                        onExpandedChange = { reasonExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = returnReason.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Return Reason") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = reasonExpanded,
                            onDismissRequest = { reasonExpanded = false }
                        ) {
                            ReturnReason.entries.forEach { reason ->
                                DropdownMenuItem(
                                    text = { Text(reason.label) },
                                    onClick = {
                                        returnReason = reason
                                        reasonExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Item Condition Dropdown
                    ExposedDropdownMenuBox(
                        expanded = conditionExpanded,
                        onExpandedChange = { conditionExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = itemCondition.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Item Condition Inspection") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = conditionExpanded,
                            onDismissRequest = { conditionExpanded = false }
                        ) {
                            ItemCondition.entries.forEach { cond ->
                                DropdownMenuItem(
                                    text = { Text(cond.label) },
                                    onClick = {
                                        itemCondition = cond
                                        conditionExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Refund Status Dropdown
                    ExposedDropdownMenuBox(
                        expanded = refundExpanded,
                        onExpandedChange = { refundExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = refundStatus.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Refund / Dispute Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = refundExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = refundExpanded,
                            onDismissRequest = { refundExpanded = false }
                        ) {
                            RefundStatus.entries.forEach { ref ->
                                DropdownMenuItem(
                                    text = { Text(ref.label) },
                                    onClick = {
                                        refundStatus = ref
                                        refundExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = claimAmount,
                        onValueChange = { claimAmount = it },
                        label = { Text("Claim / Refund Amount (₱)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Inspection Notes") },
                        placeholder = { Text("e.g. Unopened package, seal intact, returned to shelf A-3") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = claimAmount.toDoubleOrNull() ?: 0.0
                    onConfirm(trackingNumber.trim(), returnReason, itemCondition, refundStatus, amount, notes)
                },
                enabled = trackingNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                modifier = Modifier.testTag("confirm_log_return")
            ) {
                Text("Log Return Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RoleSwitchDialog(
    currentAccount: UserAccount,
    onDismiss: () -> Unit,
    onSelectAccount: (UserAccount) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Authentication & Role Management")
        },
        text = {
            Column {
                Text(
                    text = "Module 1: Demonstrates Role-Based Access Control (RBAC) required by capstone specification.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                UserAccount.ALL_ACCOUNTS.forEach { account ->
                    val isCurrent = account.id == currentAccount.id
                    Surface(
                        color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectAccount(account)
                                onDismiss()
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(account.avatarColorHex), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = account.fullName.take(1),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = account.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (account.role == UserRole.OWNER_ADMIN) Color(0xFFF97316) else Color(0xFF0D9488),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (account.role == UserRole.OWNER_ADMIN) "ADMIN" else "STAFF",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = account.role.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ShipmentDetailDialog(
    shipment: ShipmentEntity,
    onDismiss: () -> Unit
) {
    val df = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Waybill Details", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    BarcodeDrawerView(trackingNumber = shipment.trackingNumber)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PlatformBadge(platform = shipment.platform)
                            CourierBadge(courier = shipment.courier)
                        }
                        StatusBadge(status = shipment.status)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Customer / Recipient:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "${shipment.recipientName} (${shipment.recipientPhone})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = shipment.recipientAddress, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Package Contents:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = shipment.itemsSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Weight: ${shipment.weightKg} kg  •  Declared Value: ₱${shipment.declaredValue}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Lifecycle Timestamps Audit:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(6.dp))

                    val timestamps = listOfNotNull<Pair<String, String>>(
                        Pair("Scanned / Registered", df.format(Date(shipment.scannedAt))),
                        shipment.packedAt?.let { Pair("Packed & Prepared", df.format(Date(it))) },
                        shipment.dispatchedAt?.let { Pair("Dispatched to Courier", df.format(Date(it))) },
                        shipment.inTransitAt?.let { Pair("In Transit", df.format(Date(it))) },
                        shipment.completedAt?.let { Pair("Completed / Terminated", df.format(Date(it))) }
                    )

                    timestamps.forEach { (event, time) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = event, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (shipment.batchId != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Handover Batch ID: ${shipment.batchId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    if (shipment.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Notes: ${shipment.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

