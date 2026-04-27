package com.example.mymobileproject.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mymobileproject.R
import com.example.mymobileproject.presentation.ai.AIChatScreen
import com.example.mymobileproject.presentation.auth.LoginScreen
import com.example.mymobileproject.presentation.dashboard.DashboardScreen
import com.example.mymobileproject.presentation.group.AddGroupExpenseScreen
import com.example.mymobileproject.presentation.group.CreateGroupScreen
import com.example.mymobileproject.presentation.group.GroupDetailScreen
import com.example.mymobileproject.presentation.group.GroupListScreen
import com.example.mymobileproject.presentation.receipt.ReceiptScanScreen
import com.example.mymobileproject.presentation.transaction.AddTransactionScreen
import com.example.mymobileproject.presentation.transaction.TransactionListScreen
import com.example.mymobileproject.presentation.splash.SplashScreen
import com.example.mymobileproject.ui.theme.DarkCard

data class BottomNavItem(
    val route: String,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, R.string.nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    BottomNavItem(Screen.TransactionList.route, R.string.nav_transactions, Icons.Filled.Receipt, Icons.Outlined.Receipt),
    BottomNavItem(Screen.GroupList.route, R.string.nav_groups, Icons.Filled.Group, Icons.Outlined.Group),
    BottomNavItem(Screen.AIChat.route, R.string.nav_ai_chat, Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
)

// Animation duration
private const val ANIM_DURATION = 350

// Slide-up enter (for modal-style sub-screens like AddTransaction)
private val slideUpEnter = slideInVertically(tween(ANIM_DURATION, easing = FastOutSlowInEasing)) { it / 3 } +
        fadeIn(tween(ANIM_DURATION, easing = FastOutSlowInEasing))
private val slideDownExit = slideOutVertically(tween(ANIM_DURATION, easing = FastOutSlowInEasing)) { it / 3 } +
        fadeOut(tween(ANIM_DURATION, easing = FastOutSlowInEasing))

// Slide-right enter (for detail screens like GroupDetail)
private val slideRightEnter = slideInHorizontally(tween(ANIM_DURATION, easing = FastOutSlowInEasing)) { it / 3 } +
        fadeIn(tween(ANIM_DURATION, easing = FastOutSlowInEasing))
private val slideLeftExit = slideOutHorizontally(tween(ANIM_DURATION, easing = FastOutSlowInEasing)) { it / 3 } +
        fadeOut(tween(ANIM_DURATION, easing = FastOutSlowInEasing))

@Composable
fun SmartFinanceNavGraph(
    isLoggedIn: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = Screen.Splash.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500))
            ) {
                NavigationBar(containerColor = DarkCard) {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy?.any {
                            it.route == item.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.titleRes)
                                )
                            },
                            label = { Text(stringResource(item.titleRes)) },
                            colors = NavigationBarItemDefaults.colors()
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val tabRoutes = bottomNavItems.map { it.route }
        fun getTabIndex(route: String?) = tabRoutes.indexOf(route)

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                val initialIndex = getTabIndex(initialState.destination.route)
                val targetIndex = getTabIndex(targetState.destination.route)
                if (initialIndex != -1 && targetIndex != -1 && initialIndex != targetIndex) {
                    val direction = if (targetIndex > initialIndex) 1 else -1
                    slideInHorizontally(tween(ANIM_DURATION, easing = FastOutSlowInEasing)) { it / 4 * direction } + fadeIn(tween(ANIM_DURATION))
                } else {
                    fadeIn(tween(250))
                }
            },
            exitTransition = {
                val initialIndex = getTabIndex(initialState.destination.route)
                val targetIndex = getTabIndex(targetState.destination.route)
                if (initialIndex != -1 && targetIndex != -1 && initialIndex != targetIndex) {
                    val direction = if (targetIndex > initialIndex) -1 else 1
                    slideOutHorizontally(tween(ANIM_DURATION, easing = FastOutSlowInEasing)) { it / 4 * direction } + fadeOut(tween(ANIM_DURATION))
                } else {
                    fadeOut(tween(250))
                }
            },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { fadeOut(tween(250)) }
        ) {
            // ── Splash ──
            composable(
                route = Screen.Splash.route,
                enterTransition = { fadeIn(tween(0)) },
                exitTransition = { fadeOut(tween(500)) }
            ) {
                SplashScreen(
                    onFinished = {
                        val dest = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
                        navController.navigate(dest) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Login ──
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Tab Screens (smooth crossfade by default) ──
            composable(
                route = Screen.Dashboard.route,
                enterTransition = {
                    if (initialState.destination.route == Screen.Splash.route) {
                        fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 20 }
                    } else null // Fallback to NavHost default
                }
            ) {
                DashboardScreen(
                    onNavigateToTransactions = { navController.navigate(Screen.TransactionList.route) },
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateToReceiptScan = { navController.navigate(Screen.ReceiptScan.route) }
                )
            }

            composable(Screen.TransactionList.route) {
                TransactionListScreen(
                    onNavigateToAdd = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateToReceiptScan = { navController.navigate(Screen.ReceiptScan.route) }
                )
            }

            composable(Screen.GroupList.route) {
                GroupListScreen(
                    onNavigateToGroup = { groupId ->
                        navController.navigate(Screen.GroupDetail.createRoute(groupId))
                    },
                    onNavigateToCreate = { navController.navigate(Screen.CreateGroup.route) }
                )
            }

            composable(Screen.AIChat.route) {
                AIChatScreen()
            }

            // ── Sub Screens: Slide-up (modal style) ──
            composable(
                route = Screen.AddTransaction.route,
                enterTransition = { slideUpEnter },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { slideDownExit }
            ) {
                AddTransactionScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.CreateGroup.route,
                enterTransition = { slideUpEnter },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { slideDownExit }
            ) {
                CreateGroupScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.ReceiptScan.route,
                enterTransition = { slideUpEnter },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { slideDownExit }
            ) {
                ReceiptScanScreen(onNavigateBack = { navController.popBackStack() })
            }

            // ── Sub Screens: Slide-right (detail style) ──
            composable(
                route = Screen.GroupDetail.route,
                arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
                enterTransition = { slideRightEnter },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { slideLeftExit }
            ) {
                GroupDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddExpense = { groupId ->
                        navController.navigate(Screen.AddGroupExpense.createRoute(groupId))
                    }
                )
            }

            composable(
                route = Screen.AddGroupExpense.route,
                arguments = listOf(navArgument("groupId") { type = NavType.StringType }),
                enterTransition = { slideUpEnter },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { slideDownExit }
            ) {
                AddGroupExpenseScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
