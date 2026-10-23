package com.connectapp.presentation.professionals.search

import com.connectapp.domain.professionals.ProfessionalSearchCriterion
import com.connectapp.domain.professionals.ProfessionalSummary

data class ProfessionalSearchState(
    val criterion: ProfessionalSearchCriterion = ProfessionalSearchCriterion.FULL_NAME,
    val query: String = "",
    val includeArchived: Boolean = false,
    val results: List<ProfessionalSummary> = emptyList(),
    val isLoading: Boolean = true,
    /** Record shown in the detail pane when list and detail fit side by side. */
    val selectedId: String? = null,
) {
    override fun toString(): String =
        "ProfessionalSearchState(criterion=$criterion, results=${results.size}, includeArchived=$includeArchived)"
}

sealed interface ProfessionalSearchIntent {
    /** One criterion at a time, so changing it clears the query. */
    data class CriterionSelected(val criterion: ProfessionalSearchCriterion) : ProfessionalSearchIntent
    data class QueryChanged(val query: String) : ProfessionalSearchIntent
    data class IncludeArchivedChanged(val includeArchived: Boolean) : ProfessionalSearchIntent
    data class ResultSelected(val professionalId: String?) : ProfessionalSearchIntent
}
