package com.resolum.intiva.features.finances.presentation.spendinglimits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.resolum.intiva.core.common.state.UiState
import com.resolum.intiva.features.finances.domain.models.TransactionType
import com.resolum.intiva.features.finances.presentation.spendinglimits.components.SpendingLimitCreateContent
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.category.CategoryViewModel
import com.resolum.intiva.features.shared.domain.model.OwnerType

@Composable
fun SpendingLimitScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SpendingLimitViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var selectedLimitToAdjust by remember { mutableStateOf<SpendingLimitSummary?>(null) }

    LaunchedEffect(Unit) {
        categoryViewModel.getCategories(
            ownerType = OwnerType.INDIVIDUAL.name,
            type = TransactionType.EXPENSE.name
        )
        viewModel.loadSpendingLimits()
    }

    LaunchedEffect(uiState.createState) {
        if (uiState.createState is UiState.Success) {
            showCreate = false
            viewModel.clearCreateState()
        }
    }

    LaunchedEffect(uiState.updateState) {
        if (uiState.updateState is UiState.Success) {
            selectedLimitToAdjust = null
            viewModel.clearUpdateState()
        }
    }

    if (showCreate) {
        SpendingLimitCreateContent(
            createState = uiState.createState,
            onClose = {
                showCreate = false
                viewModel.clearCreateState()
            },
            onSave = { category, amount, frequency ->
                viewModel.createCategorySpendingLimit(
                    categoryId = category.id,
                    amount = amount,
                    frequency = frequency
                )
            }
        )
    } else {
        SpendingLimitListContent(
            planLimitsState = uiState.planLimitsState,
            planMonth = uiState.planMonth,
            onPlanMonthChange = viewModel::loadPlanLimits,
            limitsState = uiState.spendingLimitsState,
            categories = categoryUiState.categories,
            onAddClick = { showCreate = true },
            updateState = uiState.updateState,
            selectedLimitToAdjust = selectedLimitToAdjust,
            onAdjustClick = { selectedLimitToAdjust = it },
            onDismissAdjust = {
                selectedLimitToAdjust = null
                viewModel.clearUpdateState()
            },
            onSaveAdjust = { limit, amount, frequency, updatePeriod ->
                viewModel.updateSpendingLimit(
                    limit = limit,
                    amount = amount,
                    frequency = frequency,
                    updatePeriod = updatePeriod
                )
            },
            onBack = onNavigateBack
        )
    }
}
