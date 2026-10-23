package com.connectapp.presentation.professionals.detail

import com.connectapp.domain.professionals.Professional
import org.jetbrains.compose.resources.StringResource

enum class ProfessionalDialog { ARCHIVE, DELETE }

data class ProfessionalDetailState(
    val professional: Professional? = null,
    val isLoading: Boolean = true,
    val dialog: ProfessionalDialog? = null,
    val errorMessage: StringResource? = null,
) {
    override fun toString(): String = "ProfessionalDetailState(isLoading=$isLoading, dialog=$dialog)"
}

sealed interface ProfessionalDetailIntent {
    object ArchiveClicked : ProfessionalDetailIntent
    object UnarchiveClicked : ProfessionalDetailIntent
    object DeleteClicked : ProfessionalDetailIntent
    object DialogConfirmed : ProfessionalDetailIntent
    object DialogDismissed : ProfessionalDetailIntent
}

sealed interface ProfessionalDetailEffect {
    object Deleted : ProfessionalDetailEffect
}
