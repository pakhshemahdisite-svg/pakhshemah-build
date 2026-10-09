package com.pakhshmahdi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pakhshmahdi.app.ui.theme.PMTheme

@Composable
fun PMScreenTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = PMTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = c.surface,
                border = BorderStroke(1.dp, c.border)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "بازگشت", tint = c.textPrimary)
                }
            }
            Spacer(Modifier.width(10.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = c.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )
            }
        }

        trailing?.invoke()
    }
}

@Composable
fun PMSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onSearch: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val c = PMTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PMTheme.shapes.medium,
        color = c.surfaceElevated,
        border = BorderStroke(1.dp, c.border)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = {
                Text(
                    placeholder,
                    color = c.textMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = c.textMuted)
            },
            trailingIcon = onSearch?.let { action ->
                {
                    TextButton(onClick = action) {
                        Text("جستجو", color = c.primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = c.surfaceElevated,
                unfocusedContainerColor = c.surfaceElevated,
                disabledContainerColor = c.surfaceElevated,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                cursorColor = c.primary,
                focusedTextColor = c.textPrimary,
                unfocusedTextColor = c.textPrimary
            )
        )
    }
}

@Composable
fun PMPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    val c = PMTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(PMTheme.dimensions.buttonHeight),
        shape = PMTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = c.primary,
            contentColor = if (c.primary == c.accentGold) c.primaryDark else androidx.compose.ui.graphics.Color.White,
            disabledContainerColor = c.border,
            disabledContentColor = c.textMuted
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = if (c.primary == c.accentGold) c.primaryDark else androidx.compose.ui.graphics.Color.White
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PMSectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    val c = PMTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = c.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (!action.isNullOrBlank() && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, color = c.accentGold, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PMEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val c = PMTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = c.surfaceElevated,
            border = BorderStroke(1.dp, c.border)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = c.primary,
                modifier = Modifier.padding(24.dp).size(46.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = c.textPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary
        )
        if (!action.isNullOrBlank() && onAction != null) {
            Spacer(Modifier.height(16.dp))
            PMPrimaryButton(action, onAction, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun PMErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PMTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "اتصال برقرار نشد",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = c.textPrimary
        )
        Spacer(Modifier.height(7.dp))
        Text(message, color = c.textSecondary)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRetry,
            border = BorderStroke(1.dp, c.border),
            shape = PMTheme.shapes.medium
        ) {
            Text("تلاش دوباره", color = c.primary)
        }
    }
}

@Composable
fun PMSelectableCard(
    selected: Boolean,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null
) {
    val c = PMTheme.colors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = PMTheme.shapes.medium,
        color = if (selected) c.accentGold.copy(alpha = .10f) else c.surfaceElevated,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) c.accentGold else c.border)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = c.textPrimary, fontWeight = FontWeight.Bold)
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                }
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = c.accentGold)
            )
        }
    }
}

@Composable
fun PMCheckoutSteps(active: Int, labels: List<String>) {
    val c = PMTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        labels.forEachIndexed { index, label ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    color = if (index <= active) c.accentGold else c.surfaceElevated,
                    border = BorderStroke(1.dp, if (index <= active) c.accentGold else c.border)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            (index + 1).toString(),
                            color = if (index <= active && c.primary == c.accentGold) c.primaryDark
                            else if (index <= active) androidx.compose.ui.graphics.Color.White
                            else c.textMuted,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == active) c.accentGold else c.textMuted
                )
            }
            if (index != labels.lastIndex) {
                Box(
                    Modifier
                        .padding(top = 14.dp)
                        .height(1.dp)
                        .weight(.55f)
                        .background(if (index < active) c.accentGold else c.border)
                )
            }
        }
    }
}
