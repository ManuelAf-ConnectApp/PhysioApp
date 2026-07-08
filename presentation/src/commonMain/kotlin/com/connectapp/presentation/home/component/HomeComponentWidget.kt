package com.connectapp.presentation.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.connectapp.designresources.DimensResources


@Composable
fun HomeComponentWidget(
    onEmailClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(bottom = DimensResources.spacing32),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Desarrollado por: Manuel María Alconchel Fernández",
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = "ConnectApp (ES)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            modifier = Modifier.clickable {
                onEmailClicked()
            },
            text = "Email: incidencias@connectapp.es",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {

            Text(
                text = "Physio App",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}