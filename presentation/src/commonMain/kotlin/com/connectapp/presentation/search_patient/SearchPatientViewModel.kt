package com.connectapp.presentation.search_patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.Patient
import com.connectapp.domain.usecase.GetPatientByDNIUseCase
import com.connectapp.domain.usecase.GetPatientByEmailUseCase
import com.connectapp.domain.usecase.GetPatientByPhoneUseCase
import com.connectapp.domain.validator.FormValidator
import com.connectapp.domain.validator.model.ValidationError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.emptyList


class SearchPatientViewModel(
    private val getPatientByEmailUseCase: GetPatientByEmailUseCase,
    private val getPatientByPhoneUseCase: GetPatientByPhoneUseCase,
    private val getPatientByDNIUseCase: GetPatientByDNIUseCase,
    private val formValidator: FormValidator
) : ViewModel() {

    private val _state: MutableStateFlow<SearchPatientState> =
        MutableStateFlow(SearchPatientState())
    val state: StateFlow<SearchPatientState> get() = _state.asStateFlow()

    private val _intents: MutableSharedFlow<SearchPatientIntent> = MutableSharedFlow()
    val intents: SharedFlow<SearchPatientIntent> get() = _intents.asSharedFlow()

    init {
        initState()
        handleIntents()
    }

    private fun initState() {
        _state.update { SearchPatientState.DATA_EMPTY }
    }


    fun onIntent(intent: SearchPatientIntent) {
        viewModelScope.launch {
            _intents.emit(intent)
        }
    }

    private fun handleIntents() {
        viewModelScope.launch {
            intents.collect { intent ->
                when (intent) {
                    is SearchPatientIntent.EmailChanged -> {
                        _state.update {
                            it.copy(
                                email = intent.email,
                                phone = "",
                                dni = "",
                                emailError = formValidator.validateEmail(intent.email),
                                phoneError = null,
                                dniError = null
                            )
                        }
                    }

                    is SearchPatientIntent.PhoneChanged -> {
                        _state.update {
                            it.copy(
                                phone = intent.phone,
                                email = "",
                                dni = "",
                                phoneError = formValidator.validatePhone(intent.phone),
                                emailError = null,
                                dniError = null
                            )
                        }
                    }

                    is SearchPatientIntent.DNIChanged -> {
                        val uppercaseDNI = intent.dni.uppercase()
                        _state.update {
                            it.copy(
                                dni = uppercaseDNI,
                                email = "",
                                phone = "",
                                dniError = formValidator.validateDni(uppercaseDNI),
                                emailError = null,
                                phoneError = null
                            )
                        }
                    }

                    SearchPatientIntent.SearchClicked -> {
                        searchPatients()
                    }

                    is SearchPatientIntent.PatientSelected -> {
                        _state.update {
                            val newSelectedPatient =
                                if (it.selectedPatient == intent.patient) null else intent.patient
                            it.copy(selectedPatient = newSelectedPatient)
                        }
                    }

                    SearchPatientIntent.ClearSearch -> {
                        _state.update { SearchPatientState.DATA_EMPTY }
                    }
                }
            }
        }
    }

    private fun searchPatients() {
        val currentState = _state.value
        val isEmailError = currentState.email.isNotEmpty() && currentState.emailError != null && currentState.emailError != ValidationError.FIELD_EMPTY
        val isPhoneError = currentState.phone.isNotEmpty() && currentState.phoneError != null && currentState.phoneError != ValidationError.FIELD_EMPTY
        val isDniError = currentState.dni.isNotEmpty() && currentState.dniError != null && currentState.dniError != ValidationError.FIELD_EMPTY

        if (isEmailError || isPhoneError || isDniError) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    results = emptyList(),
                    selectedPatient = null
                )
            }
            try {
                val results = getPatientListInBaseOnSearchCriteria()
                if (results.isEmpty()) {
                    _state.update { it.copy(isLoading = false, error = "No se encontraron resultados") }
                } else {
                    _state.update { it.copy(isLoading = false, results = results) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "No se encontraron resultados") }
            }
        }
    }

    private fun getPatientListInBaseOnSearchCriteria(): List<Patient> {

        val list = mutableListOf<Patient>()
        when {
            state.value.email.isNotEmpty() -> {
                list.add(getPatientByEmailUseCase(state.value.email))
            }

            state.value.phone.isNotEmpty() -> {
                list.add(getPatientByPhoneUseCase(state.value.phone))
            }

            state.value.dni.isNotEmpty() -> {
                list.add(getPatientByDNIUseCase(dni = state.value.formattedDNI))
            }

            else -> {
                emptyList<Patient>()
            }
        }

        return list
    }
}
