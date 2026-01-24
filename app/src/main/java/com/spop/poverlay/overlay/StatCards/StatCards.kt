package com.spop.poverlay.overlay.StatCards

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.compose.material3.ExperimentalMaterial3Api // Assuming Material 3 is used for context menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spop.poverlay.R
import com.spop.poverlay.overlay.OverlaySensorViewModel
import com.spop.poverlay.overlay.RecordingState

import java.util.Locale
import com.spop.poverlay.util.LineChart


class StatCardValues {
    val width = 120
    val height = 80

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun statCardCadence(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {

    val name = "Cadence"
    val value by sensorViewModel.rpmValue.collectAsStateWithLifecycle("-")

    val averages = true
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)


    val activityAvgCadence by sensorViewModel.activityAvgCadence.collectAsStateWithLifecycle(
        initialValue = 0L
    )
    val activityMaxCadence by sensorViewModel.activityMaxCadence.collectAsStateWithLifecycle(
        initialValue = 0L
    )

    StatCardStatic(
        "Cadence",
        value,
        "\uD83C\uDF00 RPM",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        true,
        activityAvgCadence.toString(),
        activityMaxCadence.toString()
    )
}


@Composable
fun statCardDistance(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {

    val activityDistance by sensorViewModel.activityDistance.collectAsStateWithLifecycle(
        initialValue = 0.0
    )
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)

    val value = String.format(
        Locale.ROOT,
        "%.1f",
        activityDistance
    )
    val name = "Distance"


    val averages = false


    var height: Int = 110
    var width: Int = 125

    if (averages == true) {
        width = 160
    }


    StatCardStatic(
        name,
        value,
        "Miles",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        false,
        "", ""

    )
}

@Composable
fun statCardResistance(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {

    val name = "Resistance"
    val value by sensorViewModel.resistanceValue.collectAsStateWithLifecycle(initialValue = "")

    val averages = false
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)

    var height: Int = 110
    var width: Int = 125

    if (averages == true) {
        width = 160
    }



    StatCardStatic(
        name,
        value,
        "",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        false,
        "", "",
        ""

    )
}

@Composable
fun statCardCalories(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)
    val name = "\uD83D\uDD25 Calories"

    val activityCalories by sensorViewModel.activityCalories.collectAsStateWithLifecycle(
        initialValue = 0
    )
    val value = activityCalories.toString()


    val averages = false


    var height: Int = 110
    var width: Int = 125

    if (averages == true) {
        width = 160
    }



    StatCardStatic(
        name,
        value,
        "",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        false,
        "", "",
        ""

    )
}

@Composable
fun statCardDuration(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)
    val name = "⏱\uFE0F Duration"


    val activityDurationTime by sensorViewModel.activityDurationTime.collectAsStateWithLifecycle(
        initialValue = "-"
    )

     var value = activityDurationTime


    val averages = false

    StatCardStatic(
        name,
        value,
        "",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        false,
        "", "",
        ""

    )
}

@Composable
fun statCardHeartRate(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)
    val name = "HeartRate"

    val heartRate by sensorViewModel.heartRate.collectAsStateWithLifecycle(initialValue = "-")


    val activityMaxHeartRate by sensorViewModel.activityMaxHeartRate.collectAsStateWithLifecycle(
        initialValue = 0
    )

    val activityAvgHeartRate by sensorViewModel.activityAvgHeartRate.collectAsStateWithLifecycle(
        initialValue = 0
    )
    val value = heartRate.toString()


    val averages = true


    var height: Int = 110
    var width: Int = 125

    if (averages == true) {
        width = 160
    }


    StatCardStatic(
        name,
        value,
        "❤ BPM",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        averages,
        activityAvgHeartRate.toString(), activityMaxHeartRate.toString(), ""

    )
}

@Composable
fun statCardPower(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)
    val name = "Power"

    val power by sensorViewModel.powerValue.collectAsStateWithLifecycle(initialValue =  "-")


    val activityAvgPower by sensorViewModel.activityAvgPower.collectAsStateWithLifecycle(
        initialValue = 0
    )
    val activityMaxPower by sensorViewModel.activityMaxPower.collectAsStateWithLifecycle(
        initialValue = 0.0
    )

    val value = power.toString()


    val averages = true


    var height: Int = 110
    var width: Int = 125

    if (averages == true) {
        width = 160
    }


    StatCardStatic(
        name,
        value,
        "⚡ WATTS",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        averages,
        activityAvgPower.toString(), activityMaxPower.toString(), ""

    )
}


