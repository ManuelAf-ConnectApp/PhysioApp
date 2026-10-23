package com.connectapp.presentation.patients

import com.connectapp.commonresources.document_type_nif_nie
import com.connectapp.commonresources.document_type_other
import com.connectapp.commonresources.document_type_passport
import com.connectapp.commonresources.error_document_already_registered
import com.connectapp.commonresources.error_unexpected
import com.connectapp.commonresources.patient_not_found
import com.connectapp.commonresources.professional_has_records_notice
import com.connectapp.domain.patients.PatientError
import com.connectapp.domain.shared.validation.IdentityDocumentType
import org.jetbrains.compose.resources.StringResource

internal val IdentityDocumentType.label: StringResource
    get() = when (this) {
        IdentityDocumentType.NIF_NIE -> document_type_nif_nie
        IdentityDocumentType.PASSPORT -> document_type_passport
        IdentityDocumentType.OTHER -> document_type_other
    }

internal fun Throwable.toMessage(): StringResource = when (this) {
    PatientError.DocumentAlreadyRegistered -> error_document_already_registered
    PatientError.NotFound -> patient_not_found
    // Same wording as for professionals: "Tiene informes o facturas: se puede archivar, pero no eliminar."
    PatientError.HasClinicalRecords -> professional_has_records_notice
    else -> error_unexpected
}
