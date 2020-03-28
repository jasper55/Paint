package wagner.jasper.paint.ui

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import wagner.jasper.paint.util.ViewModelAccessor
import wagner.jasper.paint.util.ViewModelInjector
import android.graphics.*


class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?
) : View(context, attrs),
    ViewModelAccessor by ViewModelInjector(context) {

    private lateinit var extraCanvas: Canvas
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
    }

    private fun initCanvas() {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(sharedViewModel.backgroundColor.value!!)
    }

    // onSizeChanged is for initilalizing nothing visible happens here
    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        sharedViewModel.updateXY(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> sharedViewModel.touchStart()
            MotionEvent.ACTION_MOVE -> if (sharedViewModel.isTouchEventWithinTolerance(touchTolerance))
            { sharedViewModel.touchMove() }
            MotionEvent.ACTION_UP -> sharedViewModel.touchUp()

        }
        invalidate()
        return true
    }

    fun getBitmap(): Bitmap {
        return extraBitmap
    }
}
