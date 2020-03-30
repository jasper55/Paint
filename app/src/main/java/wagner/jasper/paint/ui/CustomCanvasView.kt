package wagner.jasper.paint.ui

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import wagner.jasper.paint.util.ViewModelAccessor
import wagner.jasper.paint.util.ViewModelInjector
import android.graphics.*
import android.graphics.Bitmap


class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs),
    ViewModelAccessor by ViewModelInjector(context) {

    val attributeSet = attrs
    private lateinit var extraCanvas: Canvas
    private lateinit var latestCanvas: Canvas
    private lateinit var emptyBitmap: Bitmap
    private lateinit var extraBitmap: Bitmap
    // the drawing will be interpolated and not drawn for every pixel
    // the sensibility of the distance between two points is set here
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    init {
        initCanvas()
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        sharedViewModel.path.value?.let {
            for ((key, value) in it) {
                canvas.drawPath(key, value.paint)
            }
        }
        // Draw any current squiggle
        sharedViewModel.currentPath.value?.let {
            canvas.drawPath(it, sharedViewModel.currentPaint.value!!)
        }
        latestCanvas = canvas
    }

    fun initCanvas() {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        emptyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        latestCanvas = Canvas(emptyBitmap)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(sharedViewModel.backgroundColor.value!!)
    }

    // onSizeChanged is for initilalizing nothing visible happens here
    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
        emptyBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        sharedViewModel.updateXY(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                sharedViewModel.touchStart()
//                captureScreen()
            }
            MotionEvent.ACTION_MOVE -> if (sharedViewModel.isTouchEventWithinTolerance(
                    touchTolerance
                )
            ) {
                sharedViewModel.touchMove()
            }
            MotionEvent.ACTION_UP -> sharedViewModel.touchUp()

        }
        invalidate()
        return true
    }

    fun getLastestBitmap(): Bitmap {
        return emptyBitmap
    }

    fun getBitmap(): Bitmap {
        return extraBitmap
    }

    fun getCanvas(): Canvas {
        return extraCanvas
    }
}
