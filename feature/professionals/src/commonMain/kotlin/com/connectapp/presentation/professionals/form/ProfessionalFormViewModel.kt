package com.connectapp.presentation.professionals.form

import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.professional_not_found
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.professionals.CreateProfessionalUseCase
import com.connectapp.domain.professionals.ObserveProfessionalUseCase
import com.connectapp.domain.professionals.UpdateProfessionalUseCase
import com.connectapp.domain.professionals.toDraft
import com.connectapp.presentation.professionals.toMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** "Registrar profesional" and, with a [professionalId], its edit mode. */
class ProfessionalFormViewModel(
    private val professionalId: String?,
    private val createProfessional: CreateProfessionalUseCase,
    private val updateProfessional: UpdateProfessionalUseCase,
    observeProfessional: ObserveProfessionalUseCase,
) : MviViewModel<ProfessionalFormState, ProfessionalFormIntent, ProfessionalFormEffect>(
    ProfessionalFormState(isEditing = professionalId != null, isLoading = professionalId != null),
) {

    init {
        if (professionalId != null) {
            viewModelScope.launch {
                val professional = observeProfessional(professionalId).first()
                updateState {
                    if (professional == null) {
                        it.copy(isLoading = false, errorMessage = professional_not_found)
                    } else {
                        it.copy(isLoading = false, draft = professional.toDraft(), savedDraft = professional.toDraft())
                    }
                }
            }
        }
    }

    override fun reduce(intent: ProfessionalFormIntent) {
        when (intent) {
            is ProfessionalFormIntent.DraftChanged -> updateState { it.copy(draft = intent.draft, errorMessage = null) }
            ProfessionalFormIntent.SaveClicked -> save()
        }
    }

    private fun save() {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (professionalId == null) {
                createProfessional(form.draft)
            } else {
                updateProfessional(professionalId, form.draft).map { professionalId }
            }
            result
                .onSuccess { id ->
                    updateState { it.copy(isSaving = false, savedDraft = form.draft) }
                    emitEffect(ProfessionalFormEffect.Saved(id))
                }
                .onFailure { error -> updateState { it.copy(isSaving = false, errorMessage = error.toMessage()) } }
        }
    }
}
