package com.dergoogler.liquidwars.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.liquidwars.R

@Composable
fun ConfirmDialog(
    title: @Composable (() -> Unit)?,
    description: @Composable (() -> Unit)?,
    confirmText: @Composable (RowScope.() -> Unit),
    cancelText: @Composable (RowScope.() -> Unit),
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        title = title,
        text = description,
        onDismissRequest = {
            onClose()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                },
                content = confirmText
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onClose()
                },
                content = cancelText
            )
        }
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    description: String,
    cancelText: String = stringResource(id = R.string.cancel),
    confirmText: String = stringResource(id = R.string.confirm),
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) = ConfirmDialog(
    title = {
        Text(text = title)
    },
    description = {
        Text(text = description)
    },
    confirmText = {
        Text(text = confirmText)
    },
    cancelText = {

        Text(text = cancelText)
    },
    onClose = onClose,
    onConfirm = onConfirm
)

@Composable
fun ConfirmDialog(
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes confirmText: Int = R.string.confirm,
    @StringRes cancelText: Int = R.string.cancel,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) = ConfirmDialog(
    title = {
        Text(text = stringResource(title))
    },
    description = {
        Text(text = stringResource(description))
    },
    confirmText = {
        Text(text = stringResource(confirmText))
    },
    cancelText = {

        Text(text = stringResource(cancelText))
    },
    onClose = onClose,
    onConfirm = onConfirm
)