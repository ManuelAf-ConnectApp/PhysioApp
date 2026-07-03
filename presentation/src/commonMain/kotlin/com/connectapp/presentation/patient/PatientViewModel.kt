package com.connectapp.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.Patient
import com.connectapp.domain.usecase.SavePatientUseCase
import com.connectapp.domain.validator.FormValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PatientViewModel(
    private val savePatientUseCase: SavePatientUseCase,
    private val formValidator: FormValidator
) : ViewModel() {

    private val _state = MutableStateFlow(PatientState.EMPTY)
    val state: StateFlow<PatientState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PatientEffect>()
    val effect: SharedFlow<PatientEffect> = _effect.asSharedFlow()

    init {
        _state.update {
            PatientState.EMPTY
        }
    }

    fun onIntent(intent: PatientIntent) {
        when (intent) {
            is PatientIntent.NameChanged -> onNameChanged(intent.name)

            is PatientIntent.SurnameChanged -> onSurnameChanged(intent.surname)

            is PatientIntent.EmailChanged -> onEmailChanged(intent.email)

            is PatientIntent.PhoneChanged -> onPhoneChanged(intent.phone)

            is PatientIntent.DniChanged -> onDniChanged(intent.dni)

            is PatientIntent.BirthDateChanged -> onBirthDateChanged(intent.birthDate)

            is PatientIntent.NotesChanged -> _state.update { it.copy(notes = intent.notes) }

            PatientIntent.SaveClicked -> savePatient()

            PatientIntent.BackClicked -> emitEffect(PatientEffect.NavigateBack)
        }
    }

    private fun onNameChanged(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameError = null
            )
        }
    }

    private fun onSurnameChanged(surname: String) {
        _state.update {
            it.copy(
                surname = surname,
                surnameError = null
            )
        }
    }

    private fun onEmailChanged(email: String) {
        _state.update {
            it.copy(
                email = email,
                emailError = null
            )
        }
    }

    private fun onPhoneChanged(phone: String) {
        _state.update {
            it.copy(
                phone = phone,
                phoneError = null
            )
        }
    }

    private fun onDniChanged(dni: String) {
        _state.update {
            it.copy(
                dni = dni,
                dniError = null
            )
        }
    }

    private fun onBirthDateChanged(birthDate: String) {
        _state.update {
            it.copy(
                birthDate = birthDate,
                birthDateError = null
            )
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

    private fun savePatient() {
        if (!validate()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val patient = Patient(
                    firstName = _state.value.name,
                    lastName = _state.value.surname,
                    email = _state.value.email,
                    phone = _state.value.phone,
                    dni = _state.value.dni.uppercase(),
                    birthDate = _state.value.birthDate,
                    additionalInfo = _state.value.notes
                )
                val inserted = savePatientUseCase(patient)
                if (!inserted) {
                    _state.update { it.copy(errorMessage = "Error saving patient") }
                    emitEffect(PatientEffect.ShowError("Error saving patient"))
                } else {
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                    emitEffect(PatientEffect.NavigateBack)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    private fun emitEffect(effect: PatientEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
