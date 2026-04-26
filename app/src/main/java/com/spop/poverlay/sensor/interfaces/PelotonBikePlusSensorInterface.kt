package com.spop.poverlay.sensor.interfaces

import android.content.Context
import android.os.IBinder
import android.os.Parcel
import com.spop.poverlay.sensor.v2.BikePlusCombinedSensor
import com.spop.poverlay.sensor.v2.getV2Binder
import com.spop.poverlay.util.windowed
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlin.coroutines.CoroutineContext

class PelotonBikePlusSensorInterface(val context: Context) : SensorInterface, CoroutineScope {
    companion object{
        /**
         * Resistance is filtered with a moving window since it occasionally spikes
         * The last few resistance readings will grouped, and the lowest reading will be shown
         *
         * The spikes are likely a limitation of ADC accuracy
         */
        const val ResistanceMovingAverageWindowSize = 3
    }
    private val binder = MutableSharedFlow<IBinder>(replay = 1)

    init {
        launch(Dispatchers.IO) {
            val service = getV2Binder(context)
            binder.emit(service)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob()

    fun stop() {
        coroutineContext.cancelChildren()
    }

    private val combinedSensorState = binder.transformLatest { service ->
        val sensor = BikePlusCombinedSensor(service)
        sensor.start()
        emit(sensor)
        try {
            awaitCancellation()
        } finally {
            sensor.stop()
        }
    }.shareIn(this, SharingStarted.Lazily, 1)

    override val power: Flow<Float>
        get() = combinedSensorState.flatMapLatest { it.power }

    override val cadence: Flow<Float>
        get() = combinedSensorState.flatMapLatest { it.cadence }

    override val resistance: Flow<Float>
        get() = combinedSensorState.flatMapLatest { it.resistance }
            .windowed(ResistanceMovingAverageWindowSize, 1, true) { readings ->
                // Resistance sensor occasionally spikes for a single reading
                // So take the least of the last few readings
                readings.minOf { it }
            }





    public fun setResistance2(resistance: Int) {

        launch(Dispatchers.IO) {
            try {

                val iBinder = binder.first()
                val _data = Parcel.obtain()
                try {
                    _data.writeInterfaceToken("com.onepeloton.affernetservice.IBikeInterface")
                    _data.writeInt(resistance )
                    iBinder.transact(7, _data, null, 1)
                } finally {
                    _data.recycle()
                }


            } catch (e: Exception) {

            }


        }
    }


}