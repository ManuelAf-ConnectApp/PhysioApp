package com.connectapp.presentation.patients.form

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.patient_not_found
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.patients.ConsentSignature
import com.connectapp.domain.patients.CreatePatientUseCase
import com.connectapp.domain.patients.ObservePatientUseCase
import com.connectapp.domain.patients.UpdatePatientUseCase
import com.connectapp.domain.patients.toDraft
import com.connectapp.presentation.patients.toMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * "Registrar paciente" with its consent signature (D13) and, with a
 * [patientId], its edit mode.
 */
class PatientFormViewModel(
    private val patientId: String?,
    private val createPatient: CreatePatientUseCase,
    private val updatePatient: UpdatePatientUseCase,
    observePatient: ObservePatientUseCase,
) : MviViewModel<PatientFormState, PatientFormIntent, PatientFormEffect>(
    PatientFormState(isEditing = patientId != null, isLoading = patientId != null),
) {

    init {
        if (patientId != null) {
            viewModelScope.launch {
                val patient = observePatient(patientId).first()
                updateState {
                    if (patient == null) {
                        it.copy(isLoading = false, errorMessage = patient_not_found)
                    } else {
                        it.copy(isLoading = false, draft = patient.toDraft(), savedDraft = patient.toDraft())
                    }
                }
            }
        }
    }

    override fun reduce(intent: PatientFormIntent) {
        when (intent) {
            is PatientFormIntent.DraftChanged -> updateState { it.copy(draft = intent.draft, errorMessage = null) }
            is PatientFormIntent.ConsentChanged ->
                updateState { it.copy(consent = intent.consent, errorMessage = null) }
            is PatientFormIntent.SaveClicked -> save(intent.signaturePng)
        }
    }

    private fun save(signaturePng: ByteArray) {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (patientId == null) {
                val consent = form.consent
                createPatient(
                    form.draft,
                    ConsentSignature(
                        signaturePng,
                        consent.signer,
                        consent.representativeName,
                        consent.representativeDocument,
                    ),
                )
            } else {
                updatePatient(patientId, form.draft).map { patientId }
            }
            result
                .onSuccess { id ->
                    updateState { it.copy(isSaving = false, isSaved = true) }
                    emitEffect(PatientFormEffect.Saved(id))
                }
                .onFailure { error -> updateState { it.copy(isSaving = false, errorMessage = error.toMessage()) } }
        }
    }
}
