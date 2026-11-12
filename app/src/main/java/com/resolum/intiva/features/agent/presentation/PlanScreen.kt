package com.resolum.intiva.features.agent.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PlanScreen(onBack: () -> Unit, model: PlanViewModel = hiltViewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    val plan = state.plan
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()
        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("Plan financiero", style = MaterialTheme.typography.headlineMedium)
        Text("Revisa la proyección y decide qué límite aplicar.")
        OutlinedTextField(state.month, model::month, label = { Text("Mes del plan (AAAA-MM)") },
            singleLine = true, enabled = !state.busy, modifier = Modifier.fillMaxWidth())
        Button(model::generate, enabled = !state.busy && state.month.length == 7 && plan?.status != "APPLYING") {
            Text("Generar plan")
        }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (plan != null) {
            Text(when (plan.status) {
                "DRAFT" -> "Borrador · versión ${plan.version}"
                "APPLYING" -> "Aplicando cambios"
                "ACTIVE" -> "Plan activo"
                "FAILED" -> "No se confirmó la aplicación"
                else -> plan.status
            }, style = MaterialTheme.typography.titleLarge)
            Text("Período: ${plan.month}")
            Text(plan.explanation)
            plan.actions.forEach { action ->
                Text(action.label, style = MaterialTheme.typography.titleMedium)
                when (action.type) {
                    "SPENDING_LIMIT" -> {
                        Text("Gasto estimado: S/ ${action.projectedExpense}")
                        Text(action.currentLimit?.let { "Límite previo: S/ $it" } ?: "Sin límite previo")
                        Text("Límite propuesto: S/ ${action.proposedLimit}")
                    }
                    "PLANNED_CONTRIBUTION" -> Text("Aporte previsto: S/ ${action.proposedLimit}. No mueve dinero.")
                }
                Text(when (action.status) {
                    "APPLIED" -> "Aplicado"
                    "PENDING" -> "Pendiente"
                    else -> "Propuesto"
                })
            }
            if (plan.status == "DRAFT") {
                OutlinedTextField(state.draftAmount, model::amount, label = { Text("Nuevo límite (S/)") },
                    enabled = !state.busy, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Text("Aporte y recordatorio (opcionales)", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(model::loadGoals, enabled = !state.busy) { Text("Cargar mis metas") }
                state.goals.forEach { goal ->
                    FilterChip(selected = state.selectedGoal == goal.id,
                        onClick = { model.selectGoal(if (state.selectedGoal == goal.id) null else goal.id) },
                        label = { Text(goal.label) }, enabled = !state.busy)
                }
                if (state.selectedGoal != null) {
                    OutlinedTextField(state.contribution, model::contribution, label = { Text("Aporte previsto (S/)") },
                        enabled = !state.busy, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    TextButton(onClick = { model.selectGoal(null) }, enabled = !state.busy) { Text("Quitar aporte") }
                }
                OutlinedTextField(state.reminderDate, model::reminderDate, label = { Text("Fecha (AAAA-MM-DD)") },
                    enabled = !state.busy, singleLine = true)
                OutlinedTextField(state.reminderLabel, model::reminderLabel, label = { Text("Pago que quieres recordar") },
                    enabled = !state.busy, singleLine = true)
                OutlinedButton(model::edit, enabled = !state.busy) { Text("Guardar cambios en borrador") }
                Button(model::approve, enabled = !state.busy) { Text("Aprobar y aplicar plan") }
            }
            if (plan.status == "FAILED" || plan.status == "APPLYING") {
                OutlinedButton(model::approve, enabled = !state.busy) { Text("Reintentar aplicación") }
            }
            OutlinedButton(model::refresh, enabled = !state.busy) { Text("Consultar estado y aviso") }
            state.notices.forEach { notice -> Text(notice.message, style = MaterialTheme.typography.bodyMedium) }
        }
        HorizontalDivider()
        Text("Gastos pequeños", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(state.insightMonth, model::insightMonth, label = { Text("Mes a consultar (AAAA-MM)") },
            singleLine = true, enabled = !state.busy)
        OutlinedButton(model::insights, enabled = !state.busy) { Text("Consultar mis gastos") }
        state.insights?.let { info ->
            Text("${info.period}: ${info.smallExpenseCount} gastos pequeños · S/ ${info.smallExpenseTotal}")
            Text(info.note)
        }
    }
}
