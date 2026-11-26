package com.resolum.intiva.features.finances.presentation.spendinglimits

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resolum.intiva.core.common.state.UiState
import com.resolum.intiva.core.ui.theme.IntivaColors
import com.resolum.intiva.features.finances.domain.models.SpendingLimit
import com.resolum.intiva.features.finances.presentation.spendinglimits.components.AdjustSpendingLimitSheet
import com.resolum.intiva.features.finances.presentation.spendinglimits.components.BudgetSummaryCard
import com.resolum.intiva.features.finances.presentation.spendinglimits.components.EmptyLimitsCard
import com.resolum.intiva.features.finances.presentation.spendinglimits.components.MessageCard
import com.resolum.intiva.features.finances.presentation.spendinglimits.components.SpendingLimitListItem
import com.resolum.intiva.features.paymentmethodsandcategories.domain.models.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingLimitListContent(
    planLimitsState: UiState<List<com.resolum.intiva.features.agent.data.PlanBudget>>,
    planMonth: String,
    onPlanMonthChange: (Long) -> Unit,
    limitsState: UiState<List<SpendingLimitSummary>>,
    categories: List<Category>,
    onAddClick: () -> Unit,
    updateState: UiState<SpendingLimit>,
    selectedLimitToAdjust: SpendingLimitSummary?,
    onAdjustClick: (SpendingLimitSummary) -> Unit,
    onDismissAdjust: () -> Unit,
    onSaveAdjust: (SpendingLimit, String, SpendingLimitFrequency, Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = IntivaColors.BackgroundDefault,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = IntivaColors.PrimaryBrand,
                contentColor = IntivaColors.TextInverse,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear límite",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = IntivaColors.TextPrimary
                        )
                    }
                    Text(
                        text = "Límites de Gasto",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntivaColors.TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Monitorea tus presupuestos por categoría este mes.",
                    fontSize = 18.sp,
                    color = IntivaColors.TextSecondary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Presupuestos del plan mensual", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onPlanMonthChange(-1) }) { Text("Anterior") }
                        Text(planMonth, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onPlanMonthChange(1) }) { Text("Siguiente") }
                    }
                    when (planLimitsState) {
                        is UiState.Loading -> CircularProgressIndicator()
                        is UiState.Success -> {
                            if (planLimitsState.data.isEmpty()) Text("No hay límites aprobados para este mes.")
                            planLimitsState.data.forEach { budget ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(budget.categoryLabel, fontWeight = FontWeight.Bold)
                                        Text("Límite aprobado: S/ ${budget.amount}")
                                        Text("Gastos registrados: S/ ${budget.spent}")
                                        Text("Disponible: S/ ${budget.remaining}")
                                    }
                                }
                            }
                        }
                        is UiState.Error -> {
                            Text(planLimitsState.message)
                            TextButton(onClick = { onPlanMonthChange(0) }) { Text("Reintentar") }
                        }
                        else -> Unit
                    }
                }
            }
            when (limitsState) {
                is UiState.Loading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = IntivaColors.IconPurple)
                    }
                }

                is UiState.Success -> {
                    val summaries = limitsState.data
                    item {
                        BudgetSummaryCard(summaries = summaries)
                    }

                    if (summaries.isEmpty()) {
                        item {
                            EmptyLimitsCard(onAddClick = onAddClick)
                        }
                    } else {
                        items(summaries) { summary ->
                            SpendingLimitListItem(
                                summary = summary,
                                category = categories.firstOrNull { it.id == summary.limit.targetId },
                                onAdjustClick = { onAdjustClick(summary) }
                            )
                        }
                    }
                }

                is UiState.Error -> item {
                    MessageCard(
                        title = "No pudimos cargar tus límites",
                        message = limitsState.message
                    )
                }

                is UiState.Idle -> item {
                    MessageCard(
                        title = "Aún no tienes límites",
                        message = "Crea tu primer límite para controlar tu presupuesto mensual."
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    selectedLimitToAdjust?.let { summary ->
        AdjustSpendingLimitSheet(
            limit = summary.limit,
            updateState = updateState,
            onDismiss = onDismissAdjust,
            onSave = { amount, frequency, updatePeriod ->
                onSaveAdjust(summary.limit, amount, frequency, updatePeriod)
            }
        )
    }
}
