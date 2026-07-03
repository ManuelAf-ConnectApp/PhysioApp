package com.connectapp.presentation.search_professional.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.connectapp.designresources.DimensResources
import com.connectapp.designresources.TokensResources
import com.connectapp.presentation.model.ProfessionalUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfessionalItem(
    professional: ProfessionalUi,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DimensResources.spacing4)
    ) {
        Column(modifier = Modifier.padding(DimensResources.spacing16)) {
            Text(
                text = "${professional.firstName} ${professional.lastName}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${stringResource(TokensResources.specialty)}: ${professional.specialty}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${stringResource(TokensResources.email)}: ${professional.email}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${stringResource(TokensResources.phone)}: ${professional.phone}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
