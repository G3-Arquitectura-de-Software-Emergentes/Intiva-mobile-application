package com.resolum.intiva.features.finances.domain.models

import com.resolum.intiva.features.paymentmethodsandcategories.domain.models.FinancialAccount

/**
 * Represents a financial transaction resource with all its details.
 *
 * @property id The unique identifier of the transaction.
 * @property amount The amount of the transaction as a string (e.g., "100.00").
 * @property currencyCode The currency code (e.g., "USD", "EUR") for the transaction.
 * @property description A brief description of the transaction.
 * @property ownerId The ID of the owner of the transaction (e.g., user or group).
 * @property financialAccountId The ID of the financial account associated with the transaction.
 * @property actorUserId The ID of the user who performed the transaction.
 * @property transactionType The type of the transaction (e.g., "income", "expense").
 * @property categoryId The optional ID of the category associated with the transaction.
 */
data class Transaction(
    val id: Long,
    val amount: String,
    val currencyCode: String,
    val description: String,
    val ownerId: Long,
    val financialAccountId: Long,
    val financialAccountName: String? = null,
    val actorUserId: Long,
    val transactionType: String,
    val categoryId: Long?,
    val registeredAt: String? = null,
    val financialAccount: FinancialAccount? = null,
    val aiCategorized: Boolean = false
)
