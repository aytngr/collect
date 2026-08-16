package com.aytngr.feature.notes.noteList.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.aytngr.core.designsystem.theme.AppShapes
import com.aytngr.core.designsystem.theme.AppTextStyle
import com.aytngr.core.designsystem.theme.AppTheme
import com.aytngr.core.designsystem.theme.Spacing
import com.aytngr.domain.models.NoteCategory
import com.aytngr.feature.notes.R

@Composable
fun CategoryChip(
    selectedCategory: NoteCategory?,
    onCategorySelected: (NoteCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(R.string.notes_category_all), style = AppTextStyle.Chip) },
                shape = AppShapes.extraLarge,
                colors = FilterChipDefaults.filterChipColors(
                    labelColor = AppTheme.colors.sub,
                    selectedContainerColor = AppTheme.colors.accent,
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == null,
                    borderColor = AppTheme.colors.line,
                    selectedBorderColor = Color.Transparent,
                )
            )
        }
        items(NoteCategory.entries.toTypedArray()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { (Text(category.name.lowercase().replaceFirstChar { it.uppercase() }, style = AppTextStyle.Chip)) },
                shape = AppShapes.extraLarge,
                colors = FilterChipDefaults.filterChipColors(
                    labelColor = AppTheme.colors.sub,
                    selectedContainerColor = AppTheme.colors.accent,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedCategory == category,
                    borderColor = AppTheme.colors.line,
                    selectedBorderColor = Color.Transparent,
                )
            )
        }
    }
}