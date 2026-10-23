package com.connectapp.presentation.professionals.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The record as a screen of its own (windows too narrow for list and detail side by side). */
@Composable
fun ProfessionalDetailScreen(
    professionalId: String,
    onEdit: (professionalId: String) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfessionalDetail(professionalId = professionalId, onEdit = onEdit, onDeleted = onDeleted, modifier = modifier)
}
