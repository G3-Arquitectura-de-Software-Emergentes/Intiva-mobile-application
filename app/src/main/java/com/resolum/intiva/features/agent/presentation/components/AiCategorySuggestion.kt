package com.resolum.intiva.features.agent.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Description field + "Sugerir categoría con IA" + the suggestion box, as in the product video.
 * The suggestion never saves the expense: the user still taps "Registrar Gasto".
 */
@Composable
fun AiCategorySuggestion(
    description: String,
    onDescriptionChange: (String) -> Unit,
    canSuggest: Boolean,
    busy: Boolean,
    onSuggest: () -> Unit,
    suggestedCategory: String?,
    unavailable: Boolean,
    onUseSuggestion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción del gasto") },
            placeholder = { Text("Compras del supermercado") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgentColors.Purple,
                focusedLabelColor = AgentColors.Purple,
                unfocusedBorderColor = AgentColors.CardBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = AgentColors.Purple,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        AgentPillButton(
            text = if (busy) "Buscando sugerencia…" else "Sugerir categoría con IA",
            onClick = onSuggest,
            enabled = canSuggest,
            loading = busy,
        )
        when {
            suggestedCategory != null -> AiNote(
                text = buildAnnotatedString {
                    append("Sugerencia: ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(suggestedCategory) }
                    append(". El gasto aún no se ha guardado.")
                },
                action = { AgentPillButton("Usar esta categoría", onUseSuggestion, icon = null, compact = true) },
            )
            unavailable -> AiNote(
                "No hay una sugerencia disponible. Puedes elegir una categoría abajo.",
                icon = Icons.Outlined.Info,
            )
        }
    }
}
