package com.connectapp.data.repository

import com.connectapp.data.database.AppDatabase
import com.connectapp.data.mapper.toDomain
import com.connectapp.domain.model.Professional
import com.connectapp.domain.repository.ProfessionalRepository

class ProfessionalRepositoryImpl(
    appDatabase: AppDatabase
) : ProfessionalRepository {
    private val professionalQueries = appDatabase.appDatabaseQueries


    override fun getProfessionalByEmail(email: String): Professional? {
        return professionalQueries.getProfessionalByEmail(email).executeAsOneOrNull()?.toDomain()
    }

    override fun getProfessionalByPhone(phone: String): Professional? {
        return professionalQueries.getProfessionalByPhone(phone).executeAsOneOrNull()?.toDomain()
    }

    override fun getProfessionalByCollegiateNumber(collegiateNumber: String): Professional? {
        return professionalQueries.getProfessionalByCollegiateNumber(collegiateNumber)
            .executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insertProfessional(professional: Professional): Result<Boolean> {
        val insertProfessional = professionalQueries.insertProfessional(
            firstName = professional.firstName,
            lastName = professional.lastName,
            email = professional.email,
            collegiateNumber = professional.collegiateNumber,
            phone = professional.phone,
            specialty = professional.specialty,
            additionalInfo = professional.additionalInfo
        )
        return Result.success(insertProfessional.value > 0)
    }

    override fun getProfessionalById(id: Long): Professional {
        return professionalQueries.getProfessionalById(id).executeAsOne().toDomain()
    }

    override suspend fun updateProfessional(professional: Professional): Boolean {
        val updateProfessional = professionalQueries.updateProfessional(
            firstName = professional.firstName,
            lastName = professional.lastName,
            email = professional.email,
            collegiateNumber = professional.collegiateNumber,
            phone = professional.phone,
            specialty = professional.specialty,
            additionalInfo = professional.additionalInfo,
            id = professional.id
        )
        return updateProfessional.value > 0
    }

    override suspend fun deleteProfessional(id: Long) {
        professionalQueries.deleteProfessional(id)
    }

    override fun getAllProfessionals(): List<Professional> {
        return professionalQueries.selectAllProfessionals().executeAsList().map { it.toDomain() }
    }
}
