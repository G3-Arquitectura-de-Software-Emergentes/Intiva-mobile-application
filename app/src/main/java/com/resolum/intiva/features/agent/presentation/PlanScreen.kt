package com.resolum.intiva.features.agent.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resolum.intiva.core.ui.theme.IntivaColors
import com.resolum.intiva.features.agent.data.FinancialInsights
import com.resolum.intiva.features.agent.data.MonthlyPlan
import com.resolum.intiva.features.agent.data.PlanAction
import com.resolum.intiva.features.agent.presentation.components.AgentChip
import com.resolum.intiva.features.agent.presentation.components.AgentColors
import com.resolum.intiva.features.agent.presentation.components.AgentPillButton
import com.resolum.intiva.features.agent.presentation.components.AgentPrimaryButton
import com.resolum.intiva.features.agent.presentation.components.AgentSecondaryButton
import com.resolum.intiva.features.agent.presentation.components.AiNote
import com.resolum.intiva.features.agent.presentation.components.AmountRow
import com.resolum.intiva.features.agent.presentation.components.ChipTone
import com.resolum.intiva.features.agent.presentation.components.PlanActionCard
import com.resolum.intiva.features.agent.presentation.components.RiskChip
import com.resolum.intiva.features.agent.presentation.components.soles
import com.resolum.intiva.features.finances.presentation.transactions.components.iconForTransactionCategory
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val spanish: Locale = Locale.forLanguageTag("es-PE")

/**
 * "Plan financiero": the agent's monthly plan and the small-expenses summary in one screen,
 * following the team's product video. Nothing changes until the user approves the plan.
 */
@Composable
fun PlanScreen(onBack: () -> Unit, model: PlanViewModel = hiltViewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    val plan = state.plan

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Header(onBack)

        AgentTextField(
            value = state.month,
            onValueChange = model::month,
            label = "Mes del plan",
            enabled = !state.busy,
            placeholder = "AAAA-MM",
        )
        AgentTextField(
            value = state.expectedIncome,
            onValueChange = model::expectedIncome,
            label = "Ingresos previstos en soles (opcional)",
            enabled = !state.busy,
            placeholder = "Monto que esperas recibir en el mes",
        )
        AgentPillButton(
            text = "Generar plan",
            onClick = model::generate,
            filled = true,
            enabled = state.month.length == 7 && plan?.status != "APPLYING",
            loading = state.busy && plan == null,
        )
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = AgentColors.Purple)
        state.error?.let { ErrorBox(it) }

        if (plan != null) PlanContent(plan, state, model)

        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = AgentColors.CardBorder)
        SmallExpenses(state, model)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = IntivaColors.TextPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text("Plan financiero", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = IntivaColors.TextPrimary)
        }
        Text(
            "Revisa la proyección y decide qué límite aplicar.",
            fontSize = 14.sp,
            color = IntivaColors.TextSecondary,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp),
        )
    }
}

@Composable
private fun PlanContent(plan: MonthlyPlan, state: PlanUiState, model: PlanViewModel) {
    val draft = plan.status == "DRAFT"
    StatusChip(plan)
    if (plan.explanation.isNotBlank()) AiNote(plan.explanation)

    plan.actions.forEach { action ->
        when (action.type) {
            "SPENDING_LIMIT" -> LimitCard(action, draft, state, model)
            "PLANNED_CONTRIBUTION" -> ContributionCard(action)
            "PAYMENT_REMINDER" -> ReminderCard(action)
        }
    }

    if (draft) {
        AgentSecondaryButton("Descartar borrador", model::discard, enabled = !state.busy)
        DraftExtras(plan, state, model)
        AgentSecondaryButton("Guardar cambios en borrador", model::edit, enabled = !state.busy)
        AgentPrimaryButton(
            "Aprobar y aplicar plan",
            model::approve,
            icon = Icons.Outlined.CheckCircle,
            enabled = !state.busy,
        )
    }
    if (plan.status == "FAILED" || plan.status == "APPLYING") {
        AgentPrimaryButton("Reintentar aplicación", model::approve, icon = Icons.Filled.Refresh, enabled = !state.busy)
    }
    if (!draft) {
        AgentSecondaryButton("Consultar estado y avisos", model::refresh, enabled = !state.busy)
    }
    state.notices.forEach { notice -> AiNote(notice.message, icon = Icons.Filled.NotificationsActive) }
}

