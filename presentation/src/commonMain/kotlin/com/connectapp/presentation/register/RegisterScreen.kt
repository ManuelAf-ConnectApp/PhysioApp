/*
Author: Manuel María Alconchel Fernández
E-mail: incidencias@connectapp.es
Date: 30/06/2006

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.connectapp.presentation.register

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import com.connectapp.presentation.component.CustomOutlinedTextField
import com.connectapp.presentation.util.asString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
) {
    val viewModel: RegisterViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterEffect.NavigateToLogin -> {
                    snackBarHostState.showSnackbar(
                        message = "Registration successful",
                        duration = SnackbarDuration.Short
                    )
                    onNavigateBack()
                }
            }
        }
    }
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            RegisterScreenContent(state, onIntent)
        }
    }

}

@Composable
fun RegisterScreenContent(
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {

        Spacer(modifier = Modifier.height(DimensResources.spacing16))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(TokensResources.createAccount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing16))

        CustomOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.firstName,
            onValueChange = { onIntent(RegisterIntent.FirstNameChanged(it)) },
            label = stringResource(TokensResources.firstName),
            isError = state.firstNameError != null,
            supportingText = state.firstNameError?.let { error ->
                { Text(text = error.asString()) }
            },
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing24))

        CustomOutlinedTextField(
            value = state.lastName,
            onValueChange = { onIntent(RegisterIntent.LastNameChanged(it)) },
            label = stringResource(TokensResources.lastName),
            modifier = Modifier.fillMaxWidth(),
            isError = state.lastNameError != null,
            supportingText = state.lastNameError?.let { error ->
                { Text(text = error.asString()) }
            },
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ),
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing24))

        CustomOutlinedTextField(
            value = state.email,
            onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
            label = stringResource(TokensResources.email),
            modifier = Modifier.fillMaxWidth(),
            isError = state.emailError != null,
            supportingText = state.emailError?.let { error ->
                { Text(text = error.asString()) }
            },
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            )
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing24))

        CustomOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.password,
            onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
            label = stringResource(TokensResources.password),
            isError = state.passwordError != null,
            supportingText = state.passwordError?.let { error ->
                { Text(text = error.asString()) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ),
            isPassword = true,
            isPasswordVisible = state.isPasswordVisible,
            onPasswordVisibilityToggle = { onIntent(RegisterIntent.TogglePasswordVisibility) }
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing24))

        CustomOutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
            label = stringResource(TokensResources.confirmPassword),
            modifier = Modifier.fillMaxWidth(),
            isError = state.confirmPasswordError != null,
            supportingText = state.confirmPasswordError?.let { error ->
                { Text(text = error.asString()) }
            },
            shape = MaterialTheme.shapes.large,
            isPassword = true,
            isPasswordVisible = state.isConfirmPasswordVisible,
            onPasswordVisibilityToggle = { onIntent(RegisterIntent.ToggleConfirmPasswordVisibility) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onIntent(RegisterIntent.RegisterClicked)
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing48))

        Button(
            onClick = { onIntent(RegisterIntent.RegisterClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isFormValid
        ) {
            Text(stringResource(TokensResources.createAccount))
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        TextButton(onClick = { onIntent(RegisterIntent.LoginClicked) }) {
            Text(stringResource(TokensResources.backToLogin))
        }
    }
}
