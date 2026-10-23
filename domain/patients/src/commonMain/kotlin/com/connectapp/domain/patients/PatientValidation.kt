package com.connectapp.domain.patients

import com.connectapp.domain.shared.validation.EmailValidator
import com.connectapp.domain.shared.validation.IdentityDocumentType
import com.connectapp.domain.shared.validation.IdentityDocumentValidator
import com.connectapp.domain.shared.validation.NameValidator
import com.connectapp.domain.shared.validation.PhoneValidator

enum class PatientField {
    DOCUMENT_NUMBER,
    FIRST_NAME,
    LAST_NAME,
    EMAIL,
    PHONE,
    SIGNATURE,
    REPRESENTATIVE_NAME,
    REPRESENTATIVE_DOCUMENT,
}

/**
 * D9 (confirmed 2026-10-20): the document (by its type) and the names are required; email and
 * phone are optional but must be well formed when given. D13: the consent signature is required
 * at creation, and a legal representative must give a name and a document.
 */
object PatientValidation {
    fun errors(draft: PatientDraft): Set<PatientField> = buildSet {
        if (!IdentityDocumentValidator.isValid(draft.documentType, draft.documentNumber)) {
            add(PatientField.DOCUMENT_NUMBER)
        }
        if (!NameValidator.isValid(draft.firstName)) add(PatientField.FIRST_NAME)
        if (!NameValidator.isValid(draft.lastName)) add(PatientField.LAST_NAME)
        if (draft.email.isNotBlank() && !EmailValidator.isValid(draft.email)) add(PatientField.EMAIL)
        if (draft.phone.isNotBlank() && !PhoneValidator.isValid(draft.phone)) add(PatientField.PHONE)
    }

    fun signatureErrors(
        signer: SignatureSigner,
        hasDrawing: Boolean,
        representativeName: String,
        representativeDocument: String,
    ): Set<PatientField> = buildSet {
        if (!hasDrawing) add(PatientField.SIGNATURE)
        if (signer == SignatureSigner.REPRESENTATIVE) {
            if (!NameValidator.isValid(representativeName)) add(PatientField.REPRESENTATIVE_NAME)
            if (!IdentityDocumentValidator.isValid(IdentityDocumentType.OTHER, representativeDocument)) {
                add(PatientField.REPRESENTATIVE_DOCUMENT)
            }
        }
    }

    fun signatureErrors(signature: ConsentSignature): Set<PatientField> = signatureErrors(
        signer = signature.signer,
        hasDrawing = signature.pngBytes.isNotEmpty(),
        representativeName = signature.representativeName,
        representativeDocument = signature.representativeDocument,
    )
}
