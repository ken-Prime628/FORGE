package com.kennedy.forge.ui.screens.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kennedy.forge.ui.theme.GoldPrimary
import com.kennedy.forge.ui.theme.SoftOlive

@Composable
fun Indicator(current: Int) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        repeat(3) { index ->

            val color = if (index + 1 == current) {
                GoldPrimary   // active dot
            } else {
                SoftOlive     // inactive dot
            }

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(10.dp)
                    .background(color, shape = CircleShape)
            )
        }
    }
}