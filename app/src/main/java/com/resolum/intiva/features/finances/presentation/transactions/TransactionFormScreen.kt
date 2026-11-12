package com.resolum.intiva.features.finances.presentation.transactions

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import com.resolum.intiva.features.agent.presentation.CategorySuggestionViewModel
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.category.CategoryViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.resolum.intiva.core.common.state.UiState
import com.resolum.intiva.core.ui.components.IntivaBackButton
import com.resolum.intiva.core.ui.snackbar.IntivaSnackBarHost
import com.resolum.intiva.core.ui.snackbar.SnackBarBus
import com.resolum.intiva.core.ui.theme.IntivaColors
import com.resolum.intiva.features.finances.domain.models.TransactionType
import com.resolum.intiva.features.finances.presentation.transactions.components.AmountInput.appendDigit
import com.resolum.intiva.features.finances.presentation.transactions.components.AmountInput.deleteDigit
import com.resolum.intiva.features.finances.presentation.transactions.components.NumPad
import com.resolum.intiva.features.profiles.domain.models.FirstTransactionTutorialStep
import com.resolum.intiva.features.profiles.presentation.onboarding.OnboardingViewModel
import com.resolum.intiva.features.profiles.presentation.onboarding.components.OnboardingOverlay
import com.resolum.intiva.features.paymentmethodsandcategories.domain.models.Category
import com.resolum.intiva.features.paymentmethodsandcategories.domain.models.FinancialAccount
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.category.components.CategoryGrid
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.financialaccount.components.FinancialAccountSelector
import com.resolum.intiva.features.shared.domain.model.OwnerType
import kotlinx.coroutines.flow.collectLatest

/**
 * Composable function that represents the transaction form screen, allowing users to input transaction details such as amount, category, and financial account.
 *
 * This screen is designed to handle both income and expense transactions, providing a user-friendly interface for entering transaction information. It also integrates onboarding steps for first-time users.
 *
 * @param transactionType The type of transaction being registered (e.g., income or expense).
 * @param onDismiss Callback function to be invoked when the user dismisses the screen.
 * @param navController The NavController used for navigation within the app.
 * @param viewModel The ViewModel responsible for managing the state and logic of the transaction form.
 * @param onboardingViewModel The ViewModel responsible for managing the state and logic of the onboarding process.
 */
