package com.connectapp.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.component.LoadingContentPlaceholder
import com.connectapp.commonresources.error_user_not_found
import com.connectapp.commonresources.label_college
import com.connectapp.commonresources.label_email
import com.connectapp.commonresources.label_license_number
import com.connectapp.commonresources.label_name
import com.connectapp.commonresources.label_nif
import com.connectapp.commonresources.logout
import com.connectapp.commonresources.profile_section_account
import com.connectapp.commonresources.profile_section_professional
import com.connectapp.domain.model.Account
import com.connectapp.domain.model.ProfessionalProfile
import com.connectapp.presentation.profile.component.ProfileInfoItem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateBack -> onNavigateBack()
                ProfileEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val account = state.account
        when {
            state.isLoading -> LoadingContentPlaceholder()
            account == null -> Text(stringResource(error_user_not_found), modifier = Modifier.align(Alignment.Center))
            else -> ProfileContent(
                account = account,
                professional = state.professional,
                onLogout = { viewModel.onIntent(ProfileIntent.LogoutClicked) },
            )
        }
    }
}

@Composable
private fun ProfileContent(account: Account, professional: ProfessionalProfile?, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(text = "${account.firstName} ${account.lastName}", style = MaterialTheme.typography.headlineSmall)

        InfoCard(title = profile_section_account) {
            ProfileInfoItem(
                icon = Icons.Default.Person,
                label = stringResource(label_name),
                value = "${account.firstName} ${account.lastName}",
            )
            ProfileInfoItem(Icons.Default.Email, stringResource(label_email), account.email)
        }

        professional?.let {
            InfoCard(title = profile_section_professional) {
                ProfileInfoItem(Icons.Default.Person, stringResource(label_name), "${it.firstName} ${it.lastName}")
                ProfileInfoItem(Icons.Default.CreditCard, stringResource(label_nif), it.nif)
                ProfileInfoItem(Icons.Default.AccountBalance, stringResource(label_college), it.college.displayName)
                ProfileInfoItem(Icons.Default.Badge, stringResource(label_license_number), it.licenseNumber)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            Text(stringResource(logout), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun InfoCard(title: StringResource, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}
