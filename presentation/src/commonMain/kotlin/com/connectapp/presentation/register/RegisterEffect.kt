package com.connectapp.presentation.register

import com.connectapp.domain.model.User

sealed interface RegisterEffect {
    data object NavigateToLogin : RegisterEffect
}