@Composable
fun TransactionFormScreen(
    transactionType: TransactionType,
    ownerType: OwnerType,
    onDismiss: () -> Unit,
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val suggestionModel: CategorySuggestionViewModel = hiltViewModel()
    val categoryModel: CategoryViewModel = hiltViewModel()
    val suggestion by suggestionModel.state.collectAsState()
    val categoryState by categoryModel.uiState.collectAsState()
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("0") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedAccount by remember { mutableStateOf<FinancialAccount?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    var saveButtonRect by remember { mutableStateOf<Rect?>(null) }
    var categoryRect by remember { mutableStateOf<Rect?>(null) }
    var numpadRect by remember { mutableStateOf<Rect?>(null) }
    var amountDisplayRect by remember { mutableStateOf<Rect?>(null) }

    val onboardingState by onboardingViewModel.state.collectAsState()

    var confirmedAmount by remember { mutableStateOf("") }
    var showLocalEnterAmountStep by remember { mutableStateOf(false) }


    val effectiveStep = when {
        showLocalEnterAmountStep -> FirstTransactionTutorialStep.ENTER_AMOUNT
        else -> onboardingState.step
    }

    val displayAmount = amountText

    val amountToSend = when {
        amountText.contains(".") -> {
            val decimal = amountText.substringAfter(".")
            when (decimal.length) {
                0 -> "${amountText}00"
                1 -> "${amountText}0"
                else -> amountText
            }
        }
        else -> "$amountText.00"
    }

    val isOnboardingActive = effectiveStep == FirstTransactionTutorialStep.SELECT_CATEGORY ||
            effectiveStep == FirstTransactionTutorialStep.ENTER_AMOUNT ||
            effectiveStep == FirstTransactionTutorialStep.CONFIRM_TRANSACTION
    val isSavingTransaction = uiState.state is UiState.Loading

    LaunchedEffect(Unit) {
        onboardingViewModel.loadStatus()
    }

    LaunchedEffect(onboardingState.step) {
        if (onboardingState.step == FirstTransactionTutorialStep.CONFIRM_TRANSACTION
            && confirmedAmount.isEmpty()
            && selectedCategory == null
        ) {
            onboardingViewModel.rollback()

        }
    }

    LaunchedEffect(Unit) {
        SnackBarBus.messages.collectLatest { event ->
            snackBarHostState.showSnackbar(event)
        }
    }

    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) {
            if (onboardingState.step == FirstTransactionTutorialStep.CONFIRM_TRANSACTION) {
                onboardingViewModel.advance()
            }
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("transaction_success", true)
            onDismiss()
        }
    }

    val title = when (transactionType) {
        TransactionType.INCOME -> "Nuevo Ingreso"
        TransactionType.EXPENSE -> "Nuevo Gasto"
        else -> "Nueva Transacción"
    }

    val saveLabel = when (transactionType) {
        TransactionType.INCOME -> "Registrar Ingreso"
        TransactionType.EXPENSE -> "Registrar Gasto"
        else -> "Guardar"
    }
    val actionButtonLabel = if (showLocalEnterAmountStep) "Continuar" else saveLabel
    val actionButtonColor = if (showLocalEnterAmountStep) Color(0xFF534AB7) else Color(0xFFCCFF00)
    val actionButtonContentColor = if (showLocalEnterAmountStep) Color.White else Color.Black

    val headerBackground = IntivaColors.BackgroundPurple

    Scaffold(
        snackbarHost = { IntivaSnackBarHost(hostState = snackBarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBackground)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    IntivaBackButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBackground)
                        .padding(bottom = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "S/. $displayAmount",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                amountDisplayRect = coordinates.boundsInRoot()
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box {
                            FinancialAccountSelector(
                                onAccountSelected = { selectedAccount = it }
                            )
                            if (isOnboardingActive) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { }
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    if (transactionType == TransactionType.EXPENSE && ownerType == OwnerType.INDIVIDUAL && !isOnboardingActive) {
                        OutlinedTextField(description, { description = it.take(180); suggestionModel.clear() },
                            label = { Text("Descripción del gasto") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        val value = amountToSend.toBigDecimalOrNull()
                        OutlinedButton(
                            onClick = { value?.let { suggestionModel.suggest(description, it) } },
                            enabled = !suggestion.busy && description.isNotBlank() && value != null && value.signum() > 0
                        ) { Text(if (suggestion.busy) "Buscando sugerencia…" else "Sugerir categoría con IA") }
                        val proposed = categoryState.categories.find { it.id == suggestion.result?.categoryId && it.isActive }
                        if (proposed != null) {
                            Text("Sugerencia: ${proposed.name}. El gasto aún no se ha guardado.")
                            OutlinedButton(onClick = { selectedCategory = proposed; suggestionModel.clear() }) {
                                Text("Usar esta categoría")
                            }
                        } else if (suggestion.unavailable || suggestion.result != null) {
                            Text("No hay una sugerencia disponible. Puedes elegir una categoría abajo.")
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                categoryRect = coordinates.boundsInRoot()
                            }
                    ) {
                        CategoryGrid(
                            selectedCategory = selectedCategory,
                            viewModel = categoryModel,
                            onCategorySelected = { category ->
                                selectedCategory = category
                                suggestionModel.clear()
                                if (onboardingState.step == FirstTransactionTutorialStep.SELECT_CATEGORY) {
                                    showLocalEnterAmountStep = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            ownerType = ownerType.name,
                            type = transactionType.name
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                numpadRect = coordinates.boundsInRoot()
                            }
                    ) {
                        NumPad(
                            onNumberClick = { digit ->
                                amountText = appendDigit(amountText, digit)
                                suggestionModel.clear()
                            },
                            onDecimalClick = {
                                if (!amountText.contains(".")) amountText = "$amountText."
                                suggestionModel.clear()
                            },
                            onDeleteClick = {
                                amountText = deleteDigit(amountText)
                                suggestionModel.clear()
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            when {
                                showLocalEnterAmountStep -> {
                                    confirmedAmount = amountToSend
                                    showLocalEnterAmountStep = false
                                    onboardingViewModel.advance()
                                }
                                else -> {
                                    viewModel.registerIndividualTransaction(
                                        amount = confirmedAmount.ifEmpty { amountToSend },
                                        category = selectedCategory,
                                        account = selectedAccount,
                                        transactionType = transactionType,
                                        ownerType = ownerType.name,
                                        description = description
                                    )
                                }
                            }
                        },
                        enabled = !isSavingTransaction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .onGloballyPositioned { coordinates ->
                                saveButtonRect = coordinates.boundsInRoot()
                            },
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = actionButtonColor,
                            disabledContainerColor = Color(0xFFE5E0EC),
                            disabledContentColor = IntivaColors.TextSecondary
                        )
                    ) {
                        if (isSavingTransaction) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = actionButtonLabel,
                                color = actionButtonContentColor,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            OnboardingOverlay(
                step = effectiveStep,
                incomeRect = null,
                categoryRect = categoryRect,
                amountRect = numpadRect,
                amountDisplayRect = amountDisplayRect,
                saveButtonRect = saveButtonRect,
                onNext = {
                    if (showLocalEnterAmountStep) {
                        confirmedAmount = amountToSend
                        showLocalEnterAmountStep = false
                        onboardingViewModel.advance()
                    } else {
                        onboardingViewModel.advance()
                    }
                }
            )
        }
    }
}
