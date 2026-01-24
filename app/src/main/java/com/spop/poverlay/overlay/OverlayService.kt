package com.spop.poverlay.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spop.poverlay.MainActivity
import com.spop.poverlay.R
import com.spop.poverlay.overlay.StatCards.OverlayControls
import com.spop.poverlay.sensor.DeadSensorDetector
import com.spop.poverlay.sensor.interfaces.DummySensorInterface
import com.spop.poverlay.sensor.interfaces.PelotonBikeSensorInterfaceV1New
import com.spop.poverlay.sensor.interfaces.PelotonBikePlusSensorInterface
import com.spop.poverlay.sensor.interfaces.SensorInterface
import com.spop.poverlay.util.IsBikePlus
import com.spop.poverlay.util.IsRunningOnPeloton
import com.spop.poverlay.util.LifecycleEnabledService
import com.spop.poverlay.util.disableAnimations
import com.spop.poverlay.overlay.StatCards.statCardCadence
import com.spop.poverlay.overlay.StatCards.statCardDistance
import com.spop.poverlay.overlay.StatCards.statCardResistance
import com.spop.poverlay.overlay.StatCards.statCardCalories
import com.spop.poverlay.overlay.StatCards.statCardDuration
import com.spop.poverlay.overlay.StatCards.statCardHeartRate
import com.spop.poverlay.overlay.StatCards.statCardPower
import com.spop.poverlay.overlay.StatCards.statCardSpeed
import com.spop.poverlay.overlay.StatCards.statCardGear
import com.spop.poverlay.overlay.StatCards.statCardGrade
import com.spop.poverlay.overlay.StatCards.lineChartsMovable
import com.spop.poverlay.overlay.StatCards.alphaSlider
import com.spop.poverlay.overlay.StatCards.gearSlider
import com.spop.poverlay.overlay.StatCards.controlBox
import com.spop.poverlay.overlay.StatCards.StopConfirmationDialog
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt
import com.spop.poverlay.VeloFreeApplication

