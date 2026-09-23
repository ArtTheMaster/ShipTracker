package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.ShipmentEntity
import com.example.data.repository.LogisticsRepository
import com.example.ui.components.ChangeStatusDialog
import com.example.ui.components.CreateBatchDialog
import com.example.ui.components.DuplicateScanDialog
import com.example.ui.components.LogReturnDialog
import com.example.ui.components.RoleSwitchDialog
import com.example.ui.components.ShipmentDetailDialog
import com.example.ui.components.StateErrorDialog
import com.example.ui.components.TopAppHeader
import com.example.ui.navigation.NavDestination
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DispatchScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ReturnsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.ShipmentsScreen
import com.example.ui.screens.SyncScreen
import com.example.ui.theme.LogisticsBlue
import com.example.ui.theme.LogisticsNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShipTrackerViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LogisticsRepository(database)

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ShipTrackerViewModel(repository) as T
            }
        }

        setContent {
            MyApplicationTheme {
                val vm: ShipTrackerViewModel = viewModel(factory = viewModelFactory)
                MainAppScreen(viewModel = vm)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: ShipTrackerViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
    val duplicateWarning by viewModel.duplicateWarning.collectAsStateWithLifecycle()
    val stateError by viewModel.stateTransitionError.collectAsStateWithLifecycle()
    val preparedParcels by viewModel.preparedForDispatch.collectAsStateWithLifecycle()
    val allShipments by viewModel.allShipments.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(NavDestination.DASHBOARD) }

    // Dialog States
    var showRoleSwitchDialog by remember { mutableStateOf(false) }
    var statusChangeShipment by remember { mutableStateOf<ShipmentEntity?>(null) }
    var returnShipmentTarget by remember { mutableStateOf<ShipmentEntity?>(null) }
    var showCreateBatchDialog by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }
    var detailShipment by remember { mutableStateOf<ShipmentEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppHeader(
                currentUser = currentUser,
                isOnline = isOnline,
                pendingSyncCount = pendingSyncCount,
                onToggleOnline = { viewModel.toggleOnlineStatus() },
                onRoleSwitchClick = { showRoleSwitchDialog = true },
                onSyncClick = { currentDestination = NavDestination.SYNC }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = LogisticsNavy,
                contentColor = Color.White
            ) {
                val navItems = listOf(
                    NavDestination.DASHBOARD,
                    NavDestination.SCAN,
                    NavDestination.SHIPMENTS,
                    NavDestination.DISPATCH,
                    NavDestination.RETURNS,
                    NavDestination.REPORTS
                )

                navItems.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = LogisticsBlue,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_${destination.route}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                NavDestination.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToScan = { currentDestination = NavDestination.SCAN },
                    onNavigateToDispatch = { currentDestination = NavDestination.DISPATCH },
                    onNavigateToReturns = { currentDestination = NavDestination.RETURNS },
                    onNavigateToReports = { currentDestination = NavDestination.REPORTS },
                    onNavigateToShipments = { currentDestination = NavDestination.SHIPMENTS }
                )

                NavDestination.SCAN -> ScannerScreen(
                    viewModel = viewModel,
                    onScanSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Outbound parcel scanned & verified!")
                        }
                        currentDestination = NavDestination.SHIPMENTS
                    }
                )

                NavDestination.SHIPMENTS -> ShipmentsScreen(
                    viewModel = viewModel,
                    onOpenStatusDialog = { item -> statusChangeShipment = item },
                    onOpenReturnDialog = { item ->
                        returnShipmentTarget = item
                        showReturnDialog = true
                    },
                    onOpenDetailSheet = { item -> detailShipment = item }
                )

                NavDestination.DISPATCH -> DispatchScreen(
                    viewModel = viewModel,
                    onCreateBatchClick = { showCreateBatchDialog = true }
                )

                NavDestination.RETURNS -> ReturnsScreen(
                    viewModel = viewModel,
                    onLogNewReturnClick = {
                        returnShipmentTarget = null
                        showReturnDialog = true
                    }
                )

                NavDestination.REPORTS -> ReportsScreen(viewModel = viewModel)

                NavDestination.SYNC -> SyncScreen(viewModel = viewModel)

                NavDestination.ACCOUNT -> AccountScreen(viewModel = viewModel)
            }
        }
    }

    // Duplicate Scan Dialog
    duplicateWarning?.let { dup ->
        DuplicateScanDialog(
            duplicate = dup,
            onDismiss = { viewModel.clearDuplicateWarning() }
        )
    }

    // State SOP Violation Dialog
    stateError?.let { err ->
        StateErrorDialog(
            errorMessage = err,
            onDismiss = { viewModel.clearStateError() }
        )
    }

    // Change Status Transition Dialog
    statusChangeShipment?.let { shipment ->
        ChangeStatusDialog(
            shipment = shipment,
            onDismiss = { statusChangeShipment = null },
            onConfirm = { newStatus, notes ->
                viewModel.updateShipmentStatus(shipment.trackingNumber, newStatus, notes)
                statusChangeShipment = null
            }
        )
    }

    // Create Dispatch Batch Dialog
    if (showCreateBatchDialog) {
        CreateBatchDialog(
            preparedShipments = preparedParcels,
            onDismiss = { showCreateBatchDialog = false },
            onConfirm = { courier, driver, plate, phone, trackingList, notes ->
                viewModel.createDispatchBatch(courier, driver, plate, phone, trackingList, notes)
                showCreateBatchDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Dispatch handover batch created for $driver ($plate)")
                }
            }
        )
    }

    // Log Return Dialog
    if (showReturnDialog) {
        LogReturnDialog(
            initialShipment = returnShipmentTarget,
            allShipments = allShipments,
            onDismiss = {
                showReturnDialog = false
                returnShipmentTarget = null
            },
            onConfirm = { tracking, reason, condition, refundStatus, amount, notes ->
                viewModel.logReturn(tracking, reason, condition, refundStatus, amount, notes)
                showReturnDialog = false
                returnShipmentTarget = null
                scope.launch {
                    snackbarHostState.showSnackbar("Return record logged for waybill #$tracking")
                }
            }
        )
    }

    // Role Switch Dialog
    if (showRoleSwitchDialog) {
        RoleSwitchDialog(
            currentAccount = currentUser,
            onDismiss = { showRoleSwitchDialog = false },
            onSelectAccount = { account ->
                viewModel.switchUser(account)
                scope.launch {
                    snackbarHostState.showSnackbar("Switched to ${account.fullName} (${account.role.displayName})")
                }
            }
        )
    }

    // Shipment Detail Dialog
    detailShipment?.let { shipment ->
        ShipmentDetailDialog(
            shipment = shipment,
            onDismiss = { detailShipment = null }
        )
    }
}
