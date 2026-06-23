package com.connectapp.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.connectapp.designresources.DimensResources
import com.connectapp.designresources.TokensResources
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(TokensResources.close))
            }
        },
        title = {
            Text(
                text = stringResource(TokensResources.about),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(DimensResources.spacing12)
            ) {
                Column {
                    Text(
                        text = stringResource(TokensResources.developedBy),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(TokensResources.authorName),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(TokensResources.contactIncidents),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(TokensResources.supportEmail),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Column {
                    Text(
                        text = stringResource(TokensResources.licenseLabel),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(TokensResources.apacheLicense),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    )
}
