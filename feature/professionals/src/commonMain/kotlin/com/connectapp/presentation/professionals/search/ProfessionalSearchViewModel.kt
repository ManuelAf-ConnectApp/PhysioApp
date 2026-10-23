package com.connectapp.presentation.professionals.search

import androidx.lifecycle.viewModelScope
import com.connectapp.core.mvi.StateViewModel
import com.connectapp.domain.professionals.SearchProfessionalsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/** "Buscar profesional". Results update live after edits, archiving or deletion. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfessionalSearchViewModel(
    searchProfessionals: SearchProfessionalsUseCase,
) : StateViewModel<ProfessionalSearchState, ProfessionalSearchIntent>(ProfessionalSearchState()) {

    init {
        state
            .map { Triple(it.criterion, it.query, it.includeArchived) }
            .distinctUntilChanged()
            .flatMapLatest { (criterion, query, includeArchived) ->
                searchProfessionals(criterion, query, includeArchived)
            }
            .onEach { results ->
                updateState { state ->
                    state.copy(
                        results = results,
                        isLoading = false,
                        selectedId = state.selectedId?.takeIf { id -> results.any { it.id == id } },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun reduce(intent: ProfessionalSearchIntent) {
        when (intent) {
            is ProfessionalSearchIntent.CriterionSelected ->
                updateState { it.copy(criterion = intent.criterion, query = "") }
            is ProfessionalSearchIntent.QueryChanged -> updateState { it.copy(query = intent.query) }
            is ProfessionalSearchIntent.IncludeArchivedChanged ->
                updateState { it.copy(includeArchived = intent.includeArchived) }
            is ProfessionalSearchIntent.ResultSelected -> updateState { it.copy(selectedId = intent.professionalId) }
        }
    }
}
