package com.connectapp.domain.usecase

import com.connectapp.domain.model.Account
import com.connectapp.domain.model.AuthError
import com.connectapp.domain.repository.AuthRepository
import com.connectapp.domain.shared.professional.ProfessionalCollege
import com.connectapp.domain.validation.AuthValidation

/** Mandatory onboarding step after the first login (D3): creates and links the professional record. */
class CompleteProfessionalProfileUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        nif: String,
        college: ProfessionalCollege?,
        licenseNumber: String,
        firstName: String,
        lastName: String,
    ): Result<Account> {
        if (AuthValidation.professionalProfileErrors(nif, college, licenseNumber, firstName, lastName).isNotEmpty() ||
            college == null
        ) {
            return Result.failure(AuthError.InvalidInput)
        }
        return repository.completeProfessionalProfile(
            nif,
            college,
            licenseNumber.trim(),
            firstName.trim(),
            lastName.trim(),
        )
    }
}
