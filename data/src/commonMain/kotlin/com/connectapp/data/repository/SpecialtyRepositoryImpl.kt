package com.connectapp.data.repository

import com.connectapp.data.datasource.SpecialtyDataSource
import com.connectapp.domain.model.Specialty
import com.connectapp.domain.repository.SpecialtyRepository

class SpecialtyRepositoryImpl(
    private val specialtyDataSource: SpecialtyDataSource
): SpecialtyRepository {
    override suspend fun getSpecialties(): List<Specialty> {
        return specialtyDataSource.getSpecialties()
    }
}