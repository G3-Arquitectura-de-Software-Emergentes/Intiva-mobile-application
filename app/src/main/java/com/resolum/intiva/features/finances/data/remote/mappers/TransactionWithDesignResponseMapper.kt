package com.resolum.intiva.features.finances.data.remote.mappers

import com.resolum.intiva.features.finances.data.remote.models.TransactionWithDesignResponseDto
import com.resolum.intiva.features.finances.domain.models.CategoryDesign
import com.resolum.intiva.features.finances.domain.models.Transaction
import com.resolum.intiva.features.finances.domain.models.TransactionWithDesignResponse

/**
 * Extension function to map a [Transaction] domain model to a [TransactionWithDesignResponseDto].
 *
 * This function takes a [Transaction] object and converts it into a [TransactionWithDesignResponseDto] object
 * by mapping each corresponding field. This allows the application to work with the domain model
 * while still being able to send data to the remote API in the form of DTOs.
 */
fun TransactionWithDesignResponseDto.toDomain(): TransactionWithDesignResponse {
    return TransactionWithDesignResponse(
        id = id,
        amount = amount,
        currencyCode = currencyCode,
        description = description,
        ownerId = ownerId,
        financialAccountId = financialAccountId,
        actorUserId = actorUserId,
        transactionType = transactionType,
        categoryId = categoryId,
        aiCategorized = aiCategorized,
        registeredAt = registeredAt,
        categoryDesign = CategoryDesign(
            categoryColor = categoryDesign.categoryColor,
            categoryIcon = categoryDesign.categoryIcon
        )
    )
}