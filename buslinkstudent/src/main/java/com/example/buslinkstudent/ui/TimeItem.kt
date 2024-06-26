@file:Suppress("DEPRECATION")

package com.example.buslinkstudent.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buslinkstudent.theme.MyBlue
import com.example.buslinkstudent.theme.TxtBlack
import com.example.buslinkstudent.theme.UberFontFamily
import com.example.common.util.extensions.findNextAvailableTime
import java.time.LocalTime

@Composable
fun TimeItem(time: String, isNearestTime: Boolean) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .offset(y = if (!isNearestTime) (4).dp else 0.dp),
        colors = CardDefaults.cardColors(
            if (isNearestTime) MyBlue else Color.White
        )
    ) {
        Text(
            text = time,
            modifier = Modifier
                .padding(if (isNearestTime) 2.dp else 0.dp)
                .padding(horizontal = if (isNearestTime) 4.dp else 0.dp),
            color = if (isNearestTime) Color.White else TxtBlack,
            fontSize = if (isNearestTime) 14.sp else 12.sp,
            fontWeight = if (isNearestTime) FontWeight.Medium else FontWeight.Medium,
            fontFamily = UberFontFamily
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeList(times: List<String>) {
    val currentTime = LocalTime.now()

    FlowRow(verticalArrangement = Arrangement.Bottom) {
        for (time in times) {
            val isNearestTime = time == findNextAvailableTime(times, currentTime)

            TimeItem(
                time = time,
                isNearestTime = isNearestTime
            )
        }
    }
}