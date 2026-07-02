package com.example.feature.notes.noteList.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.AppShapes
import com.example.core.designsystem.theme.AppTextStyle
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.Faint
import com.example.core.designsystem.theme.Spacing
import com.example.core.ui.HorizontalSpacer
import com.example.core.ui.noRippleClickable
import com.example.feature.notes.R

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = AppTextStyle.NoteBody.copy(color = AppTheme.colors.sub),
        singleLine = true,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(AppTheme.colors.accent),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isFocused) AppTheme.colors.accent else AppTheme.colors.faint,
                        shape = AppShapes.extraLarge
                    )
                    .padding(horizontal = Spacing.md, vertical = Spacing.md)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AppTheme.colors.faint,
                    modifier = Modifier.size(20.dp)
                )
                HorizontalSpacer(Spacing.sm)
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(stringResource(R.string.notes_search_placeholder), style = AppTextStyle.Chip, color = Faint)
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    HorizontalSpacer(Spacing.sm)
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.notes_clear_search_cd),
                        tint = AppTheme.colors.sub,
                        modifier = Modifier
                            .size(20.dp)
                            .noRippleClickable() { onClearClick() }
                    )
                }
            }
        }
    )
}
