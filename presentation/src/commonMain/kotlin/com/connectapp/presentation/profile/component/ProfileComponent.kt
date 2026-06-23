package com.connectapp.presentation.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connectapp.designresources.DimensResources
import com.connectapp.designresources.TokensResources
import com.connectapp.domain.model.Professional
import com.connectapp.domain.model.User
import org.jetbrains.compose.resources.stringResource


@Composable
internal fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = DimensResources.spacing8),
        fontWeight = FontWeight.Bold
    )
}

@Composable
internal fun UserAvatar(user: User?) {
    val initials = if (user != null) {
        "${user.firstName.take(1)}${user.lastName.take(1)}".uppercase()
    } else {
        "?"
    }

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 36.sp
            )
        )
    }
}

@Composable
internal fun UserInfoSection(user: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(DimensResources.spacing16),
            verticalArrangement = Arrangement.spacedBy(DimensResources.spacing16)
        ) {
            InfoRow(
                icon = Icons.Default.Person,
                label = stringResource(TokensResources.firstName),
                value = user?.firstName ?: ""
            )
            InfoRow(
                icon = Icons.Default.Person,
                label = stringResource(TokensResources.lastName),
                value = user?.lastName ?: ""
            )
            InfoRow(
                icon = Icons.Default.Email,
                label = stringResource(TokensResources.email),
                value = user?.email ?: ""
            )
        }
    }
}

@Composable
internal fun ProfessionalInfoSection(professional: Professional) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(DimensResources.spacing16),
            verticalArrangement = Arrangement.spacedBy(DimensResources.spacing16)
        ) {
            InfoRow(
                icon = Icons.Default.HealthAndSafety,
                label = stringResource(TokensResources.specialty),
                value = professional.specialty
            )
            InfoRow(
                icon = Icons.Default.Badge,
                label = stringResource(TokensResources.collegiateNumber),
                value = professional.collegiateNumber
            )
            InfoRow(
                icon = Icons.Default.Phone,
                label = stringResource(TokensResources.phone),
                value = professional.phone
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(DimensResources.spacing16))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
