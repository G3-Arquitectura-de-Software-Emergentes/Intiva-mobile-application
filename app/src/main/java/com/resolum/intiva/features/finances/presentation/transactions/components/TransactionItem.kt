package com.resolum.intiva.features.finances.presentation.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.resolum.intiva.features.finances.domain.models.TransactionType
import com.resolum.intiva.features.finances.domain.models.TransactionWithDesignResponse
import com.resolum.intiva.features.paymentmethodsandcategories.presentation.category.components.CategoryIcon
import com.resolum.intiva.core.utils.date.toRelativeDateTime

/**
 * Composable function representing a single transaction item in the transaction list.
 *
 * This component displays the transaction description, amount, and an icon representing the category.
 * The background color and amount color are dynamically determined based on the transaction type (income or expense).
 *
 * @param transaction The transaction data to be displayed, including design information for category color and icon.
 */
@Composable
fun TransactionItem(
    transaction: TransactionWithDesignResponse,
    onClick: (TransactionWithDesignResponse) -> Unit = {}
) {
    val isIncome = transaction.transactionType == TransactionType.INCOME.name

    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE53935)
    val amountPrefix = if (isIncome) "+" else "-"

    val dynamicBackgroundColor = runCatching {
        Color(transaction.categoryDesign?.categoryColor?.toColorInt() ?: throw Exception())
    }.getOrElse {
        if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    }

    val iconName = transaction.categoryDesign?.categoryIcon ?: "default"


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = dynamicBackgroundColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                CategoryIcon(
                    iconName = iconName,
                    tintColor = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description + if (transaction.aiCategorized) " (IA)" else "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = transaction.registeredAt.toRelativeDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = "$amountPrefix ${transaction.currencyCode} ${formatAmount(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

private fun formatAmount(amount: String): String {
    return runCatching {
        amount.toBigDecimal().setScale(2).toPlainString()
    }.getOrElse { amount }
}
