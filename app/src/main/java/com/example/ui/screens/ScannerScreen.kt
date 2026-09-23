package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourierType
import com.example.data.model.PlatformType
import com.example.ui.components.BarcodeDrawerView
import com.example.ui.components.CourierBadge
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsNavy
import com.example.ui.theme.LogisticsOrange
import com.example.ui.viewmodel.ShipTrackerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ShipTrackerViewModel,
    onScanSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rawBarcode by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("") }
    var recipientAddress by remember { mutableStateOf("") }
    var weightKgStr by remember { mutableStateOf("0.65") }
    var declaredValueStr by remember { mutableStateOf("850.0") }
    var itemsSummary by remember { mutableStateOf("") }
    var packingNotes by remember { mutableStateOf("") }

    var selectedPlatform by remember { mutableStateOf(PlatformType.SHOPEE) }
    var selectedCourier by remember { mutableStateOf(CourierType.SPX) }
    var ruleMatchedText by remember { mutableStateOf("Ready to scan") }
    var manualOverride by remember { mutableStateOf(false) }

    var platformMenuExpanded by remember { mutableStateOf(false) }
    var courierMenuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Auto-classify when rawBarcode changes
    LaunchedEffect(rawBarcode) {
        if (rawBarcode.isNotBlank()) {
            val result = viewModel.classifyBarcode(rawBarcode)
            if (!manualOverride) {
                selectedPlatform = result.platform
                selectedCourier = result.courier
            }
            ruleMatchedText = result.ruleMatched
        }
    }

    // Laser Animation for Reticle
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPos"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Module Title Card
        Card(
            colors = CardDefaults.cardColors(containerColor = LogisticsNavy),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(LogisticsBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Modules 3 & 4: Scanner & Classifier",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Format validation • Duplicate scan detection • Auto platform routing",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Animated Viewfinder Scanner Box
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Viewfinder lines and laser
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val padding = 30.dp.toPx()
                    val cornerLen = 24.dp.toPx()

                    // Laser beam
                    val laserY = padding + (h - 2 * padding) * laserPosition
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(padding, laserY),
                        end = Offset(w - padding, laserY),
                        strokeWidth = 3.dp.toPx()
                    )

                    // Target Reticle Corners
                    // Top-Left
                    drawLine(Color(0xFF38BDF8), Offset(padding, padding), Offset(padding + cornerLen, padding), 4f)
                    drawLine(Color(0xFF38BDF8), Offset(padding, padding), Offset(padding, padding + cornerLen), 4f)

                    // Top-Right
                    drawLine(Color(0xFF38BDF8), Offset(w - padding, padding), Offset(w - padding - cornerLen, padding), 4f)
                    drawLine(Color(0xFF38BDF8), Offset(w - padding, padding), Offset(w - padding, padding + cornerLen), 4f)

                    // Bottom-Left
                    drawLine(Color(0xFF38BDF8), Offset(padding, h - padding), Offset(padding + cornerLen, h - padding), 4f)
                    drawLine(Color(0xFF38BDF8), Offset(padding, h - padding), Offset(padding, h - padding - cornerLen), 4f)

                    // Bottom-Right
                    drawLine(Color(0xFF38BDF8), Offset(w - padding, h - padding), Offset(w - padding - cornerLen, h - padding), 4f)
                    drawLine(Color(0xFF38BDF8), Offset(w - padding, h - padding), Offset(w - padding, h - padding - cornerLen), 4f)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "POINT AT PARCEL WAYBILL",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Torch",
                            tint = Color.Yellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (rawBarcode.isNotBlank()) {
                        Surface(
                            color = Color(0xFF1E293B).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Text(
                                text = rawBarcode,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Tap a preset chip below or type waybill to simulate scan",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }

        // Quick Scan Preset Simulator Chips (Crucial for capstone testing on Android emulator)
        Text(
            text = "Quick-Scan Barcode Presets (Tap to Test Scenarios):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(
                "SPXPH0492819283" to "Shopee (Duplicate Test)",
                "SPXPH0991122334" to "Shopee SPX (New)",
                "LX092817291PH" to "Lazada LEX (Duplicate Test)",
                "LX088776655PH" to "Lazada LEX (New)",
                "TT982716253PH" to "TikTok Shop (Duplicate Test)",
                "TT911223344PH" to "TikTok Shop (New)",
                "JT8291029381PH" to "J&T Express",
                "FL992817265PH" to "Flash Express"
            )

            presets.forEach { (code, label) ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.clickable {
                        rawBarcode = code
                        manualOverride = false
                        // Check duplicate immediately on scan
                        scope.launch {
                            val dup = viewModel.checkDuplicate(code)
                            if (dup != null) {
                                viewModel.duplicateWarning.value = dup
                            }
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Manual Waybill Input Field
        OutlinedTextField(
            value = rawBarcode,
            onValueChange = {
                rawBarcode = it
                manualOverride = false
            },
            label = { Text("Parcel Tracking / Waybill Number *") },
            placeholder = { Text("e.g. SPXPH0492819283 or scan QR") },
            trailingIcon = {
                if (rawBarcode.isNotBlank()) {
                    IconButton(onClick = {
                        scope.launch {
                            val dup = viewModel.checkDuplicate(rawBarcode)
                            if (dup != null) {
                                viewModel.duplicateWarning.value = dup
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Check", tint = LogisticsBlue)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("waybill_input_field"),
            singleLine = true
        )

        // Module 4: Auto-Classification Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Classification Result (Auto-Detected)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (manualOverride) "Manual Override" else "Auto Rule",
                        fontSize = 10.sp,
                        color = if (manualOverride) LogisticsOrange else Color(0xFF059669),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformBadge(platform = selectedPlatform)
                    CourierBadge(courier = selectedCourier)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pattern Rule: $ruleMatchedText",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Override Dropdowns Toggle
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Platform Selector
                    ExposedDropdownMenuBox(
                        expanded = platformMenuExpanded,
                        onExpandedChange = { platformMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedPlatform.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Override Platform") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformMenuExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = platformMenuExpanded,
                            onDismissRequest = { platformMenuExpanded = false }
                        ) {
                            PlatformType.entries.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.displayName) },
                                    onClick = {
                                        selectedPlatform = p
                                        manualOverride = true
                                        platformMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Courier Selector
                    ExposedDropdownMenuBox(
                        expanded = courierMenuExpanded,
                        onExpandedChange = { courierMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCourier.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Override Courier") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courierMenuExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = courierMenuExpanded,
                            onDismissRequest = { courierMenuExpanded = false }
                        ) {
                            CourierType.entries.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.displayName) },
                                    onClick = {
                                        selectedCourier = c
                                        manualOverride = true
                                        courierMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Additional Parcel Manifest Details (Module 2)
        Text(
            text = "Parcel Details & Packing Manifest:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = weightKgStr,
                onValueChange = { weightKgStr = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = declaredValueStr,
                onValueChange = { declaredValueStr = it },
                label = { Text("Declared Value (₱)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = itemsSummary,
            onValueChange = { itemsSummary = it },
            label = { Text("Items Description / SKU") },
            placeholder = { Text("e.g. Wireless Headset, 10000mAh Powerbank") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it },
                label = { Text("Recipient Name") },
                placeholder = { Text("Buyer Name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = recipientPhone,
                onValueChange = { recipientPhone = it },
                label = { Text("Phone") },
                placeholder = { Text("09XX-XXX-XXXX") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = recipientAddress,
            onValueChange = { recipientAddress = it },
            label = { Text("Delivery Address") },
            placeholder = { Text("City, Province") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = packingNotes,
            onValueChange = { packingNotes = it },
            label = { Text("Packing Notes") },
            placeholder = { Text("e.g. Fragile, add bubble wrap, sealed in box") },
            modifier = Modifier.fillMaxWidth()
        )

        // Action Submit Button
        Button(
            onClick = {
                val weight = weightKgStr.toDoubleOrNull() ?: 0.5
                val value = declaredValueStr.toDoubleOrNull() ?: 500.0
                viewModel.registerScannedParcel(
                    trackingNumber = rawBarcode.trim(),
                    recipientName = recipientName,
                    recipientPhone = recipientPhone,
                    recipientAddress = recipientAddress,
                    platform = selectedPlatform,
                    courier = selectedCourier,
                    weightKg = weight,
                    declaredValue = value,
                    itemsSummary = itemsSummary,
                    notes = packingNotes
                )
                // Clear fields for next scan
                rawBarcode = ""
                itemsSummary = ""
                recipientName = ""
                recipientAddress = ""
                packingNotes = ""
                onScanSuccess()
            },
            enabled = rawBarcode.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = LogisticsBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_scan_button")
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register Scanned Parcel", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
