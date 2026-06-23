package com.connectapp.presentation.edit_patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.Patient
import com.connectapp.domain.usecase.GetPatientByDNIUseCase
import com.connectapp.domain.usecase.UpdatePatientUseCase
import com.connectapp.domain.validator.FormValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditPatientViewModel(
    private val getPatientByDNIUseCase: GetPatientByDNIUseCase,
    private val updatePatientUseCase: UpdatePatientUseCase,
    private val formValidator: FormValidator
) : ViewModel() {

    private val _state = MutableStateFlow(EditPatientState())
    val state: StateFlow<EditPatientState> get() = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EditPatientEffect>()
    val effect: SharedFlow<EditPatientEffect> get() = _effect.asSharedFlow()

    fun onIntent(intent: EditPatientIntent) {
        when (intent) {
            is EditPatientIntent.LoadPatient -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    try {
                        val patient = getPatientByDNIUseCase(intent.dni)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                patient = patient,
                                name = patient.firstName,
                                surname = patient.lastName,
                                email = patient.email,
                                phone = patient.phone,
                                dni = patient.dni,
                                birthDate = patient.birthDate,
                                notes = patient.additionalInfo
                            )
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(isLoading = false) }
                        emitEffect(EditPatientEffect.ShowError("Error loading patient"))
                    }
                }
            }
            is EditPatientIntent.NameChanged -> _state.update { it.copy(name = intent.name, nameError = null) }
            is EditPatientIntent.SurnameChanged -> _state.update { it.copy(surname = intent.surname, surnameError = null) }
            is EditPatientIntent.EmailChanged -> _state.update { it.copy(email = intent.email, emailError = null) }
            is EditPatientIntent.PhoneChanged -> _state.update { it.copy(phone = intent.phone, phoneError = null) }
            is EditPatientIntent.DniChanged -> _state.update { it.copy(dni = intent.dni, dniError = null) }
            is EditPatientIntent.BirthDateChanged -> _state.update { it.copy(birthDate = intent.birthDate, birthDateError = null) }
            is EditPatientIntent.NotesChanged -> _state.update { it.copy(notes = intent.notes, notesError = null) }
            EditPatientIntent.SaveClicked -> updatePatient()
            EditPatientIntent.BackClicked -> emitEffect(EditPatientEffect.NavigateBack)
        }
    }

    private fun validate(): Boolean {
        val currentState = _state.value

        val validationResult = formValidator.validatePatient(
            name = currentState.name,
            surname = currentState.surname,
            email = currentState.email,
            phone = currentState.phone,
            dni = currentState.dni,
            birthDate = currentState.birthDate
        )

        _state.update {
            it.copy(
                nameError = validationResult.nameError,
                surnameError = validationResult.surnameError,
                dniError = validationResult.dniError,
                emailError = validationResult.emailError,
                birthDateError = validationResult.birthDateError,
                phoneError = validationResult.phoneError,
            )
        }

        return validationResult.isValid
    }

    private fun updatePatient() {
        if (!validate()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val patient = Patient(
                    firstName = _state.value.name,
                    lastName = _state.value.surname,
                    email = _state.value.email,
                    phone = _state.value.phone,
                    dni = _state.value.dni,
                    birthDate = _state.value.birthDate,
                    additionalInfo = _state.value.notes
                )
                val updated = updatePatientUseCase(patient)
                if (!updated) {
                    _state.update { it.copy(errorMessage = "Error updating patient") }
                    emitEffect(EditPatientEffect.ShowError("Error updating patient"))
                } else {
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                    emitEffect(EditPatientEffect.NavigateBack)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message ?: "Unknown error") }
            }
        }
    }

    private fun emitEffect(effect: EditPatientEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
