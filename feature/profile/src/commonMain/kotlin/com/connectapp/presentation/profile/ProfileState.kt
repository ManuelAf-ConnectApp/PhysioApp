package com.connectapp.presentation.profile

import com.connectapp.domain.model.Account
import com.connectapp.domain.model.ProfessionalProfile

data class ProfileState(
    val isLoading: Boolean = true,
    val account: Account? = null,
    val professional: ProfessionalProfile? = null,
)
