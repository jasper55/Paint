package wagner.jasper.paint.ui

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import wagner.jasper.paint.R
import wagner.jasper.paint.util.ViewModelAccessor
import wagner.jasper.paint.util.ViewModelInjector
import android.graphics.*


class CustomCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?
) : View(context, attrs),
    ViewModelAccessor by ViewModelInjector(context) {

    lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private val drawColor = ResourcesCompat.getColor(resources, R.color.paintColor, null)
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.background, null)
    // the drawing will be interpolated and not drawn for every pixel
    // the sensibility of the distance between two points is set here
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    val paintStyle = Paint().apply {
        color = drawColor
        // smooth edges of the drawing
        isAntiAlias = true
        // samples down color with higher precision
        isDither = true
        style = Paint.Style.STROKE //what kind of drawing it is. default: FILL
        strokeJoin = Paint.Join.ROUND //default MITER
        strokeCap = Paint.Cap.ROUND  //default: BUTT
        strokeWidth = STROKE_WIDTH
    }

    init {
        initCanvas()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
        // Draw the drawing so far
        canvas.drawPath(sharedViewModel.path.value!!, paintStyle)
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

    fun drawPath(path: Path) {
        Log.i("SharedViewModel", "drawPath()")
        extraCanvas.drawPath(path, paintStyle)
        invalidate()
    }


    // onSizeChanged is for initilalizing nothing visible happens here
    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
    }

    // TODO restoring path after orientation changes not working yet
    private fun restorePath() {
        sharedViewModel.path.value?.let {
            extraCanvas.drawPath(it, paintStyle)
            invalidate()
        }
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
        return true
    }


    private fun touchMove() {
        if (sharedViewModel.isTouchEventWithinTolerance(touchTolerance)) {
            sharedViewModel.touchMove()
        }
        // forces to redraw the on the screen with the updated path
        invalidate()
    }

    fun setColor(r: Int, g: Int, b: Int) {
        val rgb = Color.rgb(r, g, b)
        paintStyle.color = rgb
    }


    companion object {
        const val STROKE_WIDTH = 12f


    }


}