package com.spop.poverlay.util



import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment



import androidx.compose.ui.unit.dp



@Composable
fun LineChartMovable(

    data: Collection<Number>,
    maxValue: Float,
    modifier: Modifier,
    pauseChart: Boolean,
    fillColor: Color = Color.LightGray,
    lineColor: Color = Color.DarkGray,
    minValue: Float = 0f,
    average: Float = 0f,
    offsetx: Int = 0,
    offsety: Int = 0,
    id: Int,

    ) {
    var height: Int = 110
    var width: Int = 750





    var offsetX by remember { mutableStateOf(offsetx.toFloat()) }
    var offsetY by remember { mutableStateOf(offsety.toFloat()) }


    Box(
        modifier = Modifier
            .offset(offsetX.dp, offsetY.dp)

    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .width(width.dp)


        )
        {


            LineChart(

                data = data,
                maxValue = maxValue,
                modifier = modifier,
                pauseChart = pauseChart,
                fillColor = fillColor,
                lineColor = lineColor,
                minValue = minValue,
                average = average,
            )


        }


    }
}

