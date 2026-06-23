package com.connectapp.data.datasource

import com.connectapp.domain.model.Specialty

interface SpecialtyDataSource {
    suspend fun getSpecialties(): List<Specialty>
}