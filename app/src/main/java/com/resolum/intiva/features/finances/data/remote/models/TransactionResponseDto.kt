package com.resolum.intiva.features.finances.data.remote.models

import com.google.gson.annotations.SerializedName
import com.resolum.intiva.features.paymentmethodsandcategories.data.remote.models.FinancialAccountResponseDto

/**
 * Data Transfer Object (DTO) representing a financial transaction response from the API.
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
data class TransactionResponseDto(

    @SerializedName("id")
    val id: Long,

    @SerializedName("amount")
    val amount: String,

    @SerializedName("currencyCode")
    val currencyCode: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("ownerId")
    val ownerId: Long,

    @SerializedName("financialAccountId")
    val financialAccountId: Long,

    @SerializedName("financialAccountName")
    val financialAccountName: String? = null,

    @SerializedName("actorUserId")
    val actorUserId: Long,

    @SerializedName("transactionType")
    val transactionType: String,

    @SerializedName("categoryId")
    val categoryId: Long?,

    @SerializedName("registeredAt")
    val registeredAt: String? = null,

    @SerializedName(value = "financialAccount", alternate = ["account"])
    val financialAccount: FinancialAccountResponseDto? = null,
    @SerializedName("aiCategorized")
    val aiCategorized: Boolean = false,
)
