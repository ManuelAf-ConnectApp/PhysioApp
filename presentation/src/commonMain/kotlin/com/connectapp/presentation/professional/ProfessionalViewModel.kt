package com.connectapp.presentation.professional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.Professional
import com.connectapp.domain.usecase.GetSpecialtyListUseCase
import com.connectapp.domain.usecase.SaveProfessionalUseCase
import com.connectapp.domain.validator.FormValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfessionalViewModel(
    private val saveProfessionalUseCase: SaveProfessionalUseCase,
    private val getSpecialtyListUseCase: GetSpecialtyListUseCase,
    private val formValidator: FormValidator
) : ViewModel() {

    private val _state = MutableStateFlow(ProfessionalState())
    val state: StateFlow<ProfessionalState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ProfessionalEffect>()
    val effect: SharedFlow<ProfessionalEffect> = _effect.asSharedFlow()


    init {
        viewModelScope.launch {
            val specialties = getSpecialtyListUseCase()
            _state.update { it.copy(specialties = specialties) }
        }
    }

    fun onIntent(intent: ProfessionalIntent) {
        when (intent) {
            is ProfessionalIntent.NameChanged -> onNameChanged(intent.name)
            is ProfessionalIntent.SurnameChanged -> onSurnameChanged(intent.surname)
            is ProfessionalIntent.SpecialtyChanged -> onSpecialtyChanged(intent.specialty)
            is ProfessionalIntent.CollegiateNumberChanged -> onCollegiateNumberChanged(intent.collegiateNumber)

            is ProfessionalIntent.EmailChanged -> onEmailChanged(intent.email)
            is ProfessionalIntent.PhoneChanged -> onPhoneChanged(intent.phone)
            ProfessionalIntent.SaveClicked -> saveProfessional()
            ProfessionalIntent.BackClicked -> emitEffect(ProfessionalEffect.NavigateBack)
            is ProfessionalIntent.NotesChanged -> onNotesChanged(intent.notes)
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

    private fun onSpecialtyChanged(specialty: String) {
        _state.update {
            it.copy(
                specialty = specialty,
                specialtyError = null
            )
        }
    }

    private fun onCollegiateNumberChanged(collegiateNumber: String) {
        _state.update {
            it.copy(
                collegiateNumber = collegiateNumber,
                collegiateNumberError = null
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

    private fun onNotesChanged(notes: String) {
        _state.update {
            it.copy(
                notes = notes,
            )
        }
    }

    private fun saveProfessional() {
        val currentState = state.value
        val validationResult = formValidator.validateProfessional(
            name = currentState.name,
            surname = currentState.surname,
            email = currentState.email,
            phone = currentState.phone,
            specialty = currentState.specialty,
            collegiateNumber = currentState.collegiateNumber
        )

        if (!validationResult.isValid) {
            _state.update {
                it.copy(
                    nameError = validationResult.nameError,
                    surnameError = validationResult.surnameError,
                    emailError = validationResult.emailError,
                    phoneError = validationResult.phoneError,
                    specialtyError = validationResult.specialtyError,
                    collegiateNumberError = validationResult.collegiateNumberError
                )
            }
            return
        }

        viewModelScope.launch {
            val result = saveProfessionalUseCase(
                Professional(
                    firstName = currentState.name,
                    lastName = currentState.surname,
                    specialty = currentState.specialty,
                    collegiateNumber = currentState.collegiateNumber,
                    email = currentState.email,
                    phone = currentState.phone,
                    additionalInfo = currentState.notes
                )
            )
            if (result.isSuccess) {
                clearForm()
                emitEffect(ProfessionalEffect.SaveSuccess)
            } else {
                emitEffect(
                    ProfessionalEffect.SaveFailure(
                        result.exceptionOrNull()?.message ?: "Unknown error"
                    )
                )
            }
        }
    }

    private fun clearForm(){
        _state.update {
            it.copy(
                name = "",
                nameError = null,
                surname = "",
                surnameError = null,
                specialty = "",
                specialtyError = null,
                collegiateNumber = "",
                collegiateNumberError = null,
                email = "",
                emailError = null,
                phone = "",
                phoneError = null,
                notes = ""
            )
        }
    }

    private fun emitEffect(effect: ProfessionalEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
