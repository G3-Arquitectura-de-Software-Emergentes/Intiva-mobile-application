package com.resolum.intiva.core.navigation.graph

import com.resolum.intiva.features.agent.presentation.PlanScreen
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.resolum.intiva.core.navigation.components.IntivaBottomNavBar
import com.resolum.intiva.core.navigation.routes.NavRoutes
import com.resolum.intiva.features.finances.domain.models.TransactionType
import com.resolum.intiva.features.finances.presentation.HomeScreen
import com.resolum.intiva.features.finances.presentation.spendinglimits.SpendingLimitScreen
import com.resolum.intiva.features.finances.presentation.transactions.TransactionFormScreen
import com.resolum.intiva.features.finances.presentation.transactions.TransactionDetailScreen
import com.resolum.intiva.features.finances.presentation.transactions.TransactionsScreen
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.category.ManageCategoriesScreen
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.financialaccount.AccountDetailScreen
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.financialaccount.CreateFinancialAccountScreen
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.financialaccount.FinancialAccountScreen
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.financialaccount.EditFinancialAccountScreen
import com.resolum.intiva.features.savings.presentation.SavingsGoalCreateScreen
import com.resolum.intiva.features.savings.presentation.SavingsGoalDetailScreen
import com.resolum.intiva.features.savings.presentation.SavingsGoalEditScreen
import com.resolum.intiva.features.savings.presentation.SavingsGoalsScreen
import com.resolum.intiva.features.savings.presentation.completion.GoalCompletedScreen
import com.resolum.intiva.features.savings.presentation.completion.GoalUncompletedScreen
import com.resolum.intiva.features.savings.presentation.contribute.ContributeToGoalScreen
import com.resolum.intiva.features.shared.domain.model.OwnerType
import com.resolum.intiva.features.profiles.presentation.ProfileScreen
import com.resolum.intiva.features.profiles.presentation.EditProfileScreen
import com.resolum.intiva.features.profiles.presentation.ConfiguracionScreen
import com.resolum.intiva.features.profiles.presentation.PrivacidadSeguridadScreen
import com.resolum.intiva.features.profiles.presentation.CentroAyudaScreen
import com.resolum.intiva.features.communications.presentation.notifications.NotificationSettingsScreen
import com.resolum.intiva.features.communications.presentation.notifications.InAppNotificationsScreen
import com.resolum.intiva.features.profiles.presentation.AparienciaScreen
import com.resolum.intiva.features.household.presentation.family.FamilyScreen
import com.resolum.intiva.features.household.presentation.invite.InviteMemberScreen
import com.resolum.intiva.features.household.presentation.invitation.InvitationDetailScreen
import com.resolum.intiva.features.household.presentation.roles.FamilyRolesScreen


/**
 * Main shell of the app, containing the bottom navigation and root-level destinations.
 * Each feature's main screen should be registered here, with deeper navigation handled within the feature's own nav graph.
 */
