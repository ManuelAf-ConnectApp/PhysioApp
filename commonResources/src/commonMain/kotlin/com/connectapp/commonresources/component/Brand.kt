package com.connectapp.commonresources.component

import androidx.compose.foundation.Image
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.connectapp.commonresources.resources.Res
import com.connectapp.commonresources.resources.logo_symbol
import org.jetbrains.compose.resources.painterResource

/** The PhysioApp symbol (without the wordmark). Decorative: the screen title names the app. */
@Composable
fun PhysioLogo(modifier: Modifier = Modifier) {
    Image(painter = painterResource(Res.drawable.logo_symbol), contentDescription = null, modifier = modifier)
}

/**
 * "PhysioApp" as in the logo: "Physio" in the text colour and "App" in the brand blue. Theme
 * colours instead of the logo's fixed navy keep it readable in dark mode too.
 */
@Composable
fun PhysioWordmark(modifier: Modifier = Modifier, style: TextStyle = LocalTextStyle.current) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) { append("Physio") }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("App") }
        },
        modifier = modifier,
        style = style.copy(fontWeight = FontWeight.Bold),
    )
}
