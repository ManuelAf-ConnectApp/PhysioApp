package com.connectapp.presentation.search_professional

import com.connectapp.domain.model.Professional

sealed interface SearchProfessionalEffect {
    data class NavigateToDetail(val professional: Professional) : SearchProfessionalEffect
}
