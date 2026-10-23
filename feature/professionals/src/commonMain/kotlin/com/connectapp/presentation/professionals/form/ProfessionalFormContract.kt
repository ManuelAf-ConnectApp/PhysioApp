package com.connectapp.presentation.professionals.form

import com.connectapp.domain.professionals.ProfessionalDraft
import com.connectapp.domain.professionals.ProfessionalField
import com.connectapp.domain.professionals.ProfessionalValidation
import org.jetbrains.compose.resources.StringResource

data class ProfessionalFormState(
    val draft: ProfessionalDraft = ProfessionalDraft(),
    /** What was loaded or last saved: leaving with a different [draft] asks for confirmation. */
    val savedDraft: ProfessionalDraft = ProfessionalDraft(),
    /** True in edit mode (the form opens prefilled and updates the same record). */
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: StringResource? = null,
) {
    val invalidFields: Set<ProfessionalField> get() = ProfessionalValidation.errors(draft)

    val canSubmit: Boolean get() = invalidFields.isEmpty() && !isSaving && !isLoading

    val hasUnsavedChanges: Boolean get() = draft != savedDraft

    // Personal data never reaches logs.
    override fun toString(): String = "ProfessionalFormState(isEditing=$isEditing, isSaving=$isSaving)"
}

sealed interface ProfessionalFormIntent {
    /** Any field edit: the screen passes the whole updated draft. */
    data class DraftChanged(val draft: ProfessionalDraft) : ProfessionalFormIntent
    object SaveClicked : ProfessionalFormIntent
}

sealed interface ProfessionalFormEffect {
    data class Saved(val professionalId: String) : ProfessionalFormEffect
}
