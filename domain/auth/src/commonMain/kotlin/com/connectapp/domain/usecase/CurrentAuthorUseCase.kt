package com.connectapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Id of the signed-in professional: the author of new reports and receipts, and the only one who
 * may change them (D5, D17, D6).
 */
class CurrentAuthorUseCase(private val observeCurrentProfessional: ObserveCurrentProfessionalUseCase) {
    suspend operator fun invoke(): String? = observeCurrentProfessional().first()?.id

    fun observe(): Flow<String?> = observeCurrentProfessional().map { it?.id }
}
