package com.connectapp.domain.usecase

import com.connectapp.domain.model.Account
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.domain.model.SessionStatus
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.policy.SessionPolicy
import com.connectapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/** Where an account goes after signing in: the mandatory onboarding (D3) or Home. */
fun Account?.toSessionStatus(): SessionStatus = when {
    this == null -> SessionStatus.SIGNED_OUT
    !hasProfessionalProfile -> SessionStatus.NEEDS_PROFESSIONAL_PROFILE
    else -> SessionStatus.ACTIVE
}

/**
 * Ends the session when it has been idle longer than [SessionPolicy.IDLE_TIMEOUT_MILLIS] (D14).
 * Returns true when it has just expired it.
 */
class ExpireIdleSessionUseCase(
    private val repository: AuthRepository,
    private val clock: EpochClock,
) {
    operator fun invoke(): Boolean {
        val lastActivity = repository.lastActivityAt() ?: return false
        val expired = clock.nowMillis() - lastActivity > SessionPolicy.IDLE_TIMEOUT_MILLIS
        if (expired) repository.logout()
        return expired
    }
}

/**
 * Decides where the app starts (Splash): restores a remembered session unless it has been idle
 * too long, in which case it is discarded and the user must sign in again.
 */
class RestoreSessionUseCase(
    private val repository: AuthRepository,
    private val expireIdleSession: ExpireIdleSessionUseCase,
) {
    suspend operator fun invoke(): SessionStatus {
        val account = repository.restoreSession() ?: return SessionStatus.SIGNED_OUT
        return if (expireIdleSession()) SessionStatus.SIGNED_OUT else account.toSessionStatus()
    }
}

class RecordUserActivityUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke() = repository.recordActivity()
}

class ObserveCurrentAccountUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<Account?> = repository.observeCurrentAccount()
}

class ObserveCurrentProfessionalUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<ProfessionalProfile?> = repository.observeCurrentProfessional()
}

/** Whether this device has any account (a new or reinstalled device has none). */
class HasAccountsUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean = repository.hasAccounts()
}
