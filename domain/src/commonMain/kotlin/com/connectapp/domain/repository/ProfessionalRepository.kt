package com.connectapp.domain.repository

import com.connectapp.domain.model.Professional

interface ProfessionalRepository {

    fun getProfessionalByEmail(email: String): Professional?
    fun getProfessionalByPhone(phone: String): Professional?

    fun getProfessionalByCollegiateNumber(collegiateNumber: String): Professional?

    suspend fun insertProfessional(professional: Professional): Result<Boolean>

    fun getProfessionalById(id: Long): Professional

    suspend fun updateProfessional(professional: Professional): Boolean

    suspend fun deleteProfessional(id: Long)

    fun getAllProfessionals(): List<Professional>
}
