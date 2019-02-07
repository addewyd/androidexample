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
import android.widget.LinearLayout



class MainActivity : AppCompatActivity(), SensorEventListener {
    val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    var mv:MovementView? = null
    var tv:TextView? = null
    var gx:Float = 0f
    var gy:Float = 0f
    var gz:Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val linLayout = LinearLayout(this)
        linLayout.orientation = LinearLayout.VERTICAL
        val linLayoutParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        setContentView(linLayout, linLayoutParam)

        val lpView = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        tv = TextView(this)
        tv?.height = 120
        tv?.text = "TextView"
        tv?.layoutParams = lpView
        linLayout.addView(tv)

        mv = MovementView(this)
        mv?.init()
        linLayout.addView(mv)

        //setContentView(mv)
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
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

        tv?.text = "Accelerometer %s type %d\nx %.2f\ny %.2f\nz %.2f".format(sn, st,  gx, gy, gz)
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

    var xVel: Float = 0f
    var yVel: Float = 0f

    var cwidth: Int = 0
    var cheight: Int = 0

    var circleRadius: Float = 3f
    val circlePaint: Paint

    init {
        this.circlePaint = Paint() // circlePaint
    }
    lateinit var updateThread: UpdateThread

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                this.xPos = event.x
                this.yPos = event.y
            }
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
    }

    fun updatePhysics() {
        var fact: Float = 50f;

        xPos += xVel
        yPos += yVel

        if (yPos - circleRadius < 0 || yPos + circleRadius > cheight) {
            if (yPos - circleRadius < 0) {
                yPos = circleRadius
            } else {
                yPos = cheight - circleRadius
            }
            yVel = 0f
        } else {
            yVel += gy / fact
        }
        if (xPos - circleRadius < 0 || xPos + circleRadius > cwidth) {
            if (xPos - circleRadius < 0) {
                xPos = circleRadius
            } else {
                xPos = cwidth - circleRadius
            }
            xVel = 0f

        }
        else {
            xVel -= gx / fact
        }
    }
}
