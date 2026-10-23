package com.connectapp.domain.usecase

import com.connectapp.domain.fake.FakeAuthRepository
import com.connectapp.domain.fake.accountWithProfile
import com.connectapp.domain.fake.accountWithoutProfile
import com.connectapp.domain.model.SessionStatus
import com.connectapp.domain.policy.EpochClock
import com.connectapp.domain.policy.SessionPolicy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionUseCasesTest {

    private var now = 1_000_000L
    private val clock = EpochClock { now }
    private val repository = FakeAuthRepository()
    private val expireIdle = ExpireIdleSessionUseCase(repository, clock)
    private val restore = RestoreSessionUseCase(repository, expireIdle)

    @Test
    fun `nothing expires when signed out`() {
        assertFalse(expireIdle())
        assertEquals(0, repository.logoutCalls)
    }

    @Test
    fun `a session idle for exactly the timeout is kept`() {
        repository.lastActivity = now - SessionPolicy.IDLE_TIMEOUT_MILLIS

        assertFalse(expireIdle())
        assertEquals(0, repository.logoutCalls)
    }

    // "cuando la app permanece inactiva más del tiempo configurado, se exige volver a autenticarse".
    @Test
    fun `a session idle longer than the timeout is logged out`() {
        repository.lastActivity = now - SessionPolicy.IDLE_TIMEOUT_MILLIS - 1

        assertTrue(expireIdle())
        assertEquals(1, repository.logoutCalls)
    }

    @Test
    fun `startup without a remembered session goes to login`() = runTest {
        assertEquals(SessionStatus.SIGNED_OUT, restore())
    }

    @Test
    fun `a recent remembered session goes to home or to the onboarding`() = runTest {
        repository.lastActivity = now - 1_000
        repository.restoredAccount = accountWithProfile
        assertEquals(SessionStatus.ACTIVE, restore())

        repository.restoredAccount = accountWithoutProfile
        assertEquals(SessionStatus.NEEDS_PROFESSIONAL_PROFILE, restore())
    }

    @Test
    fun `a remembered session idle for too long is discarded`() = runTest {
        repository.restoredAccount = accountWithProfile
        repository.lastActivity = now - SessionPolicy.IDLE_TIMEOUT_MILLIS - 1

        assertEquals(SessionStatus.SIGNED_OUT, restore())
        assertEquals(1, repository.logoutCalls)
    }

    @Test
    fun `session status follows the account's professional profile`() {
        assertEquals(SessionStatus.SIGNED_OUT, null.toSessionStatus())
        assertEquals(SessionStatus.NEEDS_PROFESSIONAL_PROFILE, accountWithoutProfile.toSessionStatus())
        assertEquals(SessionStatus.ACTIVE, accountWithProfile.toSessionStatus())
    }

    @Test
    fun `logout and activity are forwarded to the repository`() {
        RecordUserActivityUseCase(repository)()
        LogoutUseCase(repository)()

        assertEquals(1, repository.recordActivityCalls)
        assertEquals(1, repository.logoutCalls)
    }

    @Test
    fun `personal data is masked in toString`() {
        assertFalse(accountWithProfile.toString().contains("aina"))
        assertFalse(accountWithProfile.toString().contains("García"))
    }
}