@Composable
fun statCardSpeed(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)
    val name = "Speed"

    val speed by sensorViewModel.speedValue.collectAsStateWithLifecycle(initialValue = "-")


    val activityAvgSpeed by sensorViewModel.activityAvgSpeed.collectAsStateWithLifecycle(
        initialValue = 0f
    )

    val avgSpeed = String.format(Locale.ROOT, "%.1f", activityAvgSpeed)
    val activityMaxSpeed by sensorViewModel.activityMaxSpeed.collectAsStateWithLifecycle(
        initialValue = 0.0
    )

    val maxSpeed = String.format(Locale.ROOT, "%.1f", activityMaxSpeed)

    val value = speed.toString()


    val averages = true


    var height: Int = 110
    var width: Int = 125

    if (averages == true) {
        width = 160
    }


    StatCardStatic(
        name,
        value,
        "mph",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        averages,
        avgSpeed.toString(), maxSpeed.toString(), ""

    )
}

@Composable
fun statCardGear(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)
    val name = "⚙\uFE0F Gear"
    val gear by sensorViewModel.gear.collectAsStateWithLifecycle(initialValue = 200)
    val value = (20 - gear / 2).toInt().toString()


    val averages = false


    var height: Int = 110
    var width: Int = 125

    if (averages == true) {
        width = 160
    }





    StatCardStatic(
        name,
        value.toString(),
        "",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        false,
        "", "",
        ""

    )
}


@Composable
fun statCardGrade(
    sensorViewModel: OverlaySensorViewModel,
    id: Int
) {
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)
    val name = "⛰\uFE0F Grade"


    val grade by sensorViewModel.grade.collectAsStateWithLifecycle(initialValue = 10f)


    val gradeString = String.format(Locale.ROOT, "%.1f", grade)
    val value = gradeString


    val averages = false


    var height: Int = 110
    var width: Int = 125

    if (averages == true) {
        width = 160
    }





    StatCardStatic(
        name,
        value.toString(),
        "",
        301,
        0,
        0,
        Modifier.alpha(alpha),
        false,
        "", "",
        ""

    )
}

@Composable
fun lineChartsMovable(

    sensorViewModel: OverlaySensorViewModel,


    ) {
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)

    val heartRateGraphLarge = remember { sensorViewModel.heartRateGraph }
    val powerGraphLarge = remember { sensorViewModel.powerGraphlarge }

    val cadenceGraph = remember { sensorViewModel.cadenceGraph }

    val activityAvgHeartRate by sensorViewModel.activityAvgHeartRate.collectAsStateWithLifecycle(
        initialValue = 0
    )
    val activityAvgPower by sensorViewModel.activityAvgPower.collectAsStateWithLifecycle(
        initialValue = 0
    )

    val activityAvgCadence by sensorViewModel.activityAvgCadence.collectAsStateWithLifecycle(
        initialValue = 0L
    )


    val activityMaxPower by sensorViewModel.activityMaxPower.collectAsStateWithLifecycle(
        initialValue = 0.0
    )
    val activityMaxCadence by sensorViewModel.activityMaxCadence.collectAsStateWithLifecycle(
        initialValue = 0L
    )
    val activityMaxHeartRate by sensorViewModel.activityMaxHeartRate.collectAsStateWithLifecycle(
        initialValue = 0
    )


    val heartRateMaxValue = 120f
    val powerMaxValue = 300f
    val cadenceMaxValue = 100f
    val modifier = Modifier
        .requiredWidth(750.dp)
        .requiredHeight(440.dp)
        .alpha(alpha)
    val heartRateMinValue = 50f
    val powerMinValue = 50f
    val cadenceMinValue = 50f
    val heartRateAverage = activityAvgHeartRate.toFloat()
    val powerAverage = activityAvgPower.toFloat()
    val cadenceAverage = activityAvgCadence.toFloat()
    val offsetx = 0
    val offsety = 0
    val id = 8



    Box(
        modifier = Modifier
            .width(750.dp)
            .height(200.dp)

    ) {


        LineChart(

            data = heartRateGraphLarge,
            maxValue = heartRateMaxValue,
            modifier = Modifier
                .width(750.dp)
                .padding(top = 110.dp)
                .requiredHeight(110.dp)
                .padding(horizontal = 10.dp)
                .padding(bottom = 5.dp)
                .alpha(alpha),
            pauseChart = false,
            fillColor = Color.Red,
            lineColor = Color.Red,
            minValue = heartRateMinValue,
            average = heartRateAverage,


            )

        LineChart(
            data = powerGraphLarge,
            maxValue = powerMaxValue,
            modifier = Modifier
                .width(750.dp)
                .padding(top = 55.dp)
                .requiredHeight(110.dp)
                .padding(horizontal = 10.dp)
                .padding(bottom = 5.dp)
                .alpha(alpha),
            pauseChart = false,
            fillColor = Color.Green,
            lineColor = Color.Green,
            minValue = powerMinValue,
            average = powerAverage


        )

        LineChart(
            data = cadenceGraph,
            maxValue = cadenceMaxValue,
            modifier = Modifier
                .width(750.dp)
                .padding(top = 0.dp)
                .requiredHeight(100.dp)
                .padding(horizontal = 10.dp)
                .padding(bottom = 5.dp)
                .alpha(alpha),
            pauseChart = false,
            fillColor = Color.Blue,
            lineColor = Color.Blue,
            minValue = cadenceMinValue,
            average = cadenceAverage,


            )


    }
}

