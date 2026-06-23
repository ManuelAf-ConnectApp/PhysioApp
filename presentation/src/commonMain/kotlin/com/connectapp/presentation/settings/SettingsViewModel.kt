package com.connectapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectapp.domain.usecase.ExportDatabaseUseCase
import com.connectapp.domain.usecase.ImportDatabaseUseCase
import com.connectapp.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val exportDatabaseUseCase: ExportDatabaseUseCase,
    private val importDatabaseUseCase: ImportDatabaseUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state get() = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> get() = _effect.asSharedFlow()

    private val _intents = MutableSharedFlow<SettingsIntent>()
    val intent get() = _intents.asSharedFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SelectFilePath -> {
                _state.value = _state.value.copy(selectedFilePath = intent.path)
            }

            is SettingsIntent.ImportDatabase -> {
                importDatabase(intent.databaseName)
            }

            is SettingsIntent.ExportDatabase -> {
                exportDatabase(intent.databaseName)
            }

            SettingsIntent.Logout -> {
                logout()
            }

            SettingsIntent.DismissImportInfoDialog -> {
                _state.update {
                    it.copy(showImportInfoDialog = !it.showImportInfoDialog)
                }
            }

            SettingsIntent.ShowAboutDialog -> {
                _state.update { it.copy(showAboutDialog = true) }
            }

            SettingsIntent.DismissAboutDialog -> {
                _state.update { it.copy(showAboutDialog = false) }
            }
        }
    }

    private fun importDatabase(databaseName: String) {
        val sourceFilePath = _state.value.selectedFilePath
        if (sourceFilePath.isBlank()) {
            emitEffect(SettingsEffect.ShowSnackbar("Selecciona una base de datos primero"))
            return
        }

        viewModelScope.launch {
            val isImported = importDatabaseUseCase(
                sourceFilePath = sourceFilePath,
                databaseName = databaseName
            )
            if (isImported) {
                emitEffect(SettingsEffect.ShowSnackbar("Base de datos importada correctamente"))
            } else {
                emitEffect(SettingsEffect.ShowSnackbar("Error al importar la base de datos"))
            }
        }
    }

    private fun exportDatabase(databaseName: String) {
        viewModelScope.launch {

            val isExported = exportDatabaseUseCase(databaseName)
            if (isExported != null) {
                emitEffect(SettingsEffect.ShowSnackbar("Base de datos exportada correctamente en $isExported"))
            } else {
                emitEffect(SettingsEffect.ShowSnackbar("Error al exportar la base de datos"))
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            logoutUseCase()
            _state.value = _state.value.copy(isLoading = false)
            emitEffect(SettingsEffect.LogoutSuccess)
        }
    }

    private fun emitEffect(effect: SettingsEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
