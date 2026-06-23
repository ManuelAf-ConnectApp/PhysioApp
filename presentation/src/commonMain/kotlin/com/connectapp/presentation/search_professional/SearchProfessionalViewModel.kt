package com.connectapp.presentation.search_professional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.model.Professional
import com.connectapp.domain.model.Specialty
import com.connectapp.domain.usecase.GetAllProfessionalsUseCase
import com.connectapp.domain.usecase.GetProfessionalByCollegiateNumberUseCase
import com.connectapp.domain.usecase.GetProfessionalByEmailUseCase
import com.connectapp.domain.usecase.GetProfessionalByPhoneUseCase
import com.connectapp.domain.usecase.GetSpecialtyListUseCase
import com.connectapp.domain.validator.FormValidator
import com.connectapp.domain.validator.model.ValidationError
import com.connectapp.presentation.mapper.toUi
import com.connectapp.presentation.model.ProfessionalUi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchProfessionalViewModel(
    private val getSearchProfessionalByCollegiateNumberUseCase: GetProfessionalByCollegiateNumberUseCase,
    private val getSearchProfessionalByEmailUseCase: GetProfessionalByEmailUseCase,
    private val getSearchProfessionalByPhoneUseCase: GetProfessionalByPhoneUseCase,
    private val getAllProfessionalsUseCase: GetAllProfessionalsUseCase,
    private val getSpecialtyListUseCase: GetSpecialtyListUseCase,
    private val formValidator: FormValidator
) : ViewModel() {
    private val _state: MutableStateFlow<SearchProfessionalState> =
        MutableStateFlow(SearchProfessionalState())
    val state: StateFlow<SearchProfessionalState> get() = _state.asStateFlow()

    private val _intents: MutableSharedFlow<SearchProfessionalIntent> = MutableSharedFlow()
    val intents: SharedFlow<SearchProfessionalIntent> get() = _intents.asSharedFlow()

    init {
        initState()
        handleIntents()
    }

    private fun initState() {
        _state.update { SearchProfessionalState.DATA_EMPTY }
    }

    private fun handleIntents() {
        viewModelScope.launch {
            intents.collect { intent ->
                when (intent) {
                    is SearchProfessionalIntent.EmailChanged -> {
                        _state.update {
                            it.copy(
                                email = intent.email,
                                phone = "",
                                collegiateNumber = "",
                                emailError = formValidator.validateEmail(intent.email),
                                phoneError = null,
                                collegiateNumberError = null
                            )
                        }
                    }

                    is SearchProfessionalIntent.PhoneChanged -> {
                        _state.update {
                            it.copy(
                                phone = intent.phone,
                                email = "",
                                collegiateNumber = "",
                                phoneError = formValidator.validatePhone(intent.phone),
                                emailError = null,
                                collegiateNumberError = null
                            )
                        }
                    }

                    is SearchProfessionalIntent.CollegiateNumberChanged -> {
                        _state.update {
                            it.copy(
                                collegiateNumber = intent.collegiateNumber,
                                email = "",
                                phone = "",
                                collegiateNumberError = if (intent.collegiateNumber.isBlank()) ValidationError.FIELD_EMPTY else null,
                                emailError = null,
                                phoneError = null
                            )
                        }
                    }

                    SearchProfessionalIntent.SearchClicked -> {
                        searchProfessionals()
                    }

                    is SearchProfessionalIntent.ProfessionalSelected -> {
                        _state.update {
                            val newSelected =
                                if (it.selectedProfessional == intent.professional) null else intent.professional
                            it.copy(selectedProfessional = newSelected)
                        }
                    }

                    SearchProfessionalIntent.CheckAllProfessionalsClicked -> {
                        _state.update {
                            it.copy(
                                isCheckAllProfessionals = !it.isCheckAllProfessionals,
                            )
                        }
                    }

                    SearchProfessionalIntent.ClearSearch -> {
                        _state.update { SearchProfessionalState.DATA_EMPTY }
                    }
                }
            }
        }
    }

    private fun searchProfessionals() {
        val currentState = _state.value
        val isEmailError =
            currentState.email.isNotEmpty() && currentState.emailError != null && currentState.emailError != ValidationError.FIELD_EMPTY
        val isPhoneError =
            currentState.phone.isNotEmpty() && currentState.phoneError != null && currentState.phoneError != ValidationError.FIELD_EMPTY
        val isCollegiateError =
            currentState.collegiateNumber.isNotEmpty() && currentState.collegiateNumberError != null && currentState.collegiateNumberError != ValidationError.FIELD_EMPTY

        if (!currentState.isCheckAllProfessionals && (isEmailError || isPhoneError || isCollegiateError)) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    results = emptyList(),
                    selectedProfessional = null
                )
            }
            try {
                val results =
                    getProfessionalListInBaseOnSearchCriteria(specialties = getSpecialtyListUseCase())

                if (results.isEmpty()) {
                    _state.update { it.copy(isLoading = false, error = "No results found") }
                } else {
                    _state.update { it.copy(isLoading = false, results = results) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Unknown error: ${e.message}") }
            }
        }
    }

    fun onIntent(intent: SearchProfessionalIntent) {
        viewModelScope.launch {
            _intents.emit(intent)
        }
    }

    private fun getProfessionalListInBaseOnSearchCriteria(specialties: List<Specialty>): List<ProfessionalUi> {


        val list = mutableListOf<ProfessionalUi>()
        when {
            state.value.email.isNotEmpty() -> {
                getSearchProfessionalByEmailUseCase(state.value.email)?.let {
                    list.add(it.toUi(list = specialties))
                }
            }

            state.value.phone.isNotEmpty() -> {
                getSearchProfessionalByPhoneUseCase(state.value.phone)?.let {
                    list.add(it.toUi(list = specialties))
                }
            }

            state.value.collegiateNumber.isNotEmpty() -> {
                getSearchProfessionalByCollegiateNumberUseCase(state.value.collegiateNumber)?.let {
                    list.add(it.toUi(list = specialties))
                }
            }

            state.value.isCheckAllProfessionals -> {

                list.addAll(getAllProfessionalsUseCase().map { it.toUi(list = specialties) })
            }

            else -> {
                emptyList<Professional>()
            }
        }

        return list
    }
}