@Composable
fun alphaSlider(sensorViewModel: OverlaySensorViewModel
                , onLockControls: () -> Unit = {}
) {
    val controlsLocked by sensorViewModel.lockControls.collectAsStateWithLifecycle(
        initialValue = false
    )
    val sliderLocked by sensorViewModel.sliderlockControls.collectAsStateWithLifecycle(
        initialValue = false
    )
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)
    Slider(
        value = alpha,

        onValueChange = {
            sensorViewModel.updateAlpha(it)


            if (it<0.1&&sensorViewModel.mutableSliderLockControls.value==false) {
                onLockControls()
                sensorViewModel.mutableSliderLockControls.value = true
            }
            if (it>=0.1&&sensorViewModel.mutableSliderLockControls.value==true) {
                onLockControls()
                sensorViewModel.mutableSliderLockControls.value = false
            }


        },
        valueRange = 0.0f..1f,
        modifier = Modifier
            .alpha(alpha)
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .height(30.dp)

    )
}

@Composable
fun gearSlider(sensorViewModel: OverlaySensorViewModel) {
    val alpha by sensorViewModel.alpha.collectAsStateWithLifecycle(initialValue = 1f)
    val gear by sensorViewModel.gear.collectAsStateWithLifecycle(initialValue = 10f)

    Slider(
        value = 40 - gear.toFloat(),

        onValueChange = { sensorViewModel.setGear(40 - it.toInt()) },
        valueRange = 0f..40f,
        modifier = Modifier
            .alpha(alpha)
            .fillMaxHeight()
            .background(Color.Transparent)
            .padding(horizontal = 32.dp, vertical = 8.dp)

    )


}

@Composable
fun StopConfirmationDialog(sensorViewModel: OverlaySensorViewModel) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Stop Recording?",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )
            Text(
                text = "Are you sure you want to stop recording this activity?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { sensorViewModel.onStopCancel() }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        sensorViewModel.onStopConfirm()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Stop", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun controlBox(
    sensorViewModel: OverlaySensorViewModel,
    context: Context,
    onClearStatCardPositions: () -> Unit = {}
    ) {
    val recordingState by sensorViewModel.recordingState.collectAsStateWithLifecycle(initialValue = RecordingState.Stopped)


    Box(modifier = Modifier.fillMaxSize()) {

        if (recordingState == RecordingState.Stopped) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.wrapContentSize().align(Alignment.TopEnd)
            ) {
                FloatingActionButton(
                    onClick = {

                        onClearStatCardPositions()


                    },
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Positions")
                }
                Button(
                    onClick = { sensorViewModel.onRecordClicked() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Red, CircleShape)
                    )
                }
                Button(
                    onClick = { sensorViewModel.onExitToHomeScreen() },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp),
                    content = {
                        // Specify the icon using the icon parameter
                        Image(
                            modifier = Modifier
                                .background(Color.White)
                                .requiredHeight(60.dp)
                                .requiredWidth(60.dp)
                                .align(Alignment.CenterVertically)
                                .padding(vertical = 4.dp),
                            painter = painterResource(id = R.drawable.exit),
                            contentDescription = null,
                        )

                    }
                )
            }
        }

        if (recordingState == RecordingState.Recording || recordingState == RecordingState.Confirm) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.wrapContentSize().align(Alignment.TopEnd)
            ) {
                FloatingActionButton(
                    onClick = {

                        onClearStatCardPositions()


                    },
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Positions")
                }
                Button(
                    onClick = { sensorViewModel.onStopClicked() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.size(60.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black)
                    )
                }
            }
        }
    }
}
