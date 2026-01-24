package com.spop.poverlay.util

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding // Add this
import androidx.compose.foundation.layout.requiredHeight // Add this


import androidx.compose.foundation.gestures.detectDragGestures

import androidx.compose.foundation.layout.width

import androidx.compose.runtime.*

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity

import androidx.compose.ui.unit.dp

import com.spop.poverlay.VeloFreeApplication


@Composable
fun LineChartsMovable(

    heartRateData: Collection<Number>,
    powerData: Collection<Number>,
    cadenceData: Collection<Number>,

    heartRateMaxValue: Float,
    powerMaxValue: Float,
    cadenceMaxValue: Float,


    modifier: Modifier,

    heartRateMinValue: Float = 0f,
    powerMinValue: Float = 0f,
    cadenceMinValue: Float = 0f,

    heartRateAverage: Float = 0f,
    powerAverage: Float = 0f,
    cadenceAverage: Float = 0f,


    offsetx: Int = 0,
    offsety: Int = 0,
    id: Int,

    ) {
    var height: Int = 220
    var width: Int = 750


    val context = LocalContext.current
    val dbHelper = remember { VeloFreeApplication.getDbHelper() }
    val globalVariables = remember { VeloFreeApplication.getGlobalVariables() }
    val density = LocalDensity.current

    var offsetX by remember { mutableStateOf(offsetx.toFloat()) }
    var offsetY by remember { mutableStateOf(offsety.toFloat()) }


    LaunchedEffect(id) {
        val userId = globalVariables.UserIDGet()
        val savedPos = dbHelper.getStatCardPosition(userId, id)
        if (savedPos != null) {
            offsetX = savedPos.first.toFloat()
            offsetY = savedPos.second.toFloat()
        } else {
            offsetX = offsetx.toFloat()
            offsetY = offsety.toFloat()
        }
    }

    Box(
        modifier = Modifier.width(750.dp).height(200.dp)
            .offset(offsetX.dp, offsetY.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        val userId = globalVariables.UserIDGet()
                        dbHelper.updateStatCardPosition(
                            userId,
                            id,
                            offsetX.toInt(),
                            offsetY.toInt()
                        )
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += with(density) { dragAmount.x.toDp().value }
                    offsetY += with(density) { dragAmount.y.toDp().value }
                }
            })

        {


            LineChartMovable(

                data = heartRateData,
                maxValue = heartRateMaxValue,
                modifier = Modifier
                    .width(750.dp)
                    .padding(top =  0.dp)
                    .requiredHeight(100.dp)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 5.dp),
                pauseChart = false,
                fillColor = Color.Red,
                lineColor = Color.Red,
                minValue = heartRateMinValue,
                average = heartRateAverage,
                offsetx = 0,
                offsety = 100,
                id = id,

                )

            LineChartMovable(
                data = powerData,
                maxValue = powerMaxValue,
                modifier =  Modifier
                    .width(750.dp)
                    .padding(top =  0.dp)
                    .requiredHeight(100.dp)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 5.dp),
                pauseChart = false,
                fillColor = Color.Green,
                lineColor = Color.Green,
                minValue = powerMinValue,
                average = powerAverage,
                offsetx = 0,
                offsety = 50,
                id = 100

                )

            LineChartMovable(
                data = cadenceData,
                maxValue = cadenceMaxValue ,
                modifier =   Modifier
                    .width(750.dp)
                    .padding(top =  0.dp)
                    .requiredHeight(100.dp)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 5.dp),
                pauseChart = false,
                fillColor = Color.Blue,
                lineColor = Color.Blue,
                minValue = cadenceMinValue,
                average = cadenceAverage,
                offsetx = 0,
                offsety =  0,
                id = 101


            )





    }
}
