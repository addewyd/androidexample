package com.addewyd.kot002

import android.support.v7.app.AppCompatActivity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View.OnTouchListener
import android.widget.TextView
import android.graphics.Typeface
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import java.lang.Math
import kotlin.math.abs

class MainActivity : AppCompatActivity(), SensorEventListener {
    val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }
    var mv:MovementView? = null
    var tv:TextView? = null

    var aacc = FloatArray(3)
    var amaf = FloatArray(3)
    var arot = FloatArray(3)
    var evnamer = ""
    var evnamem = ""
    var evnamea = ""

    val APP_PREFERENCES:String = "ballsettings"
    val APP_PREFERENCES_GF = "gravfact"
    var mSettings: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val linLayout = LinearLayout(this)
        linLayout.orientation = LinearLayout.VERTICAL
        val linLayoutParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        setContentView(linLayout, linLayoutParam)

        val lpView = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        tv = TextView(this)
        tv?.height = 102
        tv?.text = "TextView"
        tv?.layoutParams = lpView
        linLayout.addView(tv)

        mv = MovementView(this)
        linLayout.addView(mv)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, "Empty")
        menu?.add(0, 2, 0, "Settings")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            1 -> Toast.makeText(this, "empty", Toast.LENGTH_LONG).show()
            2 -> {
                val intent:Intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        if (mSettings?.contains(APP_PREFERENCES_GF) ?: false) {

            val gfact = mSettings?.getFloat(APP_PREFERENCES_GF, 1f)

            mv?.gfact = gfact ?: 1f
            mv?.yVelMax = 0f
            mv?.xVelMax = 0f
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        var evt = event.sensor
        var ev = event.values
        var st = evt?.type ?: - 1
        mv?.st = st

        when (st) {
            Sensor.TYPE_ACCELEROMETER -> {
                aacc = ev
                evnamea = evt.name
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                amaf = ev
                evnamem = evt.name
            }
            Sensor.TYPE_ROTATION_VECTOR-> {
                arot = ev
                evnamer = evt.name
            }
            else -> {

            }
        }

        tv?.text =
            "Accel %s %.2f %.2f %.2f\nMagnetic %s %.2f %.2f %.2f\nRotation %s %.2f %.2f %.2f\n%f %f".
            format(evnamea, aacc[0], aacc[1], aacc[2],
                evnamem, amaf[0], amaf[1], amaf[2],
                evnamer, arot[0], arot[1], arot[2], mv?.xVelMax, mv?.yVelMax)

        mv?.aacc = aacc
        mv?.arot = arot
        mv?.amaf = amaf
    }
}

// ........................................................................

class UpdateThread(mv: MovementView) : Thread() {
    private var time: Long = 0
    private val fps = 20
    private var toRun = false
    private val movementView: MovementView
    private var surfaceHolder: SurfaceHolder
    var c: Canvas? = null

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
                    movementView.updatePhysics()
                    movementView.onDraw(c)
                }
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


// ...............................................................................

class Obstacle(var centerX: Float, var centerY: Float, var radius: Float) {
    fun hit(x:Float, y :Float, br:Float = 0f) : Pair<Boolean, Double> {
        val d = 3f
        val angle = Math.atan2((y - centerY).toDouble(), (x - centerX).toDouble())

        val cdiff = Math.abs(Math.sqrt( ((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)).toDouble() ) - radius)

        val p = Pair<Boolean, Double>(
            cdiff < d,
            angle
        )

        return p
    }
}

// ..................................................................................

class MovementView(var ctx: Context) : SurfaceView(ctx), SurfaceHolder.Callback {

    var xPos: Float = 0.0f
    var yPos: Float = 0.0f

    var aacc = FloatArray(3)
    var arot = FloatArray(3)
    var amaf = FloatArray(3)

    var st:Int = 0

    var xVel: Float = 0f
    var yVel: Float = 0f
    var xVelMax: Float = 0f
    var yVelMax: Float = 0f

    var cwidth: Int = 0
    var cheight: Int = 0

    var circleRadius: Float = 13f
    val circlePaint: Paint
    val hitPaint: Paint
    val oPaint: Paint

    var gfact = 0.5f
    val aobs = arrayOf<Obstacle>(Obstacle(400f, 300f, 200f))

    init {
        this.circlePaint = Paint() // circlePaint
        getHolder().addCallback(this)
        circlePaint.color = Color.BLUE
        oPaint = Paint()
        oPaint.color = Color.LTGRAY
        hitPaint = Paint()
        hitPaint.color = Color.RED
    }

    lateinit var updateThread:UpdateThread// = UpdateThread(this)

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.performClick()
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                this.xPos = event.x
                this.yPos = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                xPos = event.x
                yPos = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {}

        }
        return super.onTouchEvent(event)
    }

    override fun surfaceCreated(holder: SurfaceHolder)
    {
        val surfaceFrame = holder.getSurfaceFrame()
        cwidth = surfaceFrame.width()
        cheight = surfaceFrame.height()

        aobs[0].centerX = cwidth / 2f
        aobs[0].centerY = cheight / 2f
        aobs[0].radius = cwidth / 2f - 16f

        xPos = cwidth / 2.0f
        yPos = cwidth /2 - aobs[0].centerY / 2 - circleRadius

        updateThread = UpdateThread(this)


        updateThread.setRunning(true)
        //if (updateThread.state == Thread.State.TERMINATED)
        Toast.makeText(ctx, when(updateThread.state) {
            Thread.State.TERMINATED -> "T"
            Thread.State.BLOCKED -> "B"
            Thread.State.NEW -> "N"
            Thread.State.RUNNABLE -> "R"
            Thread.State.TIMED_WAITING -> "TW"
            Thread.State.WAITING -> "W"
            else -> "NA"
        }, Toast.LENGTH_LONG).show()
        try {
            updateThread.start()
        }
        catch(e:Exception) {
            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
        }
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

        aobs.forEach {
            canvas?.drawCircle(it.centerX, it.centerY, it.radius, oPaint)
        }

        canvas?.drawCircle(xPos, yPos, circleRadius, circlePaint)

    }

    fun updatePhysics() {
        //val fact = 20f;
        val r = 0.5f
        xPos += xVel
        yPos += yVel

        aobs.forEach {
            var p = it.hit(xPos, yPos)
            circlePaint.color  = if(p.first) Color.RED else Color.BLUE

            if (p.first) {
                val angle = p.second

                val b = Math.atan2(yVel.toDouble(), xVel.toDouble())
                val g = angle - b

                yVel = yVel * Math.sin(g).toFloat() * r
                xVel = xVel * Math.cos(g).toFloat() * r
            }
        }

        if (yPos - circleRadius < 0 || yPos + circleRadius > cheight) {
            yPos = if (yPos - circleRadius < 0) circleRadius else  cheight - circleRadius
            yVel = -yVel * r
        } else {
            yVel += aacc[1] / gfact
        }
        if (xPos - circleRadius < 0 || xPos + circleRadius > cwidth) {
            xPos = if (xPos - circleRadius < 0) circleRadius else cwidth - circleRadius
            xVel = -xVel * r
        } else {
            xVel -= aacc[0] / gfact
        }

        xVelMax = Math.max(abs(xVel), xVelMax)
        yVelMax = Math.max(abs(yVel), yVelMax)

    }
}
