package com.connectapp.presentation.forgot_password

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/*

Author: Manuel Alconchel Fernández

Web: https://connectapp.es

Email: info@connectapp.es

Date: 11/06/2026

 */

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    val viewModel: ForgotPasswordViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val onIntent = viewModel::onIntent

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ForgotPasswordEffect.NavigateToLogin -> onNavigateBack()
            }
        }
    }

   if(state.isLoading){
       Box(
           modifier = modifier.fillMaxSize(),
           contentAlignment = Alignment.Center
       ){
           CircularProgressIndicator()
       }
   }else{
       Box(
           modifier = modifier.fillMaxSize(),
           contentAlignment = Alignment.Center
       ) {
           ForgotPasswordScreenContent(state = state, onIntent = onIntent)
       }
    }
}

@Composable
fun ForgotPasswordScreenContent(
    state: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DimensResources.spacing16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(TokensResources.forgotPasswordTitle), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(DimensResources.spacing8))

        Text(
            text = stringResource(TokensResources.forgotPasswordInstruction),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing16))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.email,
            onValueChange = { onIntent(ForgotPasswordIntent.EmailChanged(it)) },
            label = { Text(stringResource(TokensResources.email)) },
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            )
        )

        Spacer(modifier = Modifier.height(DimensResources.spacing16))

        AnimatedContent(targetState = state) { targetState ->
            when {
                targetState.isLoading -> CircularProgressIndicator()
                targetState.isEmailSent -> {
                    Text(
                        text = stringResource(TokensResources.emailSentSuccess),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                else -> {
                    Button(
                        onClick = { onIntent(ForgotPasswordIntent.SendEmailClicked) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(TokensResources.sendResetLink))
                    }
                }
            }
        }

        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        TextButton(onClick = { onIntent(ForgotPasswordIntent.BackToLoginClicked) }) {
            Text(stringResource(TokensResources.backToLogin))
        }
    }

}
