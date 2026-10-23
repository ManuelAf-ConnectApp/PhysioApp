package com.connectapp.domain.patients

/** Who signed the consent (D13): the patient or their legal representative. */
enum class SignatureSigner { PATIENT, REPRESENTATIVE }

/**
 * Consent to the processing of personal and health data, signed at the patient's creation (D13,
 * confirmed 2026-10-20). [CURRENT_VERSION] identifies the text shown on screen and is stored with
 * every signature; change it whenever that text changes.
 *
 * The current text is a provisional draft PENDING LEGAL REVIEW: the clinic or its data protection
 * officer must replace it before PhysioApp is used with real patients.
 */
object Consent {
    const val CURRENT_VERSION = "2026-10-20-provisional"
}

/** A new signature: the PNG drawn on the pad plus who signed it. */
class ConsentSignature(
    val pngBytes: ByteArray,
    val signer: SignatureSigner,
    /** Required when [signer] is [SignatureSigner.REPRESENTATIVE]. */
    val representativeName: String = "",
    val representativeDocument: String = "",
    val consentVersion: String = Consent.CURRENT_VERSION,
) {
    override fun toString(): String = "ConsentSignature(signer=$signer, consentVersion=$consentVersion)"
}

/** What the record shows about its latest signature; the image is loaded separately. */
data class SignatureInfo(
    val signer: SignatureSigner,
    val representativeName: String?,
    val representativeDocument: String?,
    val consentVersion: String,
    val capturedAtEpochMillis: Long,
    /** Earlier signatures are kept (D13): this counts all of them, the latest included. */
    val totalSignatures: Int,
) {
    override fun toString(): String = "SignatureInfo(signer=$signer, totalSignatures=$totalSignatures)"
}
