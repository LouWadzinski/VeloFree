package com.spop.poverlay.overlay.StatCards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.material3.ExperimentalMaterial3Api // Assuming Material 3 is used for context menu
import com.spop.poverlay.VeloFreeApplication


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StatCardStatic(
    name: String,
    value: String,
    unit: String,
    id: Int,
    offsetx: Int = 0,
    offsety: Int = 0,
    modifier: Modifier,
    averages: Boolean = false,
    avg: String = "",
    max: String = "",
    batteryPCT: String = ""

) {
    val statCardValues = StatCardValues()
    var height: Int = statCardValues.height
    var width: Int = statCardValues.width


    val context = LocalContext.current
    val dbHelper = remember { VeloFreeApplication.getDbHelper() }
    val globalVariables = remember { VeloFreeApplication.getGlobalVariables() }
    val density = LocalDensity.current

    var offsetX by remember { mutableStateOf(offsetx.toFloat()) }
    var offsetY by remember { mutableStateOf(offsety.toFloat()) }




    Box(
        modifier = Modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .width(width.dp)
                .background(Color.Black, shape = RoundedCornerShape(16.dp))


        ) {
            if (averages == false) {
                var ffontSize: Int = 48
                ffontSize = 32


                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.height(height.dp)
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = ffontSize.sp,
                        fontWeight = FontWeight.Bold
                    )

                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    var ffontSize: Int = 32

                    Row(modifier = Modifier
                        .width(width.dp)
                        .height(height.dp)) {
                        Column(

                            modifier = Modifier.width((width * .66).toInt().dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = unit,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                style = TextStyle(
                                    lineHeight = 16.sp  // Adjust this value to your desired line height

                                )

                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = value,
                                color = Color.White,
                                fontSize = ffontSize.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(
                                    lineHeight = 16.sp  // Adjust this value to your desired line height

                                )

                            )

                        }
                        Column(
                            modifier = Modifier.width((width * .33).toInt().dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "avg:\n $avg ", fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                                style = TextStyle(


                                )

                            )
                            Text(
                                text = "max:\n$max", fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                                style = TextStyle(


                                )


                            )

                        }
                    }
                }
            }

        }
    }
}
