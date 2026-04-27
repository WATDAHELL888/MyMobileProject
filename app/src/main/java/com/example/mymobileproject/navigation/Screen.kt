package com.example.mymobileproject.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object TransactionList : Screen("transactions")
    data object AddTransaction : Screen("add_transaction")
    data object GroupList : Screen("groups")
    data object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
    data object CreateGroup : Screen("create_group")
    data object AddGroupExpense : Screen("add_group_expense/{groupId}") {
        fun createRoute(groupId: String) = "add_group_expense/$groupId"
    }
    data object AIChat : Screen("ai_chat")
    data object ReceiptScan : Screen("receipt_scan")
}