import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.spop.poverlay.overlay.StatCards.StatCardValues
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class OverlayService : LifecycleEnabledService() {
    companion object {
        private const val OverlayServiceId = 2032
        val EmulatorSensorInterface by lazy { DummySensorInterface() }
    }




    lateinit var sensorViewModel: OverlaySensorViewModel
    lateinit var sensorInterface: SensorInterface
    lateinit var wm: WindowManager
    lateinit var overlayView: ComposeView
    lateinit var controlView: ComposeView

    lateinit var controlBoxView: ComposeView

    lateinit var gradeView: ComposeView
    lateinit var graphViewMove: ComposeView
    lateinit var distanceView: ComposeView
    lateinit var cadenceView: ComposeView
    lateinit var heartRateView: ComposeView

    lateinit var gearView: ComposeView

    lateinit var speedView: ComposeView

    lateinit var powerView: ComposeView

    lateinit var alphaSliderView: ComposeView

    lateinit var gearSliderView: ComposeView

    lateinit var durationView: ComposeView

    lateinit var graphView: ComposeView

    lateinit var caloriesView: ComposeView

    lateinit var resistanceView: ComposeView

    lateinit var confirmDialogView: ComposeView


    // LayoutParams variables

    lateinit var controlParams: LayoutParams
    lateinit var cadenceParams: LayoutParams
    lateinit var distanceParams: LayoutParams
    lateinit var resistanceParams: LayoutParams
    lateinit var caloriesParams: LayoutParams
    lateinit var durationParams: LayoutParams
    lateinit var heartRateParams: LayoutParams
    lateinit var powerParams: LayoutParams
    lateinit var speedParams: LayoutParams
    lateinit var gearParams: LayoutParams
    lateinit var gradeParams: LayoutParams
    lateinit var graphParams: LayoutParams
    lateinit var graphMoveParams: LayoutParams
    lateinit var alphaSliderParams: LayoutParams
    lateinit var gearSliderParams: LayoutParams
    lateinit var controlBoxParams: LayoutParams
    lateinit var confirmDialogParams: LayoutParams

    val globalVariables = VeloFreeApplication.getGlobalVariables()
    val dbHelper = VeloFreeApplication.getDbHelper()

    val statCardValues = StatCardValues()


    override fun onDestroy() {}

    override fun onCreate() {
        super.onCreate()
        val notificationManager = NotificationManagerCompat.from(this)
        startForeground(OverlayServiceId, prepareNotification(notificationManager))

        sensorInterface = if (IsRunningOnPeloton) {
            if (IsBikePlus) {
                PelotonBikePlusSensorInterface(this).also {
                    lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            it.stop()
                        }
                    })
                }
            } else {
                PelotonBikeSensorInterfaceV1New(this).also {
                    lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            it.stop()
                        }
                    })
                }
            }
        } else EmulatorSensorInterface

        sensorViewModel = OverlaySensorViewModel(
            application,
            sensorInterface,
            DeadSensorDetector(sensorInterface, this.coroutineContext),
            onExit = { exit() }
        )

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        buildDialog()

        observeSimMode()
        observeRecordingState()
    }

    private fun observeSimMode() {
        lifecycleScope.launch {
            sensorViewModel.simMode.collectLatest { isSimMode ->
                if (isSimMode) {
                    showSimViews()
                } else {
                    hideSimViews()
                }
            }
        }
    }

    private fun observeRecordingState() {
        lifecycleScope.launch {
            sensorViewModel.recordingState.collectLatest { state ->
                if (state == RecordingState.Confirm) {
                    if (confirmDialogView.parent == null) {
                        wm.addView(confirmDialogView, confirmDialogParams)
                    }
                } else {
                    if (confirmDialogView.parent != null) {
                        wm.removeView(confirmDialogView)
                    }
                }
            }
        }
    }

    private fun showSimViews() {
        if (gearView.parent == null) wm.addView(gearView, gearParams)
        if (gradeView.parent == null) wm.addView(gradeView, gradeParams)
        if (gearSliderView.parent == null) wm.addView(gearSliderView, gearSliderParams)
    }

    private fun hideSimViews() {
        if (gearView.parent != null) wm.removeView(gearView)
        if (gradeView.parent != null) wm.removeView(gradeView)
        if (gearSliderView.parent != null) wm.removeView(gearSliderView)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("overlay service received intent")
        return START_STICKY
    }

    val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        @Suppress("DEPRECATION")
        LayoutParams.TYPE_SYSTEM_ALERT
    }

    private fun buildDialog() {

        val userId = globalVariables.UserIDGet()

        val savedPosGraph = dbHelper.getStatCardPosition(userId, 211)





        val defaultFlags = (LayoutParams.FLAG_NOT_TOUCH_MODAL
                or LayoutParams.FLAG_NOT_FOCUSABLE
                or LayoutParams.FLAG_LAYOUT_NO_LIMITS)


        cadenceView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardCadence(sensorViewModel, 201) }
        }


        val cadenceId = 201







        cadenceView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = cadenceParams.x
                        initialY = cadenceParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            cadenceParams.x = -800
                            cadenceParams.y = -800
                        } else {
                            cadenceParams.x = newX
                            cadenceParams.y = newY
                        }
                        wm.updateViewLayout(cadenceView, cadenceParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            cadenceId,
                            cadenceParams.x,
                            cadenceParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })


        distanceView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardDistance(sensorViewModel, 202) }
        }
        val distanceId = 223




        distanceView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f

            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = distanceParams.x
                        initialY = distanceParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            distanceParams.x = -800
                            distanceParams.y = -800
                        } else {
                            distanceParams.x = newX
                            distanceParams.y = newY
                        }
                        wm.updateViewLayout(distanceView, distanceParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            distanceId,
                            distanceParams.x,
                            distanceParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })

        resistanceView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardResistance(sensorViewModel, 203) }
        }
        val resistanceId = 203






        resistanceView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = resistanceParams.x
                        initialY = resistanceParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            resistanceParams.x = -800
                            resistanceParams.y = -800
                        } else {
                            resistanceParams.x = newX
                            resistanceParams.y = newY
                        }
                        wm.updateViewLayout(resistanceView, resistanceParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            resistanceId,
                            resistanceParams.x,
                            resistanceParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })

        caloriesView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardCalories(sensorViewModel, 204) }
        }
        val caloriesId = 204






        caloriesView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = caloriesParams.x
                        initialY = caloriesParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            caloriesParams.x = -800
                            caloriesParams.y = -800
                        } else {
                            caloriesParams.x = newX
                            caloriesParams.y = newY
                        }
                        wm.updateViewLayout(caloriesView, caloriesParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            caloriesId,
                            caloriesParams.x,
                            caloriesParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })

        durationView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardDuration(sensorViewModel, 205) }
        }
        val durationId = 205






        durationView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = durationParams.x
                        initialY = durationParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            durationParams.x = -800
                            durationParams.y = -800
                        } else {
                            durationParams.x = newX
                            durationParams.y = newY
                        }
                        wm.updateViewLayout(durationView, durationParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            durationId,
                            durationParams.x,
                            durationParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })



        heartRateView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardHeartRate(sensorViewModel, 206) }
        }
        val heartRateId = 206






        heartRateView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = heartRateParams.x
                        initialY = heartRateParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            heartRateParams.x = -800
                            heartRateParams.y = -800
                        } else {
                            heartRateParams.x = newX
                            heartRateParams.y = newY
                        }
                        wm.updateViewLayout(heartRateView, heartRateParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            heartRateId,
                            heartRateParams.x,
                            heartRateParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })


        powerView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardPower(sensorViewModel, 207) }
        }
        val powerId = 207






        powerView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = powerParams.x
                        initialY = powerParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            powerParams.x = -800
                            powerParams.y = -800
                        } else {
                            powerParams.x = newX
                            powerParams.y = newY
                        }
                        wm.updateViewLayout(powerView, powerParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            powerId,
                            powerParams.x,
                            powerParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })



        speedView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardSpeed(sensorViewModel, 208) }
        }
        val speedId = 208






        speedView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = speedParams.x
                        initialY = speedParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            speedParams.x = -800
                            speedParams.y = -800
                        } else {
                            speedParams.x = newX
                            speedParams.y = newY
                        }
                        //wm.updateViewLayout(speedView, speedParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            speedId,
                            speedParams.x,
                            speedParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })


        gearView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardGear(sensorViewModel, 209) }
        }
        val gearId = 209






        gearView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = gearParams.x
                        initialY = gearParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            gearParams.x = -800
                            gearParams.y = -800
                        } else {
                            gearParams.x = newX
                            gearParams.y = newY
                        }
                        wm.updateViewLayout(gearView, gearParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(userId, gearId, gearParams.x, gearParams.y)
                        return true
                    }
                }
                return false
            }
        })



        gradeView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { statCardGrade(sensorViewModel, 210) }
        }
        val gradeId = 210

        gradeView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = gradeParams.x
                        initialY = gradeParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            gradeParams.x = -800
                            gradeParams.y = -800
                        } else {
                            gradeParams.x = newX
                            gradeParams.y = newY
                        }
                        
                        
                        wm.updateViewLayout(gradeView, gradeParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            gradeId,
                            gradeParams.x,
                            gradeParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })

        graphView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { Box(modifier = Modifier.fillMaxSize()) { lineChartsMovable(sensorViewModel) } }
        }
        val graphId = 211


        graphParams = LayoutParams(
            750.dpToPx(),
            220.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosGraph != null) {
                x = savedPosGraph.first
                y = savedPosGraph.second
            } else {
                x = 0
                y = 0
            }
            disableAnimations()
        }

        graphView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = graphParams.x
                        initialY = graphParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -75 || newY < -75 || newX > screenWidth - 50 || newY > screenHeight - 50) {
                            graphParams.x = -800
                            graphParams.y = -800
                        } else {
                            graphParams.x = newX
                            graphParams.y = newY
                        }
                        wm.updateViewLayout(graphView, graphParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            graphId,
                            graphParams.x,
                            graphParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })

        graphViewMove = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { Box(modifier = Modifier.fillMaxSize()) }
        }
        val graphMoveId = 211

        graphViewMove.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = graphParams.x
                        initialY = graphParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = initialX + (event.rawX - initialTouchX).roundToInt()
                        val newY = initialY + (event.rawY - initialTouchY).roundToInt()
                        if (newX < -200 || newY < -125 || newX > screenWidth - 200 || newY > screenHeight - 125) {
                            graphParams.x = -800
                            graphParams.y = -800
                        } else {
                            graphParams.x = newX
                            graphParams.y = newY
                        }
                        wm.updateViewLayout(graphViewMove, graphParams)
                        wm.updateViewLayout(graphView, graphParams)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        dbHelper.updateStatCardPosition(
                            userId,
                            graphId,
                            graphParams.x,
                            graphParams.y
                        )
                        return true
                    }
                }
                return false
            }
        })

        alphaSliderView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { alphaSlider(sensorViewModel ,
                onLockControls = {
                    lockControls() }) }
        }






        gearSliderView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { gearSlider(sensorViewModel) }
        }
        val gearSliderId = 222



        gearSliderParams = LayoutParams(
            resources.displayMetrics.widthPixels.toInt()-100,
            30.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.START
            {
                x = 0
                y = 0
            }
            disableAnimations()
        }
        gearSliderParams.y = 30

        controlBoxView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent { controlBox(sensorViewModel, this.context,   {resetCardPostions()} ) }
        }
        val controlBoxId = 202


        controlBoxParams = LayoutParams(
            275,
            100,

            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            {
                x = 30
                y = 0
            }
            disableAnimations()
        }









        controlView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent {
                val recordingState by sensorViewModel.recordingState.collectAsStateWithLifecycle(
                    initialValue = RecordingState.Stopped
                )
                OverlayControls(
                     sensorViewModel
                     ,
                    onLockControls = {
                        lockControls() })
            }
        }



        controlParams = LayoutParams(
            56.dpToPx(),
            56.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            disableAnimations()
        }

        confirmDialogView = ComposeView(this).apply {
            lifecycleViaService()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OverlayService))
            setContent {
                StopConfirmationDialog(sensorViewModel)
            }
        }

        confirmDialogParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            layoutFlag,
            LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            dimAmount = 0.5f
            disableAnimations()
        }

        setParams()

        wm.addView(controlView, controlParams)
        wm.addView(cadenceView, cadenceParams)
        //wm.addView(distanceView, distanceParams)
        wm.addView(resistanceView, resistanceParams)
        wm.addView(caloriesView, caloriesParams)
        wm.addView(durationView, durationParams)
        wm.addView(heartRateView, heartRateParams)
        wm.addView(powerView, powerParams)
        //wm.addView(speedView, speedParams)
        // wm.addView(gearView, gearParams)
        // wm.addView(gradeView, gradeParams)
        wm.addView(graphView, graphParams)
        wm.addView(graphViewMove, graphMoveParams)
        wm.addView(alphaSliderView, alphaSliderParams)
        // wm.addView(gearSliderView, gearSliderParams)
        wm.addView(controlBoxView, controlBoxParams)
    }

    private fun setParams() {

        var defaultFlags = (LayoutParams.FLAG_NOT_TOUCH_MODAL
                or LayoutParams.FLAG_NOT_FOCUSABLE
                or LayoutParams.FLAG_LAYOUT_NO_LIMITS)



        if (sensorViewModel.lockControls.value){
            defaultFlags = (LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or LayoutParams.FLAG_NOT_FOCUSABLE
                    or LayoutParams.FLAG_LAYOUT_NO_LIMITS or LayoutParams.FLAG_NOT_TOUCHABLE)




        }

val userId = globalVariables.UserIDGet()


        val savedPosResistance = dbHelper.getStatCardPosition(userId, 203)
        val savedPosCalories = dbHelper.getStatCardPosition(userId, 204)
        val savedPosDuration = dbHelper.getStatCardPosition(userId, 205)
        val savedPosHeartRate = dbHelper.getStatCardPosition(userId, 206)
        val savedPosPower = dbHelper.getStatCardPosition(userId, 207)
        val savedPosSpeed = dbHelper.getStatCardPosition(userId, 208)
        val savedPosGear = dbHelper.getStatCardPosition(userId, 209)
        val savedPosGrade = dbHelper.getStatCardPosition(userId, 210)
        val savedPosGraph = dbHelper.getStatCardPosition(userId, 211)
        val savedPosGraphMove = dbHelper.getStatCardPosition(userId, 211)
        val savedPosGearSlider = dbHelper.getStatCardPosition(userId, 222)
        val savedPosControlBox = dbHelper.getStatCardPosition(userId, 202)
        val savedPos = dbHelper.getStatCardPosition(userId, 201)
        val savedPosDistance = dbHelper.getStatCardPosition(userId, 223)




        controlParams = LayoutParams(
            56.dpToPx(),
            56.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            disableAnimations()
        }


        cadenceParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPos != null) {
                x = savedPos.first
                y = savedPos.second
            } else {
                x = 0
                y = (30 +statCardValues.height  *3).dpToPx()
            }
            disableAnimations()
        }

        distanceParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosDistance != null) {
                x = savedPosDistance.first
                y = savedPosDistance.second
            } else {
                x = resources.displayMetrics.widthPixels.toInt() - statCardValues.width.dpToPx()
                y = 125.dpToPx()
            }
            disableAnimations()
        }

        resistanceParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosResistance != null) {
                x = savedPosResistance.first
                y = savedPosResistance.second
            } else {
                x = 0
                y = (30 +statCardValues.height   ).dpToPx()
            }
            disableAnimations()
        }

        caloriesParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosCalories != null) {
                x = savedPosCalories.first
                y = savedPosCalories.second
            } else {
                x = resources.displayMetrics.widthPixels.toInt() - statCardValues.width.dpToPx()
                y = (125    ).dpToPx()
            }
            disableAnimations()
        }

        powerParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosPower != null) {
                x = savedPosPower.first
                y = savedPosPower.second
            } else {
                x = 0
                y = 30.dpToPx()
            }
            disableAnimations()
        }

        durationParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosDuration != null) {
                x = savedPosDuration.first
                y = savedPosDuration.second
            } else {
                x = resources.displayMetrics.widthPixels.toInt() - statCardValues.width.dpToPx()
                y = (125 + 1 * statCardValues.height).dpToPx()
            }
            disableAnimations()
        }

        gradeParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosGrade != null) {
                x = savedPosGrade.first
                y = savedPosGrade.second
            } else {
                x = resources.displayMetrics.widthPixels.toInt() - statCardValues.width.dpToPx()
                y = (125 + 2 * statCardValues.height).dpToPx()
            }
            disableAnimations()
        }

        gearParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosGear != null) {
                x = savedPosGear.first
                y = savedPosGear.second
            } else {
                x = resources.displayMetrics.widthPixels.toInt() - statCardValues.width.dpToPx()
                y = (125 + 3 * statCardValues.height).dpToPx()
            }
            disableAnimations()
        }

        heartRateParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosHeartRate != null) {
                x = savedPosHeartRate.first
                y = savedPosHeartRate.second
            } else {
                x = 0
                y = (30 +statCardValues.height  *2).dpToPx()
            }
            disableAnimations()
        }

        speedParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosSpeed != null) {
                x = savedPosSpeed.first
                y = savedPosSpeed.second
            } else {
                x = 0
                y = (30 +statCardValues.height   ).dpToPx()
            }
            disableAnimations()
        }


        powerParams = LayoutParams(
            statCardValues.width.dpToPx(),
            statCardValues.height.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosPower != null) {
                x = savedPosPower.first
                y = savedPosPower.second
            } else {
                x = 0
                y = 30.dpToPx()
            }
            disableAnimations()
        }

        graphMoveParams = LayoutParams(
            750.dpToPx(),
            220.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosGraphMove != null) {
                x = savedPosGraphMove.first
                y = savedPosGraphMove.second
            } else {
                x = 300
                y = 420.dpToPx()
            }
            disableAnimations()
        }

        graphParams = LayoutParams(
            750.dpToPx(),
            220.dpToPx(),
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (savedPosGraph != null) {
                x = savedPosGraph.first
                y = savedPosGraph.second
            } else {
                x = 300.dpToPx()
                y = 420.dpToPx()
            }
            disableAnimations()
        }

        alphaSliderParams = LayoutParams(
            resources.displayMetrics.widthPixels.toInt() - 250,
            30,
            layoutFlag,
            defaultFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            {
                x = 0
                y = 0
            }
            disableAnimations()
        }
    }

    private fun Int.dpToPx() = (this * resources.displayMetrics.density).roundToInt()

    private fun lockControls() {
         if (sensorViewModel.lockControls.value) sensorViewModel.mutableLockControls.value = false
        else
             sensorViewModel.mutableLockControls.value = true

        setParams()

      //  wm.updateViewLayout(controlView, controlParams)
        wm.updateViewLayout(cadenceView, cadenceParams)
       // wm.updateViewLayout(distanceView, distanceParams)
        wm.updateViewLayout(resistanceView, resistanceParams)
        wm.updateViewLayout(caloriesView, caloriesParams)
        wm.updateViewLayout(durationView, durationParams)
        wm.updateViewLayout(heartRateView, heartRateParams)
        wm.updateViewLayout(powerView, powerParams)
       // wm.updateViewLayout(speedView, speedParams)
        if (sensorViewModel.simMode.value) {
            wm.updateViewLayout(gearView, gearParams)
            wm.updateViewLayout(gradeView, gradeParams)
            wm.updateViewLayout(gearSliderView, gearSliderParams)
        }
        wm.updateViewLayout(graphView, graphParams)
        wm.updateViewLayout(graphViewMove, graphMoveParams)
       // wm.updateViewLayout(alphaSliderView, alphaSliderParams)
      //  wm.updateViewLayout(controlBoxView, controlBoxParams)

    }

    public fun exit() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        stopForeground(true)
        this.stopSelf()
        System.exit(0)
    }

    private fun prepareNotification(notificationManager: NotificationManagerCompat): Notification {
        val channelId = UUID.randomUUID().toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            notificationManager.getNotificationChannel(channelId) == null
        ) {
            val name: CharSequence = getString(R.string.overlay_notification)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, intentFlags)
        val notificationBuilder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(this, channelId)
            } else {
                @Suppress("DEPRECATION")
                NotificationCompat.Builder(this)
            }
        notificationBuilder
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return notificationBuilder.build()
    }

    fun resetCardPostions()
    {
        sensorViewModel.onClearStatCardPositions()

        setParams()

        wm.updateViewLayout(cadenceView, cadenceParams)
       // wm.updateViewLayout(distanceView, distanceParams)
        wm.updateViewLayout(resistanceView, resistanceParams)
        wm.updateViewLayout(caloriesView, caloriesParams)
        wm.updateViewLayout(durationView, durationParams)
        wm.updateViewLayout(heartRateView, heartRateParams)
        wm.updateViewLayout(powerView, powerParams)
        //wm.updateViewLayout(speedView, speedParams)
        if (sensorViewModel.simMode.value) {
            wm.updateViewLayout(gearView, gearParams)
            wm.updateViewLayout(gradeView, gradeParams)
            wm.updateViewLayout(gearSliderView, gearSliderParams)
        }
        wm.updateViewLayout(graphView, graphParams)
        wm.updateViewLayout(graphViewMove, graphMoveParams)
        wm.updateViewLayout(alphaSliderView, alphaSliderParams)
    }
}
