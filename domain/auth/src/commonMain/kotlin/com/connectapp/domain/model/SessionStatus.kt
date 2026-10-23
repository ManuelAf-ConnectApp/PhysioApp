package com.connectapp.domain.model

/** Where the app should start: Login, the mandatory professional onboarding (D3) or Home. */
enum class SessionStatus { SIGNED_OUT, NEEDS_PROFESSIONAL_PROFILE, ACTIVE }
