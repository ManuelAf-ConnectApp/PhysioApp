package com.connectapp.presentation.patients.form

import com.connectapp.domain.patients.PatientDraft
import com.connectapp.domain.patients.PatientField
import com.connectapp.domain.patients.PatientValidation
import com.connectapp.presentation.patients.consent.ConsentFormState
import org.jetbrains.compose.resources.StringResource

data class PatientFormState(
    val draft: PatientDraft = PatientDraft(),
    /** What was loaded or last saved: leaving with other data asks for confirmation. */
    val savedDraft: PatientDraft = PatientDraft(),
    /** Only when creating: the consent is signed with the registration (D13). */
    val consent: ConsentFormState = ConsentFormState(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: StringResource? = null,
) {
    val invalidFields: Set<PatientField>
        get() = PatientValidation.errors(draft) + if (isEditing) emptySet() else consent.invalidFields

    val canSubmit: Boolean get() = invalidFields.isEmpty() && !isSaving && !isLoading

    val hasUnsavedChanges: Boolean
        get() = !isSaved && (draft != savedDraft || (!isEditing && !consent.isEmpty))

    // Personal and health data never reach logs.
    override fun toString(): String = "PatientFormState(isEditing=$isEditing, isSaving=$isSaving)"
}

sealed interface PatientFormIntent {
    data class DraftChanged(val draft: PatientDraft) : PatientFormIntent
    data class ConsentChanged(val consent: ConsentFormState) : PatientFormIntent

    /** [signaturePng] is the pad rendered by the screen; empty in edit mode. */
    class SaveClicked(val signaturePng: ByteArray) : PatientFormIntent
}

sealed interface PatientFormEffect {
    data class Saved(val patientId: String) : PatientFormEffect
}
