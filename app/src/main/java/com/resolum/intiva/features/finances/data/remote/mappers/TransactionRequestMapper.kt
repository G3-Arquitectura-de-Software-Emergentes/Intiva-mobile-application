package com.resolum.intiva.features.finances.data.remote.mappers

import com.resolum.intiva.features.finances.data.remote.models.RegisterTransactionRequestDto
import com.resolum.intiva.features.finances.domain.models.RegisterTransactionRequest

/**
 * Mapper object to convert between RegisterTransactionRequest domain model and RegisterTransactionRequestDto.
 */
fun RegisterTransactionRequest.toDto(
    userId: Long,
    performedByUserId: Long = userId
): RegisterTransactionRequestDto {
    return RegisterTransactionRequestDto(
        amount = amount,
        currencyCode = currencyCode,
        description = description,
        financialAccountId = financialAccountId,
        userId = userId,
        transactionType = transactionType.name,
        categoryId = categoryId,
        ownerType = ownerType,
        performedByUserId = performedByUserId,
        aiCategorized = aiCategorized,
    )
}
