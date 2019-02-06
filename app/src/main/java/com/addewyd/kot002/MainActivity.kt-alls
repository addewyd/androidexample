package com.addewyd.kot002

import android.support.v7.app.AppCompatActivity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import android.widget.TextView
import android.graphics.Typeface

class MainActivity : AppCompatActivity(), SensorEventListener {
    val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    var mv:MovementView? = null
    var gx:Float = 0f
    var gy:Float = 0f
    var gz:Float = 0f

    var sensorTA: Sensor? = null
    var sensorTGY: Sensor? = null
    var sensorTMF: Sensor? = null
    var sensorTGR: Sensor? = null
    var sensorTLA: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mv = MovementView(this)
        mv?.init()
        setContentView(mv)
    }

    override fun onResume() {
        super.onResume()
        sensorTA = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorTGY = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorTMF = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorTGR = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorTLA = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        sensorManager.registerListener(
            this,
            sensorTA,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorTGY,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorTMF,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorTGR,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorTLA,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var evt = event!!.sensor
        var sn = evt.name
        var st = evt.type
        var ev = event.values
        gx = ev[0]
        gy = ev[1]
        gz = ev[2]
        mv?.gx = gx
        mv?.gy = gy
        mv?.gz = gz
        mv?.sn = sn
        mv?.st = st
    }
}

// ........................................................................

class UpdateThread(mv: MovementView) : Thread() {
    private var time: Long = 0
    private val fps = 20
    private var toRun = false
    private val movementView: MovementView // ? = null
    private var surfaceHolder: SurfaceHolder
    var c: Canvas? = null

    //constructor(rMovementView: MovementView) : this() {
    //this.movementView = rMovementView
    //}
    init {
        movementView = mv
        surfaceHolder = movementView.getHolder()
    }

    fun setRunning(run: Boolean) {
        toRun = run
    }
    override fun run()
    {

        while(toRun) {
            var cTime: Long = System.currentTimeMillis()
            if((cTime - time) <= (1000.0 / fps) ) {
                c = null

                try {
                    c = surfaceHolder.lockCanvas(null)
                    movementView.updatePhysics();
                    movementView.onDraw(c);
                }
                // catch(e: Exception) {}
                finally {
                    if(c != null) {
                        surfaceHolder.unlockCanvasAndPost(c)
                    }

                }
            }
            time = cTime
        }
    }
}

// ..................................................................................

class MovementView(var ctx: Context) : SurfaceView(ctx), SurfaceHolder.Callback {

    var xPos: Float = 0.0f
    var yPos: Float = 0.0f

    var gx: Float = 0f
    var gy: Float = 0f
    var gz: Float = 0f
    var sn: String = ""
    var st: Int = 0

    var ogx: Float = 0f
    var ogy: Float = 0f
    var ogz: Float = 0f
    var osn: String = ""
    var ost: Int = 0

    var xVel: Int = 2
    var yVel: Int = 2

    var cwidth: Int = 0
    var cheight: Int = 0

    var circleRadius: Float = 3f
    val circlePaint: Paint

    init {
        this.circlePaint = Paint() // circlePaint
    }
    lateinit var updateThread: UpdateThread

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        xPos = event!!.x
        yPos = event.y
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {}
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {}

        }
        return super.onTouchEvent(event)
    }

    override fun surfaceCreated(holder: SurfaceHolder)
    {
        val surfaceFrame = holder.getSurfaceFrame()
        cwidth = surfaceFrame.width()
        cheight = surfaceFrame.height()

        xPos = cwidth / 2.0f
        yPos = circleRadius + 30

        updateThread = UpdateThread(this)
        updateThread.setRunning(true)
        updateThread.start()
    }
    public fun init()
    {
        getHolder().addCallback(this)
        circleRadius = 10f
        circlePaint.setColor(Color.BLUE)
    }

    override fun surfaceChanged(holder: SurfaceHolder, p1:Int, p2: Int, height: Int)
    {}

    override fun surfaceDestroyed(holder: SurfaceHolder)
    {
        var retry: Boolean = false
        updateThread.setRunning(false)
        while (retry) {
            try {
                updateThread.join();
                retry = false;
            } catch (e: InterruptedException) {
            }
        }
    }

    override public fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val brush1 = Paint ()
        brush1.setARGB (155, 155, 0, 0)
        brush1.setTextSize (16f)
        brush1.setTypeface (Typeface.SANS_SERIF)
        canvas?.drawColor(Color.WHITE)
        canvas?.drawCircle(xPos, yPos, circleRadius, circlePaint)
        var sx = "%.2f".format(gx)
        var sy = "%.2f".format(gy)
        var sz = "%.2f".format(gz)

        var sname = ""
        var sty = 0f

        when(st) {
            Sensor.TYPE_ACCELEROMETER -> {
                    sname = "accelerometer"
                sty = 0f
            }
            Sensor.TYPE_GRAVITY -> {
                sname = "gravity"
                sty = 1f
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                sname = "magnetic"
                sty = 2f
            }
            Sensor.TYPE_GYROSCOPE -> {
                sname = "gyroscope"
                sty = 3f
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                sname = "linear accelerometer"
                sty = 4f
            }
        }

        var snst = "%d Name %s - %s".format(st, sn, sname)
        var x = 10f

        canvas?.drawText(sx,  x, 20f + sty * 100, brush1)
        canvas?.drawText(sy,  x, 40f + sty * 100, brush1)
        canvas?.drawText(sz,  x, 60f + sty * 100, brush1)
        canvas?.drawText(snst,x, 80f + sty * 100, brush1)

        if (st != ost) {
            when(ost) {
                Sensor.TYPE_ACCELEROMETER -> {
                    sname = "accelerometer"
                    sty = 0f
                }
                Sensor.TYPE_GRAVITY -> {
                    sname = "gravity"
                    sty = 1f
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    sname = "magnetic"
                    sty = 2f
                }
                Sensor.TYPE_GYROSCOPE -> {
                    sname = "gyroscope"
                    sty = 3f
                }
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    sname = "linear accelerometer"
                    sty = 4f
                }
            }
            var osx = "%.2f".format(ogx)
            var osy = "%.2f".format(ogy)
            var osz = "%.2f".format(ogz)

            var snst = "%d Name %s - %s".format(ost, osn, sname)
            var x = 10f

            canvas?.drawText(osx,  x, 20f + sty * 100, brush1)
            canvas?.drawText(osy,  x, 40f + sty * 100, brush1)
            canvas?.drawText(osz,  x, 60f + sty * 100, brush1)
            canvas?.drawText(snst, x, 80f + sty * 100, brush1)
            ogx = gx
            ogy = gy
            ogz = gz
            osn = sn
            ost = st
        }
    }

    fun updatePhysics() {
        xPos += xVel
        yPos += yVel

        if (yPos - circleRadius < 0 || yPos + circleRadius > cheight) {
            if (yPos - circleRadius < 0) {
                yPos = circleRadius
            } else {
                yPos = cheight - circleRadius
            }
            yVel *= -1
        }
        if (xPos - circleRadius < 0 || xPos + circleRadius > cwidth) {
            if (xPos - circleRadius < 0) {
                xPos = circleRadius
            } else {
                xPos = cwidth - circleRadius
            }
            xVel *= -1
        }
    }
}
