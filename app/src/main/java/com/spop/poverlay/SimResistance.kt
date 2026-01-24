package com.spop.poverlay

import android.content.Context
import android.util.Log
import com.spop.poverlay.overlay.OverlaySensorViewModel

import com.spop.poverlay.sensor.interfaces.SensorInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds

class SimResistance(
    sensorInterface: SensorInterface,
    context: Context,
    sensorViewModel: OverlaySensorViewModel,

    ) {
    var sensorInterface: SensorInterface = sensorInterface
    var context = context
    val sensorViewModel = sensorViewModel

     var instantUpdated: Instant = Clock.System.now()



    private val mutableGear = MutableStateFlow(0L)
    val gear = mutableGear.asStateFlow()


    var _gear: Int = 20
    var _gearOverRide = 20

    var _grade = 0.0 /// grade from sim program

    var _resistanceLevel = 50f /// peloton resistance

    var _lastResistance = 0

    fun setGrade(grade: Double) {
        _grade = grade
        updateBikeResistance()
    }



    fun updateBikeResistance() {

        if (_gear < 0) _gear = 0
        if (_gear > 40) _gear = 40


        var resistanceLevel = 65 + 3.5 * _grade
        resistanceLevel = resistanceLevel - _gear
        if (resistanceLevel < 0)
            resistanceLevel = 0.0
        if (resistanceLevel > 100)
            resistanceLevel = 100.0

        _gearOverRide = 1
        _resistanceLevel = resistanceLevel.toFloat()

        if (_lastResistance != resistanceLevel.toInt())
            try {
                instantUpdated = Clock.System.now()

                sensorInterface.setResistance(resistanceLevel.toInt(), context = context)
                _lastResistance = resistanceLevel.toInt()
            } catch (e: Exception) {
            }


    }

    public fun setGear(newGear: Int) {
        if (newGear > 40) _gear = 40
        else if (newGear < 1) _gear = 1
        else _gear = newGear

        updateBikeResistance()
    }




    // did the user change the knob?
    public fun targetResisitanceChanged(resistance: Float) {

        if (instantUpdated > Clock.System.now() - 500.milliseconds ) return


        if (sensorViewModel.simMode.value == false) return


        if (resistance.toInt() == _lastResistance) return


        _gear = _gear - (resistance.toInt() - _resistanceLevel.toInt())

        if (_gear < 1 || _gear > 40) {
            _lastResistance = -1
            updateBikeResistance()
        } else {
            _resistanceLevel = resistance
            _lastResistance = resistance.toInt()
            sensorViewModel.gear.value = _gear
        }


    }


}