@Composable
private fun StatusChip(plan: MonthlyPlan) {
    when (plan.status) {
        "DRAFT" -> AgentChip("Borrador · versión ${plan.version}", ChipTone.Amber)
        "APPLYING" -> AgentChip("Aplicando cambios…", ChipTone.Blue)
        "ACTIVE" -> AgentChip("Plan activo", ChipTone.Green, icon = Icons.Filled.CheckCircle)
        "DISCARDED" -> AgentChip("Plan descartado", ChipTone.Neutral)
        "FAILED" -> AgentChip("No se confirmó la aplicación", ChipTone.Red)
        else -> AgentChip(plan.status, ChipTone.Neutral)
    }
}

@Composable
private fun ActionBadge(status: String) = when (status) {
    "APPLIED" -> AgentChip("Aplicado", ChipTone.Green)
    "PENDING" -> AgentChip("Pendiente", ChipTone.Amber)
    else -> AgentChip("Propuesto", ChipTone.Neutral)
}

@Composable
private fun LimitCard(action: PlanAction, draft: Boolean, state: PlanUiState, model: PlanViewModel) {
    val proposed = if (draft) state.draftAmount.replace(',', '.').toBigDecimalOrNull() ?: action.proposedLimit
    else action.proposedLimit
    PlanActionCard(
        icon = iconForTransactionCategory(action.label),
        iconColor = AgentColors.FoodGreen,
        title = action.label,
        badge = { ActionBadge(action.status) },
    ) {
        AmountRow("Gasto estimado", soles(action.projectedExpense))
        AmountRow("Límite previo", action.currentLimit?.let { soles(it) } ?: "Sin límite")
        AmountRow("Límite propuesto", soles(proposed), highlight = true)
        overLimit(action.projectedExpense, action.currentLimit ?: proposed)?.let { RiskChip("${soles(it)} sobre el límite") }
        if (draft) {
            AgentTextField(
                value = state.draftAmount,
                onValueChange = model::amount,
                label = "Editar límite propuesto (S/)",
                enabled = !state.busy,
                keyboardType = KeyboardType.Decimal,
            )
        }
    }
}

/** How much the projected expense goes over the limit, or null when it fits. */
private fun overLimit(projected: BigDecimal?, limit: BigDecimal?): BigDecimal? {
    if (projected == null || limit == null) return null
    val over = projected.subtract(limit)
    return over.takeIf { it.signum() > 0 }
}

@Composable
private fun ContributionCard(action: PlanAction) {
    PlanActionCard(
        icon = Icons.Filled.Savings,
        iconColor = AgentColors.SavingsGreen,
        title = "Aporte previsto",
        subtitle = "Meta: ${action.label}",
        badge = { ActionBadge(action.status) },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(soles(action.proposedLimit), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AgentColors.Purple)
            Spacer(Modifier.width(10.dp))
            Text("No mueve dinero: queda planificado.", fontSize = 12.sp, color = IntivaColors.TextSecondary)
        }
    }
}

@Composable
private fun ReminderCard(action: PlanAction) {
    val date = action.label.substringBefore(" · ")
    val label = action.label.substringAfter(" · ")
    PlanActionCard(
        icon = Icons.Filled.NotificationsActive,
        iconColor = AgentColors.ReminderOrange,
        title = "Recordatorio de pago",
        subtitle = "$label · ${prettyDay(date)}",
        badge = { ActionBadge(action.status) },
    )
}

