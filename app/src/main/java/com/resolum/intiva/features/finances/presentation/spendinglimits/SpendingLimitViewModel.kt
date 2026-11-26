package com.resolum.intiva.features.finances.presentation.spendinglimits

import com.resolum.intiva.core.common.state.UiState
import com.resolum.intiva.core.common.viewmodel.BaseViewModel
import com.resolum.intiva.core.network.model.NetworkResult
import com.resolum.intiva.features.finances.domain.models.CreateSpendingLimitRequest
import com.resolum.intiva.features.finances.domain.models.SpendingLimit
import com.resolum.intiva.features.finances.domain.models.SpendingLimitOwnerType
import com.resolum.intiva.features.finances.domain.models.SpendingLimitTargetType
import com.resolum.intiva.features.finances.domain.models.UpdateSpendingLimitAmountRequest
import com.resolum.intiva.features.finances.domain.models.UpdateSpendingLimitPeriodRequest
import com.resolum.intiva.features.finances.domain.usecase.CreateSpendingLimitUseCase
import com.resolum.intiva.features.finances.domain.usecase.GetSpendingLimitsUseCase
import com.resolum.intiva.features.finances.domain.usecase.UpdateSpendingLimitAmountUseCase
import com.resolum.intiva.features.finances.domain.usecase.UpdateSpendingLimitPeriodUseCase
import com.resolum.intiva.features.iam.domain.repositories.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class SpendingLimitViewModel @Inject constructor(
    private val getSpendingLimitsUseCase: GetSpendingLimitsUseCase,
    private val createSpendingLimitUseCase: CreateSpendingLimitUseCase,
    private val updateSpendingLimitAmountUseCase: UpdateSpendingLimitAmountUseCase,
    private val updateSpendingLimitPeriodUseCase: UpdateSpendingLimitPeriodUseCase,
    private val sessionRepository: SessionRepository,
    private val agentRepository: com.resolum.intiva.features.agent.data.AgentRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SpendingLimitUiState())
    val uiState: StateFlow<SpendingLimitUiState> = _uiState.asStateFlow()

    fun loadMonthlySpendingLimit() {
        safeLaunch {
            _uiState.update { it.copy(spendingLimitState = UiState.Loading) }

            val userId = sessionRepository.getUserId()
            if (userId == null) {
                _uiState.update {
                    it.copy(
                        spendingLimitState = UiState.Error(
                            message = "No se pudo obtener el usuario de la sesión."
                        )
                    )
                }
                return@safeLaunch
            }

            when (
                val result = getSpendingLimitsUseCase(
                    ownerId = userId,
                    ownerType = SpendingLimitOwnerType.INDIVIDUAL
                )
            ) {
                is NetworkResult.Success -> {
                    val selectedLimit = result.data.firstOrNull { it.active }
                        ?: result.data.firstOrNull()

                    _uiState.update {
                        it.copy(
                            spendingLimitState = selectedLimit
                                ?.let { limit -> UiState.Success(limit.toSummary()) }
                                ?: UiState.Idle
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            spendingLimitState = UiState.Error(
                                message = result.message,
                                throwable = result.throwable
                            )
                        )
                    }
                }
            }
        }
    }

    fun loadSpendingLimits() {
        loadPlanLimits()
        safeLaunch {
            _uiState.update { it.copy(spendingLimitsState = UiState.Loading) }

            val userId = sessionRepository.getUserId()
            if (userId == null) {
                _uiState.update {
                    it.copy(
                        spendingLimitsState = UiState.Error(
                            message = "No se pudo obtener el usuario de la sesión."
                        )
                    )
                }
                return@safeLaunch
            }

            when (
                val result = getSpendingLimitsUseCase(
                    ownerId = userId,
                    ownerType = SpendingLimitOwnerType.INDIVIDUAL
                )
            ) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            spendingLimitsState = UiState.Success(
                                result.data.map { limit -> limit.toSummary() }
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            spendingLimitsState = UiState.Error(
                                message = result.message,
                                throwable = result.throwable
                            )
                        )
                    }
                }
            }
        }
    }

    fun loadPlanLimits(offset: Long = 0) {
        val month = java.time.YearMonth.parse(_uiState.value.planMonth).plusMonths(offset).toString()
        _uiState.update { it.copy(planMonth = month, planLimitsState = UiState.Loading) }
        safeLaunch {
            try {
                val limits = agentRepository.api.limits(month)
                if (_uiState.value.planMonth == month) {
                    _uiState.update { it.copy(planLimitsState = UiState.Success(limits)) }
                }
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (_uiState.value.planMonth == month) {
                    _uiState.update { it.copy(planLimitsState = UiState.Error("No se pudieron cargar los límites del plan.")) }
                }
            }
        }
    }

    fun createCategorySpendingLimit(
        categoryId: Long,
        amount: String,
        frequency: SpendingLimitFrequency
    ) {
        safeLaunch {
            val limitAmount = amount.toBigDecimalOrNull()
            if (limitAmount == null || limitAmount <= BigDecimal.ZERO) {
                _uiState.update {
                    it.copy(createState = UiState.Error("Ingresa un monto válido."))
                }
                return@safeLaunch
            }

            val userId = sessionRepository.getUserId()
            if (userId == null) {
                _uiState.update {
                    it.copy(createState = UiState.Error("No se pudo obtener el usuario de la sesión."))
                }
                return@safeLaunch
            }

            val period = frequency.toPeriod()
            _uiState.update { it.copy(createState = UiState.Loading) }

            val request = CreateSpendingLimitRequest(
                ownerId = userId,
                ownerType = SpendingLimitOwnerType.INDIVIDUAL,
                targetType = SpendingLimitTargetType.CATEGORY,
                targetId = categoryId,
                limitAmount = limitAmount,
                currencyCode = "PEN",
                startDate = period.first.toString(),
                endDate = period.second.toString()
            )

            when (val result = createSpendingLimitUseCase(request)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(createState = UiState.Success(result.data)) }
                    loadSpendingLimits()
                    loadMonthlySpendingLimit()
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            createState = UiState.Error(
                                message = result.message,
                                throwable = result.throwable
                            )
                        )
                    }
                }
            }
        }
    }

    fun clearCreateState() {
        _uiState.update { it.copy(createState = UiState.Idle) }
    }

    fun updateSpendingLimit(
        limit: SpendingLimit,
        amount: String,
        frequency: SpendingLimitFrequency,
        updatePeriod: Boolean
    ) {
        safeLaunch {
            val limitAmount = amount.toBigDecimalOrNull()
            if (limitAmount == null || limitAmount <= BigDecimal.ZERO) {
                _uiState.update {
                    it.copy(updateState = UiState.Error("Ingresa un monto válido."))
                }
                return@safeLaunch
            }

            val period = frequency.toPeriod()
            val amountChanged = limitAmount.compareTo(limit.limitAmount) != 0
            val periodChanged = updatePeriod &&
                (period.first.toString() != limit.startDate || period.second.toString() != limit.endDate)

            if (!amountChanged && !periodChanged) {
                _uiState.update { it.copy(updateState = UiState.Success(limit)) }
                return@safeLaunch
            }

            _uiState.update { it.copy(updateState = UiState.Loading) }

            if (amountChanged) {
                val amountResult = updateSpendingLimitAmountUseCase(
                    spendingLimitId = limit.id,
                    request = UpdateSpendingLimitAmountRequest(
                        limitAmount = limitAmount,
                        currencyCode = limit.currencyCode
                    )
                )

                if (amountResult is NetworkResult.Error) {
                    _uiState.update {
                        it.copy(
                            updateState = UiState.Error(
                                message = amountResult.message,
                                throwable = amountResult.throwable
                            )
                        )
                    }
                    return@safeLaunch
                }
            }

            if (periodChanged) {
                when (
                    val periodResult = updateSpendingLimitPeriodUseCase(
                        spendingLimitId = limit.id,
                        request = UpdateSpendingLimitPeriodRequest(
                            startDate = period.first.toString(),
                            endDate = period.second.toString()
                        )
                    )
                ) {
                    is NetworkResult.Success -> {
                        _uiState.update { it.copy(updateState = UiState.Success(periodResult.data)) }
                    }

                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                updateState = UiState.Error(
                                    message = periodResult.message,
                                    throwable = periodResult.throwable
                                )
                            )
                        }
                        return@safeLaunch
                    }
                }
            } else {
                _uiState.update { it.copy(updateState = UiState.Success(limit.copy(limitAmount = limitAmount))) }
            }

            loadSpendingLimits()
            loadMonthlySpendingLimit()
        }
    }

    fun clearUpdateState() {
        _uiState.update { it.copy(updateState = UiState.Idle) }
    }

    override fun handleError(throwable: Throwable) {
        _uiState.update {
            it.copy(
                spendingLimitState = UiState.Error(
                    message = throwable.message ?: "Error al cargar el límite de gasto.",
                    throwable = throwable
                ),
                spendingLimitsState = UiState.Error(
                    message = throwable.message ?: "Error al cargar límites de gasto.",
                    throwable = throwable
                )
            )
        }
    }
}

enum class SpendingLimitFrequency(val label: String) {
    WEEKLY("Semanal"),
    MONTHLY("Mensual"),
    YEARLY("Anual")
}

private fun SpendingLimitFrequency.toPeriod(): Pair<LocalDate, LocalDate> {
    val today = LocalDate.now()
    return when (this) {
        SpendingLimitFrequency.WEEKLY -> {
            val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
            start to start.plusDays(6)
        }
        SpendingLimitFrequency.MONTHLY ->
            today.withDayOfMonth(1) to today.with(TemporalAdjusters.lastDayOfMonth())
        SpendingLimitFrequency.YEARLY ->
            today.withDayOfYear(1) to today.with(TemporalAdjusters.lastDayOfYear())
    }
}
