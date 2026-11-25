package com.resolum.intiva.features.agent.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resolum.intiva.core.ui.theme.IntivaColors
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/** Visual tokens for the AI agent screens, taken from the product video. */
object AgentColors {
    val Purple = IntivaColors.PrimaryBrand
    val Lime = IntivaColors.PrimaryAction
    val Lavender = Color(0xFFF1EFFC)
    val CardBorder = Color(0xFFE7E4ED)
    val NeutralBadge = Color(0xFFF2F1F6)
    val AmberBg = Color(0xFFFFF1D6)
    val AmberText = Color(0xFF8A5A00)
    val GreenBg = Color(0xFFE5F4E4)
    val GreenText = Color(0xFF1B7A35)
    val RedBg = Color(0xFFFDECEC)
    val RedText = Color(0xFFC62828)
    val BlueBg = Color(0xFFE7F1FB)
    val BlueText = Color(0xFF1E4F91)
    val FoodGreen = Color(0xFF43A047)
    val SavingsGreen = Color(0xFF8BC34A)
    val ReminderOrange = Color(0xFFFB8C00)
}

private val soles = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-PE")).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
}

/** Formats an amount as "S/ 1,240.50" (no decimals when the amount is whole). */
fun soles(amount: BigDecimal?): String = amount?.let { "S/ ${soles.format(it)}" } ?: "—"

/** Purple filled pill (e.g. "Generar plan") or outlined pill (e.g. "Sugerir categoría con IA"). */
@Composable
fun AgentPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    icon: ImageVector? = Icons.Filled.AutoAwesome,
    enabled: Boolean = true,
    loading: Boolean = false,
    compact: Boolean = false,
) {
    val padding = if (compact) PaddingValues(horizontal = 14.dp, vertical = 4.dp)
    else PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = if (filled) Color.White else AgentColors.Purple,
            )
            Spacer(Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = if (compact) 13.sp else 15.sp)
    }
    if (filled) {
        Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = modifier,
            shape = RoundedCornerShape(50),
            contentPadding = padding,
            colors = ButtonDefaults.buttonColors(
                containerColor = AgentColors.Purple,
                contentColor = Color.White,
                disabledContainerColor = AgentColors.Purple.copy(alpha = 0.5f),
                disabledContentColor = Color.White,
            ),
        ) { content() }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = modifier,
            shape = RoundedCornerShape(50),
            contentPadding = padding,
            border = BorderStroke(1.dp, if (enabled) AgentColors.Purple.copy(alpha = 0.35f) else AgentColors.CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AgentColors.Purple,
                disabledContentColor = AgentColors.Purple.copy(alpha = 0.4f),
            ),
        ) { content() }
    }
}

/** Full-width lime call to action ("Aprobar y aplicar plan"). */
@Composable
fun AgentPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = AgentColors.Lime,
            contentColor = IntivaColors.TextPrimary,
            disabledContainerColor = AgentColors.Lime.copy(alpha = 0.45f),
            disabledContentColor = IntivaColors.TextPrimary.copy(alpha = 0.5f),
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = IntivaColors.TextPrimary)
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

/** Full-width outlined secondary action ("Guardar cambios en borrador"). */
@Composable
fun AgentSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, AgentColors.CardBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AgentColors.Purple),
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

/** Lavender box with the sparkle icon, used for the agent's explanation and AI hints. */
@Composable
fun AiNote(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.AutoAwesome,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AgentColors.Lavender, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = AgentColors.Purple, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontSize = 14.sp, color = IntivaColors.TextPrimary, lineHeight = 20.sp)
        }
        action?.invoke()
    }
}

@Composable
fun AiNote(text: String, modifier: Modifier = Modifier, icon: ImageVector = Icons.Filled.AutoAwesome) =
    AiNote(AnnotatedString(text), modifier, icon)

enum class ChipTone { Neutral, Amber, Green, Red, Blue }

/** Small rounded label: plan status ("Borrador · versión 1") and action badges ("Propuesto"). */
@Composable
fun AgentChip(text: String, tone: ChipTone, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val (bg, fg) = when (tone) {
        ChipTone.Neutral -> AgentColors.NeutralBadge to IntivaColors.TextSecondary
        ChipTone.Amber -> AgentColors.AmberBg to AgentColors.AmberText
        ChipTone.Green -> AgentColors.GreenBg to AgentColors.GreenText
        ChipTone.Red -> AgentColors.RedBg to AgentColors.RedText
        ChipTone.Blue -> AgentColors.BlueBg to AgentColors.BlueText
    }
    Row(
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RiskChip(text: String, modifier: Modifier = Modifier) =
    AgentChip(text, ChipTone.Red, modifier, Icons.Filled.Warning)

/** White card with a round colored icon, a title, an optional subtitle and a badge on the right. */
@Composable
fun PlanActionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    badge: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, AgentColors.CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).background(iconColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = IntivaColors.TextPrimary)
                if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = IntivaColors.TextSecondary)
            }
            badge()
        }
        content()
    }
}

/** "Gasto estimado ........ S/ 580" row. */
@Composable
fun AmountRow(label: String, value: String, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = IntivaColors.TextSecondary, modifier = Modifier.weight(1f))
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (highlight) AgentColors.Purple else IntivaColors.TextPrimary,
        )
    }
}
