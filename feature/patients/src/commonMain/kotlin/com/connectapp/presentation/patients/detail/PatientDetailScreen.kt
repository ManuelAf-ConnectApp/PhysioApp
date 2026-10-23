package com.connectapp.presentation.patients.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The record as a screen of its own (windows too narrow for list and detail side by side). */
@Composable
fun PatientDetailScreen(
    patientId: String,
    onEdit: (patientId: String) -> Unit,
    onSignAgain: (patientId: String) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PatientDetail(patientId, onEdit, onSignAgain, onDeleted, modifier)
}
