package pereira.agnaldo.fingersignlibrary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.pow
import kotlin.math.sqrt

//https://github.com/roscrazy/DrawingView
class FingerSignView : View {

    private var mStokeWidth = context.resources.getDimensionPixelSize(R.dimen.strokeWidth)
    private var mLineColor = context.resources.getColor(R.color.lineColor)
    private var mVelocityFilterWeight = context.resources
        .getInteger(R.integer.velocityFilterWeight).toFloat() / 10

    private var mPreviousPoint: Point? = null
    private var mStartPoint: Point? = null
    private var mCurrentPoint: Point? = null

    private var mLastVelocity: Float = 0.toFloat()
    private var mLastWidth: Float = 0.toFloat()
    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mBitmap: Bitmap? = null
    private var mPaintBm = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mCanvasBitmap: Canvas? = null

    /**
     * This method is used to init the mPaints.
     */
    init {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = mStokeWidth.toFloat()
        mPaint.color = mLineColor

        mPaintBm.isAntiAlias = true
        mPaintBm.style = Paint.Style.STROKE
        mPaintBm.strokeJoin = Paint.Join.ROUND
        mPaintBm.strokeCap = Paint.Cap.ROUND
        mPaintBm.strokeWidth = mStokeWidth.toFloat()
        mPaintBm.color = mLineColor

        mPaintBm.alpha = 100
    }

    constructor(context: Context) : super(context) {
        this.setWillNotDraw(false)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        getStyles(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        getStyles(attrs, defStyle)
    }

    private fun getStyles(attrs: AttributeSet, defStyle: Int) {
        this.setWillNotDraw(false)

        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.FingerSignView, defStyle, R.style.defaultFingerSignStyle
        )

        mStokeWidth = typedArray.getDimensionPixelSize(
            R.styleable.FingerSignView_strokeWidth, mStokeWidth
        )

        mVelocityFilterWeight = typedArray.getInteger(
            R.styleable.FingerSignView_velocityFilterWeight, (mVelocityFilterWeight * 10).toInt()
        ).toFloat() / 10

        mLineColor = typedArray.getColor(
            R.styleable.FingerSignView_lineColor, mLineColor
        )
        updateLineStrokeAndColor()
    }

    private fun updateLineStrokeAndColor() {
        mPaint.color = mLineColor
        mPaintBm.color = mLineColor

        mPaint.strokeWidth = mStokeWidth.toFloat()
        mPaintBm.strokeWidth = mStokeWidth.toFloat()
    }

    override fun onLayout(
        changed: Boolean, left: Int, top: Int, right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)

