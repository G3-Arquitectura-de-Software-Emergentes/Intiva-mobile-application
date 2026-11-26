package com.resolum.intiva.features.finances.domain.models

import java.math.BigDecimal

/**
 * Represents the data required to register a financial transaction.
 *
 * @property amount The amount of the transaction.
 * @property currencyCode The currency code (e.g., "USD", "EUR") for the transaction.
 * @property description A brief description of the transaction.
 * @property financialAccountId The ID of the financial account associated with the transaction.
 * @property transactionType The type of the transaction (e.g., "income", "expense").
 * @property categoryId The optional ID of the category associated with the transaction.
 * @property ownerType The type of owner (e.g., "user", "group") for the transaction.
 */
data class RegisterTransactionRequest(
    val amount: BigDecimal,
    val currencyCode: String,
    val description: String,
    val financialAccountId: Long,
    val transactionType: TransactionType,
    val categoryId: Long,
    val ownerType: String,
    val aiCategorized: Boolean = false
)