/** Optional contribution and reminder, edited before approving. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DraftExtras(plan: MonthlyPlan, state: PlanUiState, model: PlanViewModel) {
    var showContribution by rememberSaveable(plan.id) { mutableStateOf(state.selectedGoal != null) }
    var showReminder by rememberSaveable(plan.id) { mutableStateOf(state.reminderDate.isNotBlank()) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Ajusta tu borrador", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = IntivaColors.TextPrimary)

        if (!showContribution) {
            AgentPillButton("Agregar aporte a una meta", {
                showContribution = true
                model.loadGoals()
            }, icon = Icons.Filled.Savings, enabled = !state.busy)
        } else {
            if (state.goals.isEmpty()) {
                Text(
                    "Necesitas una meta personal activa en soles para planificar un aporte.",
                    fontSize = 13.sp, color = IntivaColors.TextSecondary,
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.goals.forEach { goal ->
                    FilterChip(
                        selected = state.selectedGoal == goal.id,
                        onClick = { model.selectGoal(if (state.selectedGoal == goal.id) null else goal.id) },
                        label = { Text(goal.label) },
                        enabled = !state.busy,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgentColors.Lavender,
                            selectedLabelColor = AgentColors.Purple,
                        ),
                    )
                }
            }
            if (state.selectedGoal != null) {
                AgentTextField(
                    value = state.contribution,
                    onValueChange = model::contribution,
                    label = "Aporte previsto (S/)",
                    enabled = !state.busy,
                    keyboardType = KeyboardType.Decimal,
                )
            }
            TextButton(onClick = {
                model.selectGoal(null)
                showContribution = false
            }, enabled = !state.busy) { Text("Quitar aporte", color = AgentColors.Purple) }
        }

        if (!showReminder) {
            AgentPillButton("Agregar recordatorio de pago", { showReminder = true },
                icon = Icons.Filled.NotificationsActive, enabled = !state.busy)
        } else {
            ReminderDateField(plan.month, state.reminderDate, model::reminderDate, enabled = !state.busy)
            AgentTextField(
                value = state.reminderLabel,
                onValueChange = model::reminderLabel,
                label = "Pago que quieres recordar",
                enabled = !state.busy,
                placeholder = "Recibo de agua",
            )
            TextButton(onClick = {
                model.reminderDate("")
                model.reminderLabel("")
                showReminder = false
            }, enabled = !state.busy) { Text("Quitar recordatorio", color = AgentColors.Purple) }
        }
    }
}

/** Read-only field that opens a date picker limited to the plan's month. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderDateField(planMonth: String, value: String, onPick: (String) -> Unit, enabled: Boolean) {
    var open by remember { mutableStateOf(false) }
    val month = runCatching { YearMonth.parse(planMonth) }.getOrNull()
    Box {
        AgentTextField(
            value = if (value.isBlank()) "" else prettyDay(value),
            onValueChange = {},
            label = "Fecha del recordatorio",
            enabled = enabled,
            readOnly = true,
            placeholder = "Elige un día del mes del plan",
            trailingIcon = Icons.Filled.CalendarMonth,
        )
        // The read-only field swallows taps, so a transparent layer on top opens the picker.
        Box(Modifier.matchParentSize().clickable(enabled = enabled) { open = true })
    }
    if (open) {
        val initial = runCatching { LocalDate.parse(value) }.getOrNull() ?: month?.atDay(1)
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val day = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    return month == null || YearMonth.from(day) == month
                }
                override fun isSelectableYear(year: Int) = month == null || year == month.year
            },
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    picker.selectedDateMillis?.let {
                        onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    open = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancelar") } },
        ) { DatePicker(state = picker) }
    }
}

@Composable
private fun SmallExpenses(state: PlanUiState, model: PlanViewModel) {
    Text("Gastos pequeños", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = IntivaColors.TextPrimary)
    Text("Compras de hasta S/ 10 que se repiten en el mes.", fontSize = 14.sp, color = IntivaColors.TextSecondary)
    AgentTextField(
        value = state.insightMonth,
        onValueChange = model::insightMonth,
        label = "Mes a consultar",
        enabled = !state.busy,
        placeholder = "AAAA-MM",
    )
    AgentPillButton("Consultar mis gastos", model::insights, icon = null, enabled = !state.busy)
    state.insights?.let { InsightsCard(it) }
}

@Composable
private fun InsightsCard(info: FinancialInsights) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(AgentColors.Purple, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(prettyMonth(info.period), fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
        Text("${info.smallExpenseCount} gastos pequeños", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(soles(info.smallExpenseTotal), fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = AgentColors.Lime)
        val bars = info.categories.orEmpty()
        val max = bars.maxOfOrNull { it.total }?.takeIf { it.signum() > 0 }
        bars.forEach { bar ->
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(bar.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.weight(1f))
                Text(soles(bar.total), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            LinearProgressIndicator(
                progress = { if (max == null) 0f else bar.total.toFloat() / max.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = AgentColors.Lime,
                trackColor = Color.White.copy(alpha = 0.2f),
                drawStopIndicator = {},
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AgentColors.Lime, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(info.note, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun ErrorBox(message: String) {
    Text(
        message,
        color = AgentColors.RedText,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(AgentColors.RedBg, RoundedCornerShape(12.dp))
            .padding(12.dp),
    )
}

@Composable
private fun AgentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = true,
        enabled = enabled,
        readOnly = readOnly,
        trailingIcon = trailingIcon?.let { { Icon(it, contentDescription = null, tint = AgentColors.Purple) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AgentColors.Purple,
            focusedLabelColor = AgentColors.Purple,
            unfocusedBorderColor = AgentColors.CardBorder,
            cursorColor = AgentColors.Purple,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** "2026-11" → "Noviembre 2026". */
private fun prettyMonth(value: String): String = runCatching {
    YearMonth.parse(value).format(DateTimeFormatter.ofPattern("MMMM yyyy", spanish))
        .replaceFirstChar { it.titlecase(spanish) }
}.getOrDefault(value)

/** "2026-12-15" → "15 de diciembre". */
private fun prettyDay(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d 'de' MMMM", spanish))
}.getOrDefault(value)
