package com.resolum.intiva.features.agent.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resolum.intiva.features.agent.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class PlanUiState(
    val month: String = YearMonth.now().plusMonths(1).toString(),
    val insightMonth: String = YearMonth.now().toString(),
    val plan: MonthlyPlan? = null,
    val notices: List<PlanNotice> = emptyList(),
    val insights: FinancialInsights? = null,
    val draftAmount: String = "",
    val goals: List<PlanningGoal> = emptyList(),
    val selectedGoal: Long? = null,
    val contribution: String = "",
    val reminderDate: String = "",
    val reminderLabel: String = "",
    val extrasEdited: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlanViewModel @Inject constructor(
    repository: AgentRepository, private val savedState: SavedStateHandle
) : ViewModel() {
    private val api = repository.api
    private val _state = MutableStateFlow(PlanUiState())
    val state = _state.asStateFlow()

    init {
        savedState.get<String>("planId")?.let { id -> call { accept(api.get(id)) } }
    }
    fun month(value: String) = _state.update { it.copy(month = value.take(7)) }
    fun insightMonth(value: String) = _state.update { it.copy(insightMonth = value.take(7)) }
    fun amount(value: String) = _state.update { it.copy(draftAmount = value.take(12)) }
    fun loadGoals() = call { val goals = api.goals(); _state.update { it.copy(goals = goals) } }
    fun selectGoal(id: Long?) = _state.update { it.copy(selectedGoal = id, extrasEdited = true,
        contribution = if (id == null) "" else it.contribution) }
    fun contribution(value: String) = _state.update { it.copy(contribution = value.take(12), extrasEdited = true) }
    fun reminderDate(value: String) = _state.update { it.copy(reminderDate = value.take(10), extrasEdited = true) }
    fun reminderLabel(value: String) = _state.update { it.copy(reminderLabel = value.take(120), extrasEdited = true) }
    fun generate() = call { accept(api.generate(GeneratePlan(state.value.month))) }
    fun insights() = call {
        val result = api.insights(state.value.insightMonth)
        _state.update { it.copy(insights = result) }
    }
    fun edit() = call {
        val view = state.value.plan ?: return@call
        val amount = parseAmount() ?: return@call fail("Escribe un monto positivo con hasta dos decimales.")
        val form = state.value
        val contribution = form.contribution.replace(',', '.').toBigDecimalOrNull()
        if (form.selectedGoal != null && (contribution == null || contribution.signum() <= 0))
            return@call fail("Escribe un aporte positivo para la meta.")
        if (form.reminderDate.isBlank() != form.reminderLabel.isBlank())
            return@call fail("Completa la fecha y el nombre del recordatorio.")
        if (form.reminderDate.isNotBlank() && runCatching {
                YearMonth.from(java.time.LocalDate.parse(form.reminderDate)).toString()
            }.getOrNull() != view.month) return@call fail("La fecha debe pertenecer al mes del plan (AAAA-MM-DD).")
        accept(api.edit(view.id, EditPlan(view.version, amount, form.selectedGoal,
            contribution.takeIf { form.selectedGoal != null }, form.reminderDate.ifBlank { null },
            form.reminderLabel.trim().ifBlank { null })))
    }
    fun approve() = call {
        val view = state.value.plan ?: return@call
        val proposed = view.actions.firstOrNull()?.proposedLimit ?: return@call
        if (state.value.extrasEdited || parseAmount()?.compareTo(proposed) != 0) return@call fail("Guarda el cambio antes de aprobar.")
        accept(api.approve(view.id, ApprovePlan(view.version)))
    }
    fun refresh() = call {
        val id = state.value.plan?.id ?: savedState.get<String>("planId") ?: return@call
        accept(api.get(id))
        val notices = api.notices().filter { it.planId == id }
        _state.update { it.copy(notices = notices) }
    }
    private fun parseAmount(): BigDecimal? = state.value.draftAmount.replace(',', '.').toBigDecimalOrNull()
        ?.takeIf { it.signum() > 0 && it.scale() <= 2 && it < BigDecimal("10000000") }
    private fun accept(view: MonthlyPlan) {
        savedState["planId"] = view.id
        _state.update { it.copy(plan = view, notices = it.notices.filter { n -> n.planId == view.id },
            draftAmount = view.actions.firstOrNull()?.proposedLimit?.toPlainString().orEmpty(),
            selectedGoal = view.actions.find { a -> a.id == "contribution" }?.categoryId,
            contribution = view.actions.find { a -> a.id == "contribution" }?.proposedLimit?.toPlainString().orEmpty(),
            reminderDate = view.actions.find { a -> a.id == "reminder" }?.label?.substringBefore(" · ").orEmpty(),
            reminderLabel = view.actions.find { a -> a.id == "reminder" }?.label?.substringAfter(" · ").orEmpty(),
            extrasEdited = false) }
    }
    private fun fail(message: String) { _state.update { it.copy(error = message) } }
    private fun call(block: suspend () -> Unit) {
        if (state.value.busy) return
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: HttpException) {
                fail(when (e.code()) {
                    400 -> "Revisa los datos ingresados."
                    401, 403 -> "Vuelve a iniciar sesión."
                    404 -> "No se encontró este plan."
                    409 -> "El plan cambió. Consulta el estado antes de continuar."
                    422 -> "No hay suficientes gastos registrados en soles para este período."
                    else -> "El servicio no está disponible. Inténtalo nuevamente."
                })
            } catch (e: Exception) { fail("No pudimos conectar con Intiva. Revisa tu conexión.") }
            finally { _state.update { it.copy(busy = false) } }
        }
    }
}
