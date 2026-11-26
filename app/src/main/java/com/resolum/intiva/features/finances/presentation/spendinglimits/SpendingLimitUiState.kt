package com.resolum.intiva.features.finances.presentation.spendinglimits

import com.resolum.intiva.core.common.state.UiState
import com.resolum.intiva.features.finances.domain.models.SpendingLimit
import java.math.BigDecimal
import java.math.RoundingMode

data class SpendingLimitUiState(
    val spendingLimitState: UiState<SpendingLimitSummary> = UiState.Idle,
    val spendingLimitsState: UiState<List<SpendingLimitSummary>> = UiState.Idle,
    val createState: UiState<SpendingLimit> = UiState.Idle,
    val updateState: UiState<SpendingLimit> = UiState.Idle,
    val planLimitsState: UiState<List<com.resolum.intiva.features.agent.data.PlanBudget>> = UiState.Idle,
    val planMonth: String = java.time.YearMonth.now().toString()
)

data class SpendingLimitSummary(
    val limit: SpendingLimit,
    val progressPercent: Int,
    val progressFraction: Float,
    val remainingAmount: BigDecimal,
    val isExceeded: Boolean
)

fun SpendingLimit.toSummary(): SpendingLimitSummary {
    val safeLimitAmount = limitAmount.takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE
    val rawPercent = spentAmount
        .multiply(BigDecimal(100))
        .divide(safeLimitAmount, 0, RoundingMode.HALF_UP)
        .toInt()
    val remaining = limitAmount.subtract(spentAmount).max(BigDecimal.ZERO)

    return SpendingLimitSummary(
        limit = this,
        progressPercent = rawPercent,
        progressFraction = (rawPercent / 100f).coerceIn(0f, 1f),
        remainingAmount = remaining,
        isExceeded = spentAmount > limitAmount
    )
}