        /**
         * Recreate the bitmap when the layout has changed.
         * Note : after recreate the bitmap, all drawing will be gone.
         */
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.ARGB_8888)
            mCanvasBitmap = Canvas(mBitmap!!)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)


        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // In Action down  mCurrentPoint, mPreviousPoint, mStartPoint will be set at the same point.
                mCurrentPoint = Point(event.x, event.y, System.currentTimeMillis())
                mPreviousPoint = mCurrentPoint
                mStartPoint = mPreviousPoint
            }

            MotionEvent.ACTION_MOVE -> {
                // Those of values present for : ---mStartPoint---mPreviousPoint----mCurrentPoint-----
                mStartPoint = mPreviousPoint
                mPreviousPoint = mCurrentPoint
                mCurrentPoint = Point(event.x, event.y, System.currentTimeMillis())

                // Calculate the velocity between the current point to the previous point
                var velocity = mCurrentPoint!!.velocityFrom(mPreviousPoint)

                // A simple low pass filter to mitigate velocity aberrations.
                velocity = mVelocityFilterWeight * velocity +
                        (1 - mVelocityFilterWeight) * mLastVelocity

                // Calculate the stroke width based on the velocity
                val strokeWidth = getStrokeWidth(velocity)


                // Draw line to the mCanvasBitmap canvas.
                drawLine(mCanvasBitmap, mPaint, mLastWidth, strokeWidth)

                // Tracker the velocity and the stroke width
                mLastVelocity = velocity
                mLastWidth = strokeWidth
            }
            MotionEvent.ACTION_UP -> {
                mStartPoint = mPreviousPoint
                mPreviousPoint = mCurrentPoint
                mCurrentPoint = Point(event.x, event.y, System.currentTimeMillis())
                drawLine(mCanvasBitmap, mPaint, mLastWidth, 0f)
            }
            else -> {
            }
        }

        invalidate()
        return true
    }

    private fun getStrokeWidth(velocity: Float): Float {
        return mStokeWidth - velocity
    }


    override fun onDraw(canvas: Canvas) {
        mBitmap?.let { canvas.drawBitmap(it, 0f, 0f, mPaintBm) }
    }

    // Generate mid point values
    private fun midPoint(p1: Point, p2: Point): Point {
        return Point((p1.x + p2.x) / 2.0f, (p1.y + p2.y) / 2, (p1.time + p2.time) / 2)
    }

    private fun drawLine(canvas: Canvas?, mPaint: Paint, mLastWidth: Float, currentWidth: Float) {
        mPreviousPoint?.let { pp ->
            mStartPoint?.let { sp ->
                mCurrentPoint?.let { cp ->
                    val mid1 = midPoint(pp, sp)
                    val mid2 = midPoint(cp, pp)
                    draw(canvas, mid1, pp, mid2, mPaint, mLastWidth, currentWidth)
                }
            }
        }
    }

    /**
     * This method is used to draw a smooth line. It follow "Bézier Curve" algorithm (it's Quadratic curves).
     *  For reference, you can see more detail here: [Wiki](http://en.wikipedia.org/wiki/B%C3%A9zier_curve)
     *  We 'll draw a  smooth curves from three points. And the stroke size will be changed depend on the start width and the end width
     *
     * @param canvas       : we 'll draw on this canvas
     * @param p0           the start point
     * @param p1           mid point
     * @param p2           end point
     * @param mPaint        the mPaint is used to draw the points.
     * @param mLastWidth    start stroke width
     * @param currentWidth end stroke width
     */
    private fun draw(
        canvas: Canvas?,
        p0: Point,
        p1: Point,
        p2: Point,
        mPaint: Paint,
        mLastWidth: Float,
        currentWidth: Float
    ) {
        var xa: Float
        var xb: Float
        var ya: Float
        var yb: Float
        var x: Float
        var y: Float
        val different = currentWidth - mLastWidth

        var i = 0f
        while (i < 1) {

            // This block of code is used to calculate next point to draw on the curves
            xa = getPt(p0.x, p1.x, i)
            ya = getPt(p0.y, p1.y, i)
            xb = getPt(p1.x, p2.x, i)
            yb = getPt(p1.y, p2.y, i)

            x = getPt(xa, xb, i)
            y = getPt(ya, yb, i)
            //

            // reset strokeWidth
            mPaint.strokeWidth = mLastWidth + different * i
            canvas?.drawPoint(x, y, mPaint)
            i += 0.01f
        }
    }

    // This method is used to calculate the next point cordinate.
    private fun getPt(n1: Float, n2: Float, perc: Float): Float {
        val diff = n2 - n1
        return n1 + diff * perc
    }

    /**
     * This method is used to save the bitmap to an output stream
     *
     * @param outputStream
     */
    fun save(outputStream: OutputStream?) {
        outputStream?.let {
            mBitmap?.compress(Bitmap.CompressFormat.PNG, 80, outputStream)
        }
    }

    fun save(path: String?) {
        path?.let {
            save(FileOutputStream(path))
        }
    }

    fun getSignAsBitmap(): Bitmap? {
        return mBitmap;
    }

    fun drawClear() {
        mBitmap = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.ARGB_8888)
        mCanvasBitmap = Canvas(mBitmap!!)
        invalidate()
    }

    inner class Point(val x: Float, val y: Float, val time: Long) {

        /**
         * Caculate the distance between current point to the other.
         * @param p the other point
         * @return
         */
        private fun distanceTo(p: Point): Float {
            return sqrt((x - p.x).toDouble().pow(2.0) + (y - p.y).toDouble().pow(2.0)).toFloat()
        }


        /**
         * Caculate the velocity from the current point to the other.
         * @param p the other point
         * @return
         */
        fun velocityFrom(p: Point?): Float {
            return distanceTo(p!!) / (this.time - p.time)
        }
    }

}
