package com.connectapp.commonresources.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.connectapp.commonresources.content_desc_toggle_password
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Text field that shows its validation [error] next to it once the user has left the field
 * ("cuando salgo del campo, veo un error junto al campo").
 * [supportingText] is shown while there is no visible error, e.g. the password policy.
 * A [KeyboardType.Password] field masks its value and offers a visibility toggle.
 */
// Mirrors OutlinedTextField's own parameter list (value, label, supporting text, keyboard…): a
// wrapper object would only make every form call site harder to read.
@Suppress("LongParameterList")
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: StringResource,
    error: StringResource?,
    modifier: Modifier = Modifier,
    supportingText: StringResource? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    /** More than 1 makes a multi-line field (notes, addresses). */
    minLines: Int = 1,
) {
    var hadFocus by remember { mutableStateOf(false) }
    var leftField by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val isPassword = keyboardOptions.keyboardType == KeyboardType.Password
    val errorText = error?.takeIf { leftField }?.let { stringResource(it) }
    val bottomText = errorText ?: supportingText?.let { stringResource(it) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        modifier = modifier
            .onFocusChanged { state ->
                if (state.isFocused) hadFocus = true else if (hadFocus) leftField = true
            }
            .semantics { if (errorText != null) error(errorText) },
        singleLine = minLines == 1,
        minLines = minLines,
        isError = errorText != null,
        shape = MaterialTheme.shapes.medium,
        supportingText = bottomText?.let { text -> { Text(text) } },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = if (isPassword) keyboardOptions.copy(autoCorrectEnabled = false) else keyboardOptions,
        trailingIcon = if (isPassword) {
            { PasswordVisibilityToggle(visible = passwordVisible, onToggle = { passwordVisible = !passwordVisible }) }
        } else {
            null
        },
    )
}

@Composable
private fun PasswordVisibilityToggle(visible: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = stringResource(content_desc_toggle_password),
        )
    }
}
