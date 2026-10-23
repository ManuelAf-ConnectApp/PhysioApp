package com.connectapp.presentation.patients.consent

import com.connectapp.commonresources.signature.SignatureStroke
import com.connectapp.domain.patients.PatientField
import com.connectapp.domain.patients.PatientValidation
import com.connectapp.domain.patients.SignatureSigner

/** What is being filled in to sign the consent (D13). */
data class ConsentFormState(
    val signer: SignatureSigner = SignatureSigner.PATIENT,
    val representativeName: String = "",
    val representativeDocument: String = "",
    val strokes: List<SignatureStroke> = emptyList(),
) {
    val hasDrawing: Boolean get() = strokes.any { it.isNotEmpty() }

    val invalidFields: Set<PatientField>
        get() = PatientValidation.signatureErrors(signer, hasDrawing, representativeName, representativeDocument)

    val isEmpty: Boolean get() = !hasDrawing && representativeName.isBlank() && representativeDocument.isBlank()

    override fun toString(): String = "ConsentFormState(signer=$signer, hasDrawing=$hasDrawing)"
}
