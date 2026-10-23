package com.connectapp.presentation.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.component.PhysioLogo
import com.connectapp.commonresources.component.PhysioWordmark
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val viewModel: SplashViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SplashEffect.NavigateToLogin -> onNavigateToLogin()
                SplashEffect.NavigateToOnboarding -> onNavigateToOnboarding()
                SplashEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.7f,
        animationSpec = tween(durationMillis = 500)
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.scale(scale).alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            PhysioLogo(modifier = Modifier.size(160.dp))
            PhysioWordmark(
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.semantics { heading() },
            )
        }
    }
}
