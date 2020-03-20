package wagner.jasper.paint.ui

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import wagner.jasper.paint.R
import wagner.jasper.paint.util.ViewModelAccessor
import wagner.jasper.paint.util.ViewModelInjector
import android.graphics.*
import wagner.jasper.paint.model.MyPaint


class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?
) : View(context, attrs),
    ViewModelAccessor by ViewModelInjector(context) {

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val drawColor = ResourcesCompat.getColor(resources, R.color.paintColor, null)
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.background, null)
    // the drawing will be interpolated and not drawn for every pixel
    // the sensibility of the distance between two points is set here
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    val paintStyle = MyPaint.paintStyle.apply {
        color = drawColor
    }

    init {
        initCanvas()
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        sharedViewModel.path.value?.let {
            for ((key, value) in it) {
                canvas.drawPath(key, value)
            }
        }
        // Draw any current squiggle
        sharedViewModel.currentPath.value?.let {
            canvas.drawPath(it, paintStyle)
        }
    }

    private fun initCanvas() {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }

    // onSizeChanged is for initilalizing nothing visible happens here
    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        sharedViewModel.updateXY(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> sharedViewModel.touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> {
                sharedViewModel.touchUp()
                Toast.makeText(activity, "screen touched", Toast.LENGTH_SHORT).show()
            }
        }
        invalidate()
        return true
    }


    private fun touchMove() {
        if (sharedViewModel.isTouchEventWithinTolerance(touchTolerance)) {
            sharedViewModel.touchMove()
        }
    }

    fun setColor(r: Int, g: Int, b: Int) {
        val rgb = Color.rgb(r, g, b)
        paintStyle.color = rgb
    }

    fun toggleErase(eraseOn: Boolean) {
        if (eraseOn) {
            paintStyle.color = backgroundColor
        } else {
            paintStyle.color = drawColor
        }
    }


    companion object {
        const val STROKE_WIDTH = 12f
    }


}