@Composable
fun MainShell(
    onLogout: () -> Unit = {}
) {
    val shellNavController = rememberNavController()
    val navBackStackEntry by shellNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            if (currentRoute in NavRoutes.BOTTOM_NAV_ROUTES) {
                IntivaBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        shellNavController.navigate(route) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        NavHost(
            navController = shellNavController,
            startDestination = NavRoutes.HOME,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {

            composable(NavRoutes.HOME) {
                HomeScreen(
                    onNavigateToNewExpense = { shellNavController.navigate(NavRoutes.NEW_EXPENSE) },
                    onNavigateToNewIncome = { shellNavController.navigate(NavRoutes.NEW_INCOME) },
                    navController = shellNavController,
                    onNavigateToTransactions = { shellNavController.navigate(NavRoutes.TRANSACTIONS) },
                    onNavigateToSpendingLimitAlert = {
                        shellNavController.navigate(NavRoutes.SPENDING_LIMIT_ALERT)
                    },
                    onNavigateToTransactionDetail = { transactionId ->
                        shellNavController.navigate(NavRoutes.transactionDetail(transactionId))
                    },
                )
            }

            composable(NavRoutes.MONTHLY_PLAN) {
                PlanScreen(onBack = { shellNavController.popBackStack() })
            }

            composable(NavRoutes.TRANSACTIONS) {
                TransactionsScreen(
                    onNavigateToTransactionDetail = { transactionId ->
                        shellNavController.navigate(NavRoutes.transactionDetail(transactionId))
                    }
                )
            }

            composable(NavRoutes.TRANSACTION_DETAIL) { backStackEntry ->
                val transactionId = backStackEntry.arguments
                    ?.getString("transactionId")
                    ?.toLongOrNull()
                    ?: return@composable
                TransactionDetailScreen(
                    transactionId = transactionId,
                    onNavigateBack = { shellNavController.popBackStack() }
                )
            }

            composable(NavRoutes.SPENDING_LIMIT_ALERT) {
                SpendingLimitScreen(onNavigateBack = { shellNavController.popBackStack() })
            }

            composable(NavRoutes.FAMILY) {
                FamilyScreen(
                    onInviteClick = { shellNavController.navigate(NavRoutes.INVITE_MEMBER) },
                    onViewAllActivity = { shellNavController.navigate(NavRoutes.FAMILY_ROLES) },
                    onContributeClick = {
                        shellNavController.currentBackStackEntry?.savedStateHandle?.set("selectFamilyTab", true)
                        shellNavController.navigate(NavRoutes.SAVINGS_GOALS)
                    }
                )
            }

            composable(NavRoutes.INVITE_MEMBER) {
                InviteMemberScreen()
            }

            composable(NavRoutes.FAMILY_ROLES) {
                FamilyRolesScreen()
            }

            composable(NavRoutes.INVITATION_DETAIL) {
                InvitationDetailScreen()
            }

            composable(NavRoutes.PROFILE) {
                ProfileScreen(
                    onNavigateToEdit = { shellNavController.navigate(NavRoutes.EDIT_PROFILE) },
                    onNavigateToConfig = { shellNavController.navigate(NavRoutes.CONFIG) },
                    onPrivacyClick = { shellNavController.navigate(NavRoutes.PRIVACY) },
                    onHelpClick = { shellNavController.navigate(NavRoutes.HELP) },
                    onLogoutClick = onLogout
                )
            }

            composable(NavRoutes.EDIT_PROFILE) {
                EditProfileScreen(
                    onNavigateBack = { shellNavController.popBackStack() }
                )
            }

            composable(NavRoutes.CONFIG) {
                ConfiguracionScreen(
                    onNavigateToPersonalDetails = {
                        shellNavController.navigate(NavRoutes.EDIT_PROFILE)
                    },
                    onNavigateToCategories = {
                        shellNavController.navigate(NavRoutes.MANAGE_CATEGORIES)
                    },
                    onNavigateToNotifications = {
                        shellNavController.navigate(NavRoutes.NOTIFICATIONS)
                    },
                    onNavigateToAppearance = {
                        shellNavController.navigate(NavRoutes.APPEARANCE)
                    },
                    onNavigateToLinkedAccounts = {
                        shellNavController.navigate(NavRoutes.FINANCIAL_ACCOUNTS)
                    },
                    onNavigateBack = {
                        shellNavController.popBackStack()
                    }
                )
            }

            composable(NavRoutes.PRIVACY) {
                PrivacidadSeguridadScreen(
                    onNavigateBack = { shellNavController.popBackStack() }
                )
            }

            composable(NavRoutes.HELP) {
                CentroAyudaScreen(
                    onNavigateBack = { shellNavController.popBackStack() }
                )
            }

            composable(NavRoutes.NOTIFICATIONS) {
                NotificationSettingsScreen(
                    onNavigateBack = { shellNavController.popBackStack() }
                )
            }

            composable(NavRoutes.IN_APP_NOTIFICATIONS) {
                InAppNotificationsScreen(
                    onNavigateBack = { shellNavController.popBackStack() }
                )
            }

            composable(NavRoutes.APPEARANCE) {
                AparienciaScreen(
                    onNavigateBack = { shellNavController.popBackStack() }
                )
            }

            // Payment methods and categories
            composable(NavRoutes.MANAGE_CATEGORIES) {
                ManageCategoriesScreen(
                    onNavigateBack = { shellNavController.popBackStack() }
                )
            }

            composable(NavRoutes.FINANCIAL_ACCOUNTS) {
                FinancialAccountScreen(
                    onAddAccountClick = {
                        shellNavController.navigate(NavRoutes.CREATE_FINANCIAL_ACCOUNT)
                    },
                    onAccountClick = { accountId ->
                        shellNavController.navigate(NavRoutes.financialAccountDetail(accountId))
                    }
                )
            }

            composable(NavRoutes.CREATE_FINANCIAL_ACCOUNT) {
                CreateFinancialAccountScreen(
                    onAccountCreated = { shellNavController.popBackStack() },
                    onBackClick = { shellNavController.popBackStack() }
                )
            }

            composable(NavRoutes.FINANCIAL_ACCOUNT_DETAIL) {
                AccountDetailScreen(
                    onBackClick = { shellNavController.popBackStack() },
                    onEditClick = { accountId ->
                        shellNavController.navigate(NavRoutes.editFinancialAccount(accountId))
                    }
                )
            }

            composable(NavRoutes.EDIT_FINANCIAL_ACCOUNT) {
                EditFinancialAccountScreen(
                    onAccountUpdated = { shellNavController.popBackStack() },
                    onBackClick = { shellNavController.popBackStack() }
                )
            }

            /**
             * Savings Goals feature navigation.
             */
            composable(NavRoutes.SAVINGS_GOALS) {
                val selectFamilyTab = shellNavController
                    .previousBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("selectFamilyTab", false)
                    ?.collectAsState()
                    ?.value == true
                if (selectFamilyTab) {
                    shellNavController.previousBackStackEntry?.savedStateHandle?.set("selectFamilyTab", false)
                }
                SavingsGoalsScreen(
                    onNavigateBack = { shellNavController.popBackStack() },
                    onNavigateToCreate = { accountId ->
                        shellNavController.navigate("savings_goal_create/$accountId")
                    },
                    onNavigateToDetail = { accountId, goalId ->
                        shellNavController.navigate("savings_goal_detail/$accountId/$goalId")
                    },
                    onNavigateToEdit = { accountId, goalId ->
                        shellNavController.navigate("savings_goal_edit/$goalId")
                    },
                    selectFamilyTab = selectFamilyTab
                )
            }

            /**
             * The create screen requires the account ID to know which account the goal belongs to.
             */
            composable(NavRoutes.SAVINGS_GOAL_CREATE) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull()
                    ?: return@composable
                SavingsGoalCreateScreen(
                    accountId = accountId,
                    onNavigateBack = { shellNavController.popBackStack() },
                    onGoalCreated = { resolvedAccountId, goalId ->
                        shellNavController.navigate("savings_goal_detail/$resolvedAccountId/$goalId") {
                            popUpTo(NavRoutes.SAVINGS_GOALS)
                        }
                    }
                )
            }

            composable(NavRoutes.SAVINGS_GOAL_EDIT) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                SavingsGoalEditScreen(
                    goalId = id,
                    onNavigateBack = { shellNavController.popBackStack() },
                    onGoalUpdated = { shellNavController.popBackStack() },
                    onGoalCompleted = { goalId ->
                        shellNavController.navigate("savings_goal_completed/0/$goalId") {
                            popUpTo(NavRoutes.SAVINGS_GOALS)
                        }
                    }
                )
            }

            composable(NavRoutes.SAVINGS_GOAL_DETAIL) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: return@composable
                val goalId = backStackEntry.arguments?.getString("goalId") ?: return@composable
                SavingsGoalDetailScreen(
                    accountId = accountId,
                    goalId = goalId.toLongOrNull() ?: return@composable,
                    onNavigateBack = { shellNavController.popBackStack() },
                    onContributeClick = {
                        shellNavController.navigate("savings_goal_contribute/$accountId/$goalId")
                    },
                    onGoalCompleted = {
                        shellNavController.navigate("savings_goal_completed/$accountId/$goalId") {
                            popUpTo(NavRoutes.SAVINGS_GOALS)
                        }
                    },
                    onGoalUncompleted = {
                        shellNavController.navigate("savings_goal_uncompleted/$accountId/$goalId") {
                            popUpTo(NavRoutes.SAVINGS_GOALS)
                        }
                    }
                )
            }

            composable(NavRoutes.SAVINGS_GOAL_CONTRIBUTE) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: return@composable
                val goalId = backStackEntry.arguments?.getString("goalId")?.toLongOrNull() ?: return@composable
                ContributeToGoalScreen(
                    accountId = accountId,
                    goalId = goalId,
                    onNavigateBack = { shellNavController.popBackStack() },
                    onGoalCompleted = {
                        shellNavController.navigate("savings_goal_completed/$accountId/$goalId") {
                            popUpTo(NavRoutes.SAVINGS_GOALS)
                        }
                    },
                    onGoalUncompleted = {
                        shellNavController.navigate("savings_goal_uncompleted/$accountId/$goalId") {
                            popUpTo(NavRoutes.SAVINGS_GOALS)
                        }
                    },
                    onContributionSuccess = { _ -> shellNavController.popBackStack() }
                )
            }

            composable(NavRoutes.SAVINGS_GOAL_COMPLETED) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: return@composable
                val goalId = backStackEntry.arguments?.getString("goalId")?.toLongOrNull() ?: return@composable
                GoalCompletedScreen(
                    accountId = accountId,
                    goalId = goalId,
                    onNewGoal = {
                        shellNavController.navigate("savings_goal_create/$accountId") {
                            popUpTo(NavRoutes.SAVINGS_GOALS)
                        }
                    },
                    onBack = {
                        shellNavController.navigate(NavRoutes.HOME) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(NavRoutes.SAVINGS_GOAL_UNCOMPLETED) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: return@composable
                val goalId = backStackEntry.arguments?.getString("goalId")?.toLongOrNull() ?: return@composable
                GoalUncompletedScreen(
                    accountId = accountId,
                    goalId = goalId,
                    onTryAgain = {
                        shellNavController.navigate("savings_goal_contribute/$accountId/$goalId")
                    },
                    onBack = {
                        shellNavController.navigate(NavRoutes.SAVINGS_GOALS) {
                            popUpTo(NavRoutes.SAVINGS_GOALS) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.NEW_INCOME) {
                TransactionFormScreen(
                    transactionType = TransactionType.INCOME,
                    onDismiss = { shellNavController.popBackStack() },
                    navController = shellNavController,
                    ownerType = OwnerType.INDIVIDUAL
                )
            }

            composable(NavRoutes.NEW_EXPENSE) {
                TransactionFormScreen(
                    transactionType = TransactionType.EXPENSE,
                    onDismiss = { shellNavController.popBackStack() },
                    navController = shellNavController,
                    ownerType = OwnerType.INDIVIDUAL
                )
            }

        }
    }
}
