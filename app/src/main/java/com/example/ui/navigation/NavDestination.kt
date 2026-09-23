package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Dashboard),
    SCAN("scan", "Scanner", Icons.Default.QrCodeScanner),
    SHIPMENTS("shipments", "Shipments", Icons.Default.ViewList),
    DISPATCH("dispatch", "Dispatch", Icons.Default.LocalShipping),
    RETURNS("returns", "Returns", Icons.Default.AssignmentReturn),
    REPORTS("reports", "Reports", Icons.Default.Description),
    SYNC("sync", "Sync Queue", Icons.Default.Sync),
    ACCOUNT("account", "Account", Icons.Default.Person)
}
