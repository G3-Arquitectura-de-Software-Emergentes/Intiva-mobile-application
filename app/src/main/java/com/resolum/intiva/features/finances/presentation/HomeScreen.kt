package com.resolum.intiva.features.finances.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.resolum.intiva.core.navigation.routes.NavRoutes
import com.resolum.intiva.core.common.state.UiState
import com.resolum.intiva.core.ui.snackbar.IntivaSnackBarHost
import com.resolum.intiva.core.ui.snackbar.SnackBarType
import com.resolum.intiva.core.ui.snackbar.SnackBarVisualsWithType
import com.resolum.intiva.core.ui.theme.IntivaColors
import com.resolum.intiva.features.finances.presentation.spendinglimits.SpendingLimitViewModel
import com.resolum.intiva.features.finances.presentation.spendinglimits.components.SpendingLimitCard
import com.resolum.intiva.features.finances.presentation.transactions.TransactionViewModel
import com.resolum.intiva.features.finances.presentation.transactions.components.TransactionItem
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.category.CategoryViewModel
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.financialaccount.FinancialAccountViewModel
import com.resolum.intiva.features.profiles.domain.models.FirstTransactionTutorialStep
import com.resolum.intiva.features.profiles.presentation.onboarding.OnboardingViewModel
import com.resolum.intiva.features.profiles.presentation.onboarding.components.FirstTransactionPresentationOverlay
import com.resolum.intiva.features.profiles.presentation.onboarding.components.SpotlightOverlay
import com.resolum.intiva.features.profiles.presentation.ProfileViewModel
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

