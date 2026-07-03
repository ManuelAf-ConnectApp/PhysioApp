package com.connectapp.presentation.util

import androidx.compose.runtime.Composable
import com.connectapp.designresources.TokensResources
import com.connectapp.domain.validator.model.ValidationError
import org.jetbrains.compose.resources.stringResource

@Composable
fun ValidationError.asString(): String {
    return when (this) {
        ValidationError.FIELD_EMPTY -> stringResource(TokensResources.errorFieldEmpty)
        ValidationError.TOO_SHORT -> stringResource(TokensResources.errorTooShort)
        ValidationError.INVALID_EMAIL -> stringResource(TokensResources.errorInvalidEmail)
        ValidationError.INVALID_PHONE -> stringResource(TokensResources.errorInvalidPhone)
        ValidationError.INVALID_DNI -> stringResource(TokensResources.errorInvalidDni)
        ValidationError.INVALID_DNI_LETTER -> stringResource(TokensResources.errorInvalidDniLetter)
        ValidationError.INVALID_PASSWORD -> stringResource(TokensResources.errorInvalidPassword)
        ValidationError.PASSWORDS_NOT_MATCH -> stringResource(TokensResources.errorPasswordsNotMatch)
    }
}
