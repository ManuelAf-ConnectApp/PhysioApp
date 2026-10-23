package com.connectapp.presentation.patients.sign

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.connectapp.commonresources.button_save
import com.connectapp.commonresources.component.ErrorMessage
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.navigation.GuardUnsavedChanges
import com.connectapp.commonresources.signature.renderSignaturePng
import com.connectapp.core.mvi.MviViewModel
import com.connectapp.domain.patients.AddPatientSignatureUseCase
import com.connectapp.domain.patients.ConsentSignature
import com.connectapp.presentation.patients.consent.ConsentFormState
import com.connectapp.presentation.patients.consent.ConsentSection
import com.connectapp.presentation.patients.toMessage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

data class PatientSignState(
    val consent: ConsentFormState = ConsentFormState(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: StringResource? = null,
) {
    val canSubmit: Boolean get() = consent.invalidFields.isEmpty() && !isSaving
    val hasUnsavedChanges: Boolean get() = !isSaved && !consent.isEmpty
}

sealed interface PatientSignIntent {
    data class ConsentChanged(val consent: ConsentFormState) : PatientSignIntent
    class SaveClicked(val signaturePng: ByteArray) : PatientSignIntent
}

sealed interface PatientSignEffect {
    object Saved : PatientSignEffect
}

/** "Volver a firmar" (D13): adds a signature to the patient's history. */
class PatientSignViewModel(
    private val patientId: String,
    private val addSignature: AddPatientSignatureUseCase,
) : MviViewModel<PatientSignState, PatientSignIntent, PatientSignEffect>(PatientSignState()) {

    override fun reduce(intent: PatientSignIntent) {
        when (intent) {
            is PatientSignIntent.ConsentChanged ->
                updateState { it.copy(consent = intent.consent, errorMessage = null) }
            is PatientSignIntent.SaveClicked -> save(intent.signaturePng)
        }
    }

    private fun save(png: ByteArray) {
        val form = state.value
        if (!form.canSubmit) return
        updateState { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val consent = form.consent
            addSignature(
                patientId,
                ConsentSignature(png, consent.signer, consent.representativeName, consent.representativeDocument),
            )
                .onSuccess {
                    updateState { it.copy(isSaving = false, isSaved = true) }
                    emitEffect(PatientSignEffect.Saved)
                }
                .onFailure { error -> updateState { it.copy(isSaving = false, errorMessage = error.toMessage()) } }
        }
    }
}

@Composable
fun PatientSignScreen(
    patientId: String,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PatientSignViewModel = koinViewModel { parametersOf(patientId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    GuardUnsavedChanges(dirty = state.hasUnsavedChanges)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PatientSignEffect.Saved -> onSaved()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConsentSection(
                state = state.consent,
                onChange = { viewModel.onIntent(PatientSignIntent.ConsentChanged(it)) },
            )
            state.errorMessage?.let {
                ErrorMessage(stringResource(it))
            }
            if (state.isSaving) {
                LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        viewModel.onIntent(PatientSignIntent.SaveClicked(renderSignaturePng(state.consent.strokes)))
                    },
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(stringResource(button_save), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