/**
 * HomeScreen.kt
 *
 * This file defines the HomeScreen composable, which serves as the main dashboard for the Intiva app.
 * It displays a greeting, total balance, quick action buttons for adding expenses and incomes, a monthly spending limit indicator,
 * and a list of recent transactions. The screen also integrates an onboarding tutorial for first-time users.
 *
 * Key Features:
 * - Displays user's total balance and a monthly spending limit indicator.
 * - Provides quick access buttons to add new expenses and incomes.
 * - Shows recent transactions with a link to view all transactions.
 * - Integrates an onboarding tutorial that guides users through their first transaction entry.
 *
 * The HomeScreen interacts with the TransactionViewModel to fetch and display transaction data,
 * and with the OnboardingViewModel to manage the state of the onboarding tutorial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNewExpense: () -> Unit,
    onNavigateToNewIncome: () -> Unit,
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel(),
    spendingLimitViewModel: SpendingLimitViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    financialAccountViewModel: FinancialAccountViewModel = hiltViewModel(),
    onNavigateToTransactions: () -> Unit,
    onNavigateToSpendingLimitAlert: () -> Unit = {},
    onNavigateToTransactionDetail: (Long) -> Unit = {}
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    val spendingLimitUiState by spendingLimitViewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val financialAccountUiState by financialAccountViewModel.uiState.collectAsState()
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingState by onboardingViewModel.state.collectAsState()
    var incomeButtonRect by remember { mutableStateOf<Rect?>(null) }

    var isNavigatingToForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onboardingViewModel.loadStatus()
        viewModel.getTransactionsByOwnerId(onlyLatest = true)
        spendingLimitViewModel.loadMonthlySpendingLimit()
        spendingLimitViewModel.loadSpendingLimits()
        categoryViewModel.getCategories(
            ownerType = com.resolum.intiva.features.shared.domain.model.OwnerType.INDIVIDUAL.name,
            type = com.resolum.intiva.features.finances.domain.models.TransactionType.EXPENSE.name
        )
        profileViewModel.loadProfile()

        val success = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.remove<Boolean>("transaction_success")

        if (success == true) {
            snackBarHostState.showSnackbar(
                SnackBarVisualsWithType(
                    message = "Transacción registrada exitosamente",
                    type = SnackBarType.Success
                )
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                financialAccountViewModel.getFinancialAccounts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(onboardingState.step) {
        when (onboardingState.step) {
            FirstTransactionTutorialStep.SELECT_CATEGORY,
            FirstTransactionTutorialStep.CONFIRM_TRANSACTION -> {
                if (!isNavigatingToForm) {
                    onboardingViewModel.rollback()
                }
                isNavigatingToForm = false
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val profile = (profileUiState.profileState as? UiState.Success)?.data
                        val avatarUrl = profile?.avatarUrl?.ifEmpty { null }
                        AsyncImage(
                            model = avatarUrl ?: "https://res.cloudinary.com/dcppsmlzd/image/upload/v1781121388/avatar_default_kf0yvc.png",
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = profile?.name?.split(" ")?.firstOrNull() ?: "Intiva",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = IntivaColors.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoutes.IN_APP_NOTIFICATIONS) }) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = IntivaColors.IconPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White,
        snackbarHost = { IntivaSnackBarHost(hostState = snackBarHostState) }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {

                item {
                    val profile = (profileUiState.profileState as? UiState.Success)?.data
                    val firstName = profile?.name?.split(" ")?.firstOrNull() ?: "Usuario"
                    Column {
                        Text(
                            text = "Hola, $firstName 👋",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = IntivaColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aquí está el resumen de tus finanzas hoy.",
                            fontSize = 14.sp,
                            color = IntivaColors.TextSecondary
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = IntivaColors.BackgroundPurple
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "BALANCE TOTAL",
                                fontSize = 12.sp,
                                color = Color(0xCCFFFFFF),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(text = "S/ ", fontSize = 20.sp, color = Color.White)
                                Text(
                                    text = String.format("%,.2f", financialAccountViewModel.totalBalance),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToNewExpense() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = IntivaColors.PrimaryGreen
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF0D0D0D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Gasto",
                                        color = Color(0xFF0D0D0D),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (onboardingState.step == FirstTransactionTutorialStep.OPEN_CREATE_TRANSACTION) {
                                            isNavigatingToForm = true
                                            onboardingViewModel.advance()
                                        }
                                        onNavigateToNewIncome()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .onGloballyPositioned { cords ->
                                            incomeButtonRect = cords.boundsInRoot()
                                        },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF6B5FD4)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Ingreso",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ver cuentas >",
                                    color = Color(0xCCFFFFFF),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                item {
                    when (val state = spendingLimitUiState.spendingLimitsState) {
                        is UiState.Success -> {
                            if (state.data.isEmpty()) {
                                SpendingLimitCard(
                                    state = UiState.Idle,
                                    onRetry = { spendingLimitViewModel.loadSpendingLimits() },
                                    onOpenAlert = onNavigateToSpendingLimitAlert
                                )
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 0.dp)
                                ) {
                                    items(state.data) { summary ->
                                        SpendingLimitCard(
                                            state = UiState.Success(summary),
                                            category = categoryUiState.categories.firstOrNull {
                                                it.id == summary.limit.targetId
                                            },
                                            onRetry = { spendingLimitViewModel.loadSpendingLimits() },
                                            onOpenAlert = onNavigateToSpendingLimitAlert,
                                            modifier = Modifier.fillParentMaxWidth(0.92f)
                                        )
                                    }
                                }
                            }
                        }

                        is UiState.Loading -> SpendingLimitCard(
                            state = UiState.Loading,
                            onRetry = { spendingLimitViewModel.loadSpendingLimits() },
                            onOpenAlert = onNavigateToSpendingLimitAlert
                        )

                        is UiState.Error -> SpendingLimitCard(
                            state = UiState.Error(message = state.message, throwable = state.throwable),
                            onRetry = { spendingLimitViewModel.loadSpendingLimits() },
                            onOpenAlert = onNavigateToSpendingLimitAlert
                        )

                        is UiState.Idle -> SpendingLimitCard(
                            state = spendingLimitUiState.spendingLimitState,
                            onRetry = { spendingLimitViewModel.loadSpendingLimits() },
                            onOpenAlert = onNavigateToSpendingLimitAlert
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Actividad Reciente",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = IntivaColors.TextPrimary
                        )
                        Text(
                            text = "Ver todo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = IntivaColors.IconPurple,
                            modifier = Modifier.clickable { onNavigateToTransactions() }
                        )
                    }
                }

                item {
                    when (val state = uiState.transactionsState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = IntivaColors.IconPurple)
                            }
                        }
                        is UiState.Success -> {
                            val allTransactions = state.data.flatMap { it.transactions }
                            if (allTransactions.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = IntivaColors.IconPurple.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        text = "Aún no tienes transacciones",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = IntivaColors.TextPrimary
                                    )
                                    Text(
                                        text = "Registra tu primer ingreso o gasto\npara empezar a ver tu historial.",
                                        fontSize = 13.sp,
                                        color = IntivaColors.TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    allTransactions.forEach { transaction ->
                                        TransactionItem(
                                            transaction = transaction,
                                            onClick = { selectedTransaction ->
                                                onNavigateToTransactionDetail(selectedTransaction.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Text(
                                text = "Error al cargar transacciones",
                                color = Color.Red,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        else -> Unit
                    }
                }
            }

            if (onboardingState.step == FirstTransactionTutorialStep.PRESENTATION) {
                FirstTransactionPresentationOverlay(
                    onStart = { onboardingViewModel.advance() },
                    onSkip = { onboardingViewModel.skip() }
                )
            } else if (onboardingState.step == FirstTransactionTutorialStep.OPEN_CREATE_TRANSACTION
                && incomeButtonRect != null
            ) {
                SpotlightOverlay(
                    rect = incomeButtonRect,
                    title = "Registra tu primer ingreso",
                    message = "Toca el botón Ingreso para comenzar a llevar el control de tus finanzas.",
                    stepNumber = 2,
                    totalSteps = 5,
                    onNext = { onboardingViewModel.advance() }
                )
            }
        }
    }
}
