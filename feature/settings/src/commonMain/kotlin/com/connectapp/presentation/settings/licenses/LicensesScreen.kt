package com.connectapp.presentation.settings.licenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.connectapp.commonresources.component.LoadingIndicator
import com.connectapp.commonresources.licenses_intro
import com.connectapp.commonresources.licenses_ipad_only
import com.connectapp.commonresources.licenses_load_error
import com.connectapp.commonresources.licenses_public_domain
import com.connectapp.commonresources.licenses_public_domain_note
import com.connectapp.commonresources.resources.Res
import com.connectapp.commonresources.state_collapsed
import com.connectapp.commonresources.state_expanded
import org.jetbrains.compose.resources.stringResource
import kotlin.coroutines.cancellation.CancellationException

/**
 * Settings → open-source licences: the third-party software in the app and the full text of each
 * licence, read from the bundled resources (fully offline). Required by the MIT and BSD-3-Clause
 * licences, whose notices must accompany every binary.
 */
@Composable
fun LicensesScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(licenses_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(openSourceLicenses) { group -> LicenseCard(group) }
        }
    }
}

@Composable
private fun LicenseCard(group: LicenseGroup) {
    var expanded by rememberSaveable(group.textFile) { mutableStateOf(false) }
    val textFile = group.textFile
    val stateText = stringResource(if (expanded) state_expanded else state_collapsed)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (textFile != null) {
                        Modifier
                            .clickable(role = Role.Button) { expanded = !expanded }
                            .semantics { stateDescription = stateText }
                    } else {
                        Modifier
                    },
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (textFile == null) stringResource(licenses_public_domain) else group.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() },
                )
                group.components.forEach { ComponentLine(it) }
                if (textFile == null) {
                    Text(
                        text = stringResource(licenses_public_domain_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (textFile != null) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (expanded && textFile != null) {
            HorizontalDivider()
            LicenseText(textFile)
        }
    }
}

@Composable
private fun ComponentLine(component: OpenSourceComponent) {
    Column {
        Text(component.name, style = MaterialTheme.typography.bodyLarge)
        val ipad = if (component.iPadOnly) " · ${stringResource(licenses_ipad_only)}" else ""
        Text(
            text = "© ${component.holder}$ipad",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LicenseText(textFile: String) {
    // null while loading; "" if the bundled file could not be read.
    val text by produceState<String?>(initialValue = null, textFile) {
        value = try {
            Res.readBytes("files/licenses/$textFile").decodeToString()
        } catch (error: CancellationException) {
            throw error
        } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") error: Exception) {
            ""
        }
    }
    val loaded = text
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        when {
            loaded == null -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            loaded.isEmpty() -> Text(stringResource(licenses_load_error), color = MaterialTheme.colorScheme.error)
            else -> SelectionContainer {
                Text(
                    text = loaded,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }
    }
}
