package com.spop.poverlay.sensor.interfaces

import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.RemoteException
import com.spop.poverlay.util.calculateSpeedFromPelotonV1Power
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SensorInterface {
    val power: Flow<Float>
    val cadence: Flow<Float>
    val resistance: Flow<Float>
    val speed
        get() = power.map(::calculateSpeedFromPelotonV1Power)



    fun setResistance(resistance: Int, context: Context) {
        if (Build.MODEL == "PLTN-TTR01") {
            val pbs: PelotonBikePlusSensorInterface = PelotonBikePlusSensorInterface(context)


            pbs.setResistance2(resistance)
            // Log.d("resistance", "resistance: $resistance")

        }

    }
}