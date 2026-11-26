package com.resolum.intiva.features.finances.data.remote.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * Data Transfer Object (DTO) for registering a financial transaction.
 *
 * This class is used to send the necessary data to the backend API when registering a new transaction.
 *
 * @property amount The amount of the transaction as a double.
 * @property currencyCode The currency code (e.g., "USD", "EUR") for the transaction.
 * @property description A brief description of the transaction.
 * @property financialAccountId The ID of the financial account associated with the transaction.
 * @property userId The ID of the user who owns the transaction.
 * @property performedByUserId The ID of the user who performed the transaction.
 * @property transactionType The type of the transaction (e.g., "income", "expense").
 * @property categoryId The optional ID of the category associated with the transaction.
 * @property ownerType The type of owner (e.g., "user", "group") for the transaction.
 */
data class RegisterTransactionRequestDto(

    @SerializedName("amount")
    val amount: BigDecimal,

    @SerializedName("currencyCode")
    val currencyCode: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("financialAccountId")
    val financialAccountId: Long,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("performedByUserId")
    val performedByUserId: Long,

    @SerializedName("transactionType")
    val transactionType: String,

    @SerializedName("categoryId")
    val categoryId: Long?,

    @SerializedName("ownerType")
    val ownerType: String,

    @SerializedName("aiCategorized")
    val aiCategorized: Boolean = false
)
