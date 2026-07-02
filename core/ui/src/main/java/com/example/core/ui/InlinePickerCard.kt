package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.AppShapes
import com.example.core.designsystem.theme.AppTheme

@Composable
fun InlinePickerCard(
    onCancel: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.4f))
            .noRippleClickable { onCancel() },
        contentAlignment = Alignment.Center,
    ){
        Surface(
            shape = AppShapes.medium,
            color = AppTheme.colors.bg,
            modifier = Modifier
                .padding(16.dp)
                .noRippleClickable {}
        ){
            Column(
                Modifier.padding(16.dp)
            ) {
                content()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text(stringResource(R.string.inline_picker_cancel)) }
                    TextButton(onClick = onSelect) { Text(stringResource(R.string.inline_picker_ok)) }
                }
            }
        }
    }
}