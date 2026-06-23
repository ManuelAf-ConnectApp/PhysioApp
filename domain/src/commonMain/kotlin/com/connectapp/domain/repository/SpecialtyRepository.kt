package com.connectapp.domain.repository

import com.connectapp.domain.model.Specialty

interface SpecialtyRepository {
    suspend fun getSpecialties(): List<Specialty>

}