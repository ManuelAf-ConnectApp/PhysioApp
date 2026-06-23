package com.connectapp.presentation.settings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.connectapp.designresources.DimensResources
import com.connectapp.designresources.TokensResources
import org.jetbrains.compose.resources.stringResource


@Composable
fun DatabaseInfoDialog(
    onDismissRequest: () -> Unit,
){
    AlertDialog(
        onDismissRequest = {onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(TokensResources.understood))
            }
        },
        modifier = Modifier.heightIn(max = DimensResources.screenHeight * 0.8f),
        title = {
            Text(
                text = stringResource(TokensResources.databaseImportGuide),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = DimensResources.spacing8),
                verticalArrangement = Arrangement.spacedBy(DimensResources.spacing16)
            ) {
                Text(
                    text = stringResource(TokensResources.databaseImportInstructions),
                    style = MaterialTheme.typography.bodyMedium
                )

                TableInfo(
                    tableName = "UserEntity",
                    description = "Usuarios con acceso a la aplicación.",
                    attributes = listOf(
                        "id (INTEGER PRIMARY KEY AUTOINCREMENT)",
                        "firstName (TEXT NOT NULL)",
                        "lastName (TEXT NOT NULL)",
                        "email (TEXT NOT NULL UNIQUE)",
                        "password (TEXT NOT NULL)"
                    )
                )

                TableInfo(
                    tableName = "PatientEntity",
                    description = "Información de los pacientes.",
                    attributes = listOf(
                        "id (INTEGER PRIMARY KEY AUTOINCREMENT)",
                        "firstName (TEXT NOT NULL)",
                        "lastName (TEXT NOT NULL)",
                        "birthDate (TEXT NOT NULL)",
                        "dni (TEXT NOT NULL UNIQUE)",
                        "phone (TEXT NOT NULL UNIQUE)",
                        "email (TEXT NOT NULL UNIQUE)",
                        "additionalInfo (TEXT NOT NULL)"
                    )
                )

                TableInfo(
                    tableName = "ProfessionalEntity",
                    description = "Especialistas y profesionales de la salud.",
                    attributes = listOf(
                        "id (INTEGER PRIMARY KEY AUTOINCREMENT)",
                        "firstName (TEXT NOT NULL)",
                        "lastName (TEXT NOT NULL)",
                        "email (TEXT NOT NULL UNIQUE)",
                        "collegiateNumber (TEXT NOT NULL UNIQUE)",
                        "phone (TEXT NOT NULL UNIQUE)",
                        "specialty (TEXT NOT NULL)",
                        "additionalInfo (TEXT NOT NULL)"
                    )
                )

                TableInfo(
                    tableName = "ReportEntity",
                    description = "Informes clínicos generados.",
                    attributes = listOf(
                        "id (INTEGER PRIMARY KEY AUTOINCREMENT)",
                        "patientId (INTEGER NOT NULL, FK -> PatientEntity)",
                        "professionalId (INTEGER NOT NULL, FK -> ProfessionalEntity)",
                        "title (TEXT NOT NULL)",
                        "date (TEXT NOT NULL)",
                        "clinicalContent (TEXT NOT NULL)",
                        "diagnosis (TEXT NOT NULL)",
                        "treatment (TEXT NOT NULL)"
                    )
                )

                TableInfo(
                    tableName = "InvoiceEntity",
                    description = "Facturas emitidas a pacientes.",
                    attributes = listOf(
                        "id (INTEGER PRIMARY KEY AUTOINCREMENT)",
                        "patientId (INTEGER NOT NULL, FK -> PatientEntity)",
                        "professionalId (INTEGER NOT NULL, FK -> ProfessionalEntity)",
                        "amount (REAL NOT NULL)",
                        "date (TEXT NOT NULL)",
                        "concept (TEXT NOT NULL)"
                    )
                )
            }
        }
    )
}


@Composable
private fun TableInfo(
    tableName: String,
    description: String,
    attributes: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(DimensResources.spacing8)) {
        Text(
            text = "Tabla: $tableName",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        attributes.forEach { attr ->
            Text(
                text = "• $attr",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = DimensResources.spacing8)
            )
        }
    }
}