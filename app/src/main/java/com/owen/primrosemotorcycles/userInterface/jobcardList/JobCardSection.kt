package com.owen.primrosemotorcycles.userInterface.jobcardList

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.owen.primrosemotorcycles.userInterface.checkup.CheckupDetailScreen
import com.owen.primrosemotorcycles.userInterface.checkup.CheckupFormScreen
import com.owen.primrosemotorcycles.userInterface.checkup.CheckupListScreen
import com.owen.primrosemotorcycles.userInterface.invoice.CreateInvoiceScreen
import com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails.TabletJobCardLayout
import com.owen.primrosemotorcycles.userInterface.quotation.CreateQuotationScreen
import com.owen.primrosemotorcycles.userInterface.stock.StockManagementScreen

@Composable
fun JobCardSection(
    modifier: Modifier = Modifier,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val listVm = hiltViewModel<JobCardListViewModel>()
    val uiState by listVm.uiState.collectAsState()

    // TABLET / DESKTOP - 3 Pane, NO NavHost
    if (windowWidthSizeClass == WindowWidthSizeClass.Expanded) {
        TabletJobCardLayout(
            uiState = uiState,
            listVm = listVm,
            modifier = modifier
        )
        return
    }

    // PHONE - NavigationSuiteScaffold with bottom nav items
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route ?: "list"

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = currentDestination == "list" || currentDestination.startsWith("create"),
                onClick = {
                    if (currentDestination != "list") {
                        navController.navigate("list") {
                            popUpTo("list") { inclusive = true }
                        }
                    }
                },
                icon = { Icon(Icons.Default.Build, contentDescription = "Job Cards") },
                label = { Text("Job Cards") }
            )
            item(
                selected = currentDestination.startsWith("checkup"),
                onClick = {
                    if (!currentDestination.startsWith("checkup")) {
                        navController.navigate("checkups") {
                            popUpTo("list") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Checkups") },
                label = { Text("Checkups") }
            )
            item(
                selected = currentDestination == "stock",
                onClick = {
                    if (currentDestination != "stock") {
                        navController.navigate("stock") {
                            popUpTo("list") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(Icons.Default.Inventory2, contentDescription = "Stock") },
                label = { Text("Stock") }
            )
        },
        modifier = modifier
    ) {
        NavHost(navController = navController, startDestination = "list") {
            composable("list") {
                JobCardListScreen(
                    uiState = uiState,
                    windowWidthSizeClass = windowWidthSizeClass,
                    onEvent = { event ->
                        when (event) {
                            is JobListEvent.AddClicked -> {
                                listVm.resetForNewJobCard()
                                navController.navigate("create")
                            }
                            is JobListEvent.CardClicked -> navController.navigate("create/${event.id}")
                            is JobListEvent.StockClicked -> navController.navigate("stock")
                            is JobListEvent.SearchChanged -> listVm.onSearchChange(event.query)
                            is JobListEvent.StatusSelected -> listVm.onStatusFilterChange(event.status)
                            is JobListEvent.ClearFilters -> listVm.clearFilters()
                        }
                    }
                )
            }
            composable("create") {
                CreateJobCardScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    dao = listVm.dao, firebase = listVm.firestore,
                    navController = navController, jobId = null,
                    viewModel = listVm
                )
            }
            composable("create/{jobId}", arguments = listOf(navArgument("jobId") { type = NavType.StringType })) { backStack ->
                val jobId = backStack.arguments?.getString("jobId") ?: ""
                CreateJobCardScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    dao = listVm.dao, firebase = listVm.firestore,
                    navController = navController, jobId = jobId,
                    viewModel = listVm
                )
            }
            composable("create_quotation/{jobId}", arguments = listOf(navArgument("jobId") { type = NavType.StringType })) { backStack ->
                CreateQuotationScreen(jobId = backStack.arguments?.getString("jobId") ?: "", navController = navController, onBack = { navController.popBackStack() })
            }
            composable("create_invoice/{jobId}", arguments = listOf(navArgument("jobId") { type = NavType.StringType })) { backStack ->
                CreateInvoiceScreen(jobId = backStack.arguments?.getString("jobId") ?: "", navController = navController, onBack = { navController.popBackStack() })
            }
            composable("stock") {
                StockManagementScreen(onScanBarcode = {}, onBack = { navController.popBackStack() })
            }

            composable("checkups") {
                CheckupListScreen(
                    onStartCheckup = { bike -> navController.navigate("checkup_form/$bike") },
                    onOpenCheckup = { id -> navController.navigate("checkup_detail/$id") },
                    dao = listVm.checkupDao
                )
            }
            composable("checkup_form/{bike}") { backStack ->
                val bike = backStack.arguments?.getString("bike") ?: ""
                CheckupFormScreen(
                    bikeReg = bike,
                    onFixNow = { reg, issue ->
                        listVm.resetForNewJobCard()
                        listVm.updateBikeReg(reg)
                        listVm.updateCustomerInstructions("Weekly Checkup Issue: $issue")
                        navController.navigate("create")
                    },
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(
                "checkup_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStack ->
                val id = backStack.arguments?.getInt("id") ?: 0
                CheckupDetailScreen(
                    checkupId = id,
                    dao = listVm.checkupDao,
                    onBack = { navController.popBackStack() },
                    onFixNow = { reg, issue ->
                        listVm.resetForNewJobCard()
                        listVm.updateBikeReg(reg)
                        listVm.updateCustomerInstructions("Weekly Checkup Issue: $issue")
                        navController.navigate("create")
                    }
                )
            }
        }
    }
}
