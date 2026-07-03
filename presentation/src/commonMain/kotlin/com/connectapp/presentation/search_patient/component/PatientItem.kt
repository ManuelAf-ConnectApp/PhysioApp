package com.connectapp.presentation.search_patient.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.connectapp.designresources.TokensResources
import com.connectapp.domain.model.Patient
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PatientItem(
    patient: Patient,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${patient.firstName} ${patient.lastName}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${stringResource(TokensResources.email)}: ${patient.email}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${stringResource(TokensResources.phone)}: ${patient.phone}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
internal fun PatientDetails(
    patient: Patient,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(TokensResources.patientDetails),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onEditClick) {
                Text(stringResource(TokensResources.edit))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DetailItem(label = stringResource(TokensResources.firstName), value = patient.firstName)
        DetailItem(label = stringResource(TokensResources.lastName), value = patient.lastName)
        DetailItem(label = stringResource(TokensResources.dni), value = patient.dni)
        DetailItem(label = stringResource(TokensResources.email), value = patient.email)
        DetailItem(label = stringResource(TokensResources.phone), value = patient.phone)
        DetailItem(label = stringResource(TokensResources.birthDate), value = patient.birthDate)
        DetailItem(label = stringResource(TokensResources.additionalNotes), value = patient.additionalInfo)
    }
}


@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


