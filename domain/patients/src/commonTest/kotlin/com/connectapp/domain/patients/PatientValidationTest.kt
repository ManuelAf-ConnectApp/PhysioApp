package com.connectapp.domain.patients

import com.connectapp.domain.shared.validation.IdentityDocumentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal val validPatient = PatientDraft(documentNumber = "X1234567L", firstName = "Íñigo", lastName = "López")
internal val patientSigns = ConsentSignature(pngBytes = byteArrayOf(1, 2, 3), signer = SignatureSigner.PATIENT)

class PatientValidationTest {

    private val repository = RecordingRepository()

    @Test
    fun `document and names are required and checked by document type`() {
        assertTrue(PatientValidation.errors(validPatient).isEmpty())
        assertEquals(
            setOf(PatientField.DOCUMENT_NUMBER, PatientField.FIRST_NAME, PatientField.LAST_NAME),
            PatientValidation.errors(PatientDraft()),
        )
        // A minor without DNI: another document, e.g. the guardian's.
        assertTrue(
            PatientValidation.errors(validPatient.copy(documentType = IdentityDocumentType.OTHER, documentNumber = "DNI tutor 1"))
                .isEmpty(),
        )
        assertEquals(
            setOf(PatientField.DOCUMENT_NUMBER),
            PatientValidation.errors(validPatient.copy(documentType = IdentityDocumentType.PASSPORT, documentNumber = "A-1")),
        )
    }

    @Test
    fun `optional email and phone are checked only when given`() {
        assertEquals(
            setOf(PatientField.EMAIL, PatientField.PHONE),
            PatientValidation.errors(validPatient.copy(email = "pepe@", phone = "12")),
        )
    }

    // D13: the signature is mandatory and a representative must be identified.
    @Test
    fun `a signature needs a drawing and an identified representative`() {
        assertTrue(PatientValidation.signatureErrors(patientSigns).isEmpty())
        assertEquals(
            setOf(PatientField.SIGNATURE),
            PatientValidation.signatureErrors(ConsentSignature(ByteArray(0), SignatureSigner.PATIENT)),
        )
        assertEquals(
            setOf(PatientField.REPRESENTATIVE_NAME, PatientField.REPRESENTATIVE_DOCUMENT),
            PatientValidation.signatureErrors(ConsentSignature(byteArrayOf(1), SignatureSigner.REPRESENTATIVE)),
        )
    }

    @Test
    fun `creating without a valid signature never reaches the repository`() = runTest {
        val result = CreatePatientUseCase(repository)(validPatient, ConsentSignature(ByteArray(0), SignatureSigner.PATIENT))

        assertEquals(PatientError.InvalidInput, result.exceptionOrNull())
        assertNull(repository.lastDraft)
    }

    @Test
    fun `a patient signature drops representative fields and data is trimmed`() = runTest {
        CreatePatientUseCase(repository)(
            validPatient.copy(firstName = " Íñigo "),
            ConsentSignature(byteArrayOf(1), SignatureSigner.PATIENT, representativeName = "ignored"),
        )

        assertEquals("Íñigo", repository.lastDraft?.firstName)
        assertEquals("", repository.lastSignature?.representativeName)
        assertEquals(Consent.CURRENT_VERSION, repository.lastSignature?.consentVersion)
    }

    private class RecordingRepository : PatientRepository {
        var lastDraft: PatientDraft? = null
        var lastSignature: ConsentSignature? = null

        override suspend fun create(draft: PatientDraft, signature: ConsentSignature): Result<String> {
            lastDraft = draft
            lastSignature = signature
            return Result.success("pat-new")
        }

        override suspend fun update(id: String, draft: PatientDraft) = Result.success(Unit)
        override suspend fun addSignature(id: String, signature: ConsentSignature) = Result.success(Unit)
        override fun observe(id: String): Flow<Patient?> = emptyFlow()
        override fun observeLatestSignatureImage(id: String): Flow<ByteArray?> = emptyFlow()
        override fun search(
            criterion: PatientSearchCriterion,
            query: String,
            includeArchived: Boolean,
        ): Flow<List<PatientSummary>> = emptyFlow()
        override suspend fun setArchived(id: String, archived: Boolean) = Result.success(Unit)
        override suspend fun delete(id: String) = Result.success(Unit)
    }
}
