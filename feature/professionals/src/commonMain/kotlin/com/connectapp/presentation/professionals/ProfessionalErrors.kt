package com.connectapp.presentation.professionals

import com.connectapp.commonresources.error_license_already_registered
import com.connectapp.commonresources.error_nif_already_registered
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.professional_has_account_notice
import com.connectapp.commonresources.professional_has_records_notice
import com.connectapp.commonresources.professional_not_found
import com.connectapp.domain.professionals.ProfessionalError
import org.jetbrains.compose.resources.StringResource

internal fun Throwable.toMessage(): StringResource = when (this) {
    ProfessionalError.NifAlreadyRegistered -> error_nif_already_registered
    ProfessionalError.LicenseNumberAlreadyRegistered -> error_license_already_registered
    ProfessionalError.NotFound -> professional_not_found
    ProfessionalError.HasAccount -> professional_has_account_notice
    ProfessionalError.HasClinicalRecords -> professional_has_records_notice
    else -> error_unexpected
}
