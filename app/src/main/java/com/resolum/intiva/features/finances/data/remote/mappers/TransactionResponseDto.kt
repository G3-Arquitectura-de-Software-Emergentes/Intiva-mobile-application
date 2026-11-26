package com.resolum.intiva.features.finances.data.remote.mappers

import com.resolum.intiva.features.finances.data.remote.models.TransactionResponseDto
import com.resolum.intiva.features.finances.data.remote.models.TransactionWithDesignResponseDto
import com.resolum.intiva.features.finances.domain.models.Transaction
import com.resolum.intiva.features.paymentmethodsandcategories.data.remote.mappers.toDomain

/**
 * Extension function to map a [Transaction] domain model to a [TransactionWithDesignResponseDto].
 *
 * This function takes a [Transaction] object and converts it into a [TransactionWithDesignResponseDto] object
 * by mapping each corresponding field. This allows the application to work with the domain model
 * while still being able to send data to the remote API in the form of DTOs.
 */
fun TransactionResponseDto.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        currencyCode = currencyCode,
        description = description,
        ownerId = ownerId,
        financialAccountId = financialAccountId,
        financialAccountName = financialAccountName,
        actorUserId = actorUserId,
        transactionType = transactionType,
        categoryId = categoryId,
        registeredAt = registeredAt,
        financialAccount = financialAccount?.toDomain(),
        aiCategorized = aiCategorized,
    )
}
