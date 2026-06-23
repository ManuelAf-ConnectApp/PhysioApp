package com.connectapp.presentation.search_report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.designresources.TokensResources
import com.connectapp.domain.model.Report
import com.connectapp.presentation.component.CustomOutlinedTextField
import com.connectapp.presentation.professional.ProfessionalEffect
import com.connectapp.presentation.search_report.component.ReportItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchReportScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
) {
    val viewModel: SearchReportViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent

    val pdfSaveSuccessMsg = stringResource(TokensResources.pdfSaveSuccess)
    val pdfGenerateErrorMsg = stringResource(TokensResources.pdfGenerateError)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfessionalEffect.NavigateBack -> onNavigateBack()

                is SearchReportEffect.ShowError -> {
                    println("Error: ${effect.message}")
                }

                is SearchReportEffect.SuccessGeneratePdf -> {
                    val message = when(effect.message) {
                        "pdf_save_success" -> pdfSaveSuccessMsg
                        else -> effect.message
                    }
                    snackBarHostState.showSnackbar(message = message)
                }

                is SearchReportEffect.ErrorGeneratePdf -> {
                    val message = when(effect.message) {
                        "pdf_generate_error" -> pdfGenerateErrorMsg
                        else -> effect.message
                    }
                    snackBarHostState.showSnackbar(message = message)
                }
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            SearchReportScreenContent(
                state = state,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
fun SearchReportScreenContent(
    state: SearchReportState,
    onIntent: (SearchReportIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CustomOutlinedTextField(
            value = state.query,
            onValueChange = { onIntent(SearchReportIntent.QueryChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(TokensResources.searchReportPlaceholder),
            placeHolder = stringResource(TokensResources.searchReportPlaceholder),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            if (state.reports.isEmpty()) {
                Text(
                    text = stringResource(TokensResources.noReportsFound),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.reports) { report ->
                        ReportItem(
                            report = report,
                            onClick = { onIntent(SearchReportIntent.ReportSelected(report)) }
                        )
                    }

                    item {
                        state.selectedReport?.let { report ->
                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                            ReportDetails(
                                report = report,
                                onGeneratePdfClick = { onIntent(SearchReportIntent.GeneratePDFClicked) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportDetails(
    report: Report,
    onGeneratePdfClick: () -> Unit
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
                text = stringResource(TokensResources.reportDetails),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onGeneratePdfClick) {
                Text(stringResource(TokensResources.generatePdf))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DetailItem(label = stringResource(TokensResources.labelTitle), value = report.title)
        DetailItem(label = stringResource(TokensResources.pdfDateLabel), value = report.date)
        DetailItem(label = stringResource(TokensResources.labelClinicalContent), value = report.clinicalContent)
        DetailItem(label = stringResource(TokensResources.labelDiagnosis), value = report.diagnosis)
        DetailItem(label = stringResource(TokensResources.labelTreatment), value = report.treatment)
    }
}

@Composable
fun DetailItem(label: String, value: String) {
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

