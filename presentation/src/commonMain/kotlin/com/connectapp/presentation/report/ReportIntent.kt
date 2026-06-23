package com.connectapp.presentation.report

import com.connectapp.domain.model.Patient
import com.connectapp.domain.model.Professional

sealed interface ReportIntent {
    data class Initialize(val reportId: String?) : ReportIntent
    data class ProfessionalSelected(val professional: Professional) : ReportIntent
    data class PatientNameChanged(val name: String) : ReportIntent
    data class PatientSelected(val patient: Patient) : ReportIntent
    data class ReportTitleChanged(val title: String) : ReportIntent
    data class DateChanged(val date: String) : ReportIntent
    data class ClinicalContentChanged(val clinicalContent: String) : ReportIntent
    data class DiagnosisChanged(val diagnosis: String) : ReportIntent
    data class TreatmentChanged(val treatment: String) : ReportIntent
    object SaveClicked : ReportIntent
    object AddPatientClicked : ReportIntent
    object BackClicked : ReportIntent
